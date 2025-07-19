package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.entity.Payment;
import com.rinha.backend.rinhabackend2025.enums.StatusEnum;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PaymentJob {

    private final PaymentSender sender;
    private final PaymentRepository repository;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private JdbcService jdbcService;

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @PostConstruct
    public void setConnection(){
        jdbcService = new JdbcService(url, username, password);
    }

    public PaymentJob(PaymentSender sender, PaymentRepository repository) {
        this.sender = sender;
        this.repository = repository;
    }

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
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("ERRO: " + e.getMessage());
                    System.out.println("Erro processamento");
                    e.printStackTrace();
                }
            });

//            repository.saveAll(paymentsComplete);
            jdbcService.saveAll(paymentsComplete);
        }
    }

}
