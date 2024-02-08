package org.example;

import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class WebPageReader {

    private final RedisDataService redisDataService;

    // redis
    // pub sub
    public static String[] PREFIXES = {"http://", "https://"};

    public WebPageReader(RedisDataService redisDataService) {
        this.redisDataService = redisDataService;
    }

//    public  void main(String[] args) {
//        String url = "https://www.hamichlol.org.il/"; // Replace with the desired URL
//        findLinks(url, 3, 1);
//    }

    public String getValidUrl(String url) {
        if (isValidURL(url)) {
            return url;
        }
        for (String prefix : PREFIXES) {
            if (isValidURL(prefix + url)) {
                return prefix + url;
            }
        }
        return null;
    }

    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public void findLinks(String url, int depth, int index) {
        if (index > depth) {
            return;
        }
        if(Boolean.TRUE.equals(redisDataService.exits(url).block())){
            return;
        }
        try {
            Document doc = Jsoup.connect(url).get();
            String html = doc.html();
            redisDataService.set(url, html);

            Document htmlDoc = Jsoup.parse(html);
            Elements links = htmlDoc.select("a[href]");
            for (Element link : links) {
                if (Strings.isNotBlank(link.attr("abs:href"))) {
                    String validUrl = getValidUrl(link.attr("abs:href"));
                    if (validUrl == null) {
                        System.out.println("Invalid Link: " + link.attr("abs:href"));
                    } else {
                        System.out.println((" ".repeat(index)) + index + ": Link: " + validUrl);
                        findLinks(validUrl, depth, index + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(index + ": Error in link - " + url + " Message: " + e.getMessage());
        }
    }
}
