package com.example;

import com.example.webcrawler.WebPageReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Cmd implements CommandLineRunner {
    private final WebPageReader webPageReader;


    public Cmd( WebPageReader webPageReader) {
        this.webPageReader = webPageReader;
    }

    @Override
    public void run(String... args) {
        // Add your code here that you want to run when the application starts
        String url = "www.hamichlol.org.il/"; // Replace with the desired URL
        webPageReader.findLinks(url, 3, 1);

    }
}
