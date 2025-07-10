package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.MessageDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class PaymentSender {

    @Value("${payment-processor.url.default}")
    private String urlDefault;

    @Value("${payment-processor.url.fallback}")
    private String urlFallback;

    public void sendPayment(PaymentDto paymentDto) {
        WebClient defaultClient = getWebClient(urlDefault);
        WebClient fallbackClient = getWebClient(urlFallback);

        sendPaymentWithRetries(defaultClient, paymentDto)
                .onErrorResume(ex -> sendPaymentWithoutRetries(fallbackClient, paymentDto))
                .subscribe();
    }

    private Mono<MessageDto> sendPaymentWithRetries(WebClient webClient, PaymentDto paymentDto) {
        return webClient.post()
                .uri("/payments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(paymentDto)
                .retrieve()
                .bodyToMono(MessageDto.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));
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
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
