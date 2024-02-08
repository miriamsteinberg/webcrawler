package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebCrawlerApplication {



    public static void main(String[] args) {
        System.out.println("Hello World");
        SpringApplication.run(WebCrawlerApplication.class, args);
    }

}
