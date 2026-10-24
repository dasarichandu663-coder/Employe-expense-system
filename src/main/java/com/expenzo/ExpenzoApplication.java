package com.expenzo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenzoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenzoApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  EXPENZO: ADVANCE & EXPENSE SETTLEMENT SYSTEM STARTED");
        System.out.println("  Open Frontend: http://localhost:8080/");
        System.out.println("  Admin Login:   admin@expenzo.com (or admin@company.com / admin123)");
        System.out.println("  Employee Demo: sarah@company.com / pass123");
        System.out.println("==================================================================");
    }
}
