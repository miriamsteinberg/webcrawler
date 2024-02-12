package com.example;

import com.example.webcrawler.WebPageReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        String url = args == null || args.length == 0 ? "www.hamichlol.org.il/" : args[0];
        int depth = args == null || args.length < 2 ? 3 : Integer.parseInt(args[1]);
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        WebPageReader webPageReader = context.getBean(WebPageReader.class);
        webPageReader.findLinks(url, depth, 1);
    }

}
