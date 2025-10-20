package co.istad.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class TransactionCommandApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TransactionCommandApp.class, args);
    }

    private final TransactionService transactionService;

    @Override
    public void run(String... args) throws Exception {
        transactionService.deposit();
    }
}
