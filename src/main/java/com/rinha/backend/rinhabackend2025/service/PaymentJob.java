package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.entity.Payment;
import com.rinha.backend.rinhabackend2025.enums.StatusEnum;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PaymentJob {

    private final PaymentSender sender;
    private final PaymentRepository repository;

    public PaymentJob(PaymentSender sender, PaymentRepository repository) {
        this.sender = sender;
        this.repository = repository;
    }

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @Transactional
    @Scheduled(fixedDelay = 100)
    public void processingPayments(){

        List<Payment> batchPayment = new ArrayList<>();

        if (isProcessing.compareAndSet(false, true)){
            try {
                 batchPayment = repository.findPendingPayment();
            } finally {
                isProcessing.set(false);
            }
        }

        if (batchPayment.isEmpty()) return;

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            //List<Future<Pair<Payment, String>>> futures = new ArrayList<>();

            List<CompletableFuture<Pair<Payment, String>>> futures = batchPayment.stream()
                    .map(payment -> CompletableFuture.supplyAsync(() -> sender.sendPayment(payment).block()))
                    .toList();

            List<Payment> paymentsComplete = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .map(result -> {
                        Payment p = result.getFirst();
                        p.setStrategy(result.getSecond());
                        p.setStatus(StatusEnum.OK);
                        return p;
                    })
                    .toList();

            repository.saveAll(paymentsComplete);
        }
    }
}
