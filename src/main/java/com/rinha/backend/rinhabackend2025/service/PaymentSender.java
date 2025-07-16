package com.rinha.backend.rinhabackend2025.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rinha.backend.rinhabackend2025.dto.MessageDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class PaymentSender {

    private final ObjectMapper objectMapper;

    @Value("${payment-processor.url.default}")
    private String urlDefault;

    @Value("${payment-processor.url.fallback}")
    private String urlFallback;

    private WebClient defaultClient;
    private WebClient fallbackClient;

    public PaymentSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init(){
        defaultClient = getWebClient(urlDefault);
        fallbackClient = getWebClient(urlFallback);
    }

    public Mono<Pair<Payment, String>> sendPayment(Payment payment) {

        PaymentDto paymentDto = new PaymentDto(
                payment.getCorrelationId(), payment.getAmount(), payment.getTimestamp()
        );

        System.out.println("CONVERTENDO AQUI");

        try {
            String json = objectMapper.writeValueAsString(paymentDto);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return attemptSend(payment, paymentDto, defaultClient, "default", 1, true);
    }

    private Mono<Pair<Payment, String>> attemptSend(
            Payment payment,
            PaymentDto paymentDto,
            WebClient currentClient,
            String currentClientName,
            int attemptCount,
            boolean useRetriesForCurrentAttempt) {

        Mono<MessageDto> sendMono;
        if (useRetriesForCurrentAttempt) {
            sendMono = sendPaymentWithRetries(currentClient, paymentDto);
        } else {
            sendMono = sendPaymentWithoutRetries(currentClient, paymentDto);
        }

        return sendMono
                .map(messageDto -> {
                    System.out.println("Payment sent successfully via " + currentClientName + " URL (attempt " + attemptCount + ") for correlationId: " + paymentDto.getCorrelationId());
                    return Pair.of(payment, currentClientName);
                })
                .onErrorResume(ex -> {
                    System.err.println("Attempt " + attemptCount + " via " + currentClientName + " failed for correlationId: " + paymentDto.getCorrelationId() + ". Error: " + ex.getMessage());

                    WebClient nextClient;
                    String nextClientName;

                    if (currentClient == defaultClient) {
                        nextClient = fallbackClient;
                        nextClientName = "fallback";
                    } else {
                        nextClient = defaultClient;
                        nextClientName = "default";
                    }

                    return attemptSend(payment, paymentDto, nextClient, nextClientName, attemptCount + 1, false);
                });
    }

    private Mono<MessageDto> sendPaymentWithRetries(WebClient webClient, PaymentDto paymentDto) {
        return webClient.post()
                .uri("/payments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(paymentDto)
                .retrieve()
                .bodyToMono(MessageDto.class)
                .retryWhen(Retry.backoff(5, Duration.ofMillis(10)));
    }

    private Mono<MessageDto> sendPaymentWithoutRetries(WebClient webClient, PaymentDto paymentDto) {
        return webClient.post()
                .uri("/payments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(paymentDto)
                .retrieve()
                .bodyToMono(MessageDto.class);
    }

    private WebClient getWebClient(String baseUrl) {
        return WebClient.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                })
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
