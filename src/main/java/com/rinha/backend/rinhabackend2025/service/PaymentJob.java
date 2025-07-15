package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import com.rinha.backend.rinhabackend2025.enums.StatusEnum;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class PaymentJob {

    private final PaymentSender sender;
    private final PaymentRepository repository;

    public PaymentJob(PaymentSender sender, PaymentRepository repository) {
        this.sender = sender;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 1000)
    private void processingPayments(){

        List<Payment> batchPayment = repository.findPendingPayment();

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<Pair<Payment, String>>> futures = new ArrayList<>();

            for (Payment payment : batchPayment){
                futures.add(executor.submit( () -> sender.sendPayment(payment).block()));
            }

            List<Payment> paymentsComplete = new ArrayList<>();

            futures.forEach(future -> {
                try {
                    Pair<Payment, String> paymentStringPair = future.get();
                    Payment payment = paymentStringPair.getFirst();
                    payment.setStrategy(paymentStringPair.getSecond());
                    payment.setStatus(StatusEnum.OK);
                    paymentsComplete.add(payment);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Erro processamento");
                }
            });

            repository.saveAll(paymentsComplete);
        }
    }
}
