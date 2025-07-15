package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.MessageDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
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

    private final PaymentRepository repository;

    @Value("${payment-processor.url.default}")
    private String urlDefault;

    @Value("${payment-processor.url.fallback}")
    private String urlFallback;

    public PaymentSender(PaymentRepository repository) {
        this.repository = repository;
    }

    private WebClient defaultClient;
    private WebClient fallbackClient;

    @PostConstruct
    public void init(){
        defaultClient = getWebClient(urlDefault);
        fallbackClient = getWebClient(urlFallback);
    }

    public void sendPayment(PaymentDto paymentDto) {

        sendPaymentWithRetries(defaultClient, paymentDto)
                .doOnSuccess(messageDto -> {
                    repository.save(
                            new Payment(null, paymentDto.getCorrelationId(), "default", paymentDto.getAmount(), paymentDto.getRequestedAt())
                    );
                    System.out.println("Payment sent successfully via default URL for correlationId: " + paymentDto.getCorrelationId());
                })
                .onErrorResume(ex -> {
                    System.err.println("Default payment failed for correlationId: " + paymentDto.getCorrelationId() + ". Attempting fallback. Error: " + ex.getMessage());
                    return sendPaymentWithoutRetries(fallbackClient, paymentDto)
                            .doOnSuccess(messageDto -> {
                                repository.save(
                                        new Payment(null, paymentDto.getCorrelationId(), "fallback", paymentDto.getAmount(), paymentDto.getRequestedAt())
                                );
                                System.out.println("Payment sent successfully via fallback URL for correlationId: " + paymentDto.getCorrelationId());
                            })
                            .onErrorResume(fallbackEx -> { // Captura o erro do fallback (incluindo 422)
                                System.err.println("Failed to send payment via both default and fallback for correlationId: " + paymentDto.getCorrelationId() + ". Final Error: " + fallbackEx.getMessage());
                                // Salva o pagamento com status 'failed' no banco de dados local
                                repository.save(
                                        new Payment(null, paymentDto.getCorrelationId(), "failed", paymentDto.getAmount(), paymentDto.getRequestedAt())
                                );
                                // Retorna Mono.empty() para "consumir" o erro e evitar que chegue ao subscribe final
                                return Mono.empty();
                            });
                })
                .subscribe(
                        messageDto -> System.out.println("Overall payment processing completed for correlationId: " + paymentDto.getCorrelationId()),
                        // Este onError só será chamado se um erro não for tratado pelos onErrorResume anteriores
                        throwable -> System.err.println("Overall payment processing failed for correlationId: " + paymentDto.getCorrelationId() + ". Unhandled error: " + throwable.getMessage())
                );
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
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
