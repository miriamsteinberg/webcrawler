package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyCommandLineRunner implements CommandLineRunner {

    private final WebPageReader webPageReader;

    public MyCommandLineRunner(WebPageReader webPageReader) {
        this.webPageReader = webPageReader;
    }

    @Override
    public void run(String... args) throws Exception {
        // Add your code here that you want to run when the application starts
        System.out.println("This code will run when the application starts");
        String url = "https://www.hamichlol.org.il/"; // Replace with the desired URL
        webPageReader.findLinks(url, 3, 1);
    }
}
