package com.example.webcrawler;

import com.example.model.LinkData;
import com.example.producer.KafkaProducer;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

@Service
public class WebPageReader {

    private final Logger logger = LoggerFactory.getLogger(WebPageReader.class);
    private final RedisTemplate<String, String> redisTemplate;

    private final KafkaProducer kafkaProducer;

    public static String[] PREFIXES = {"https://", "http://"};

    public WebPageReader(RedisTemplate<String, String> redisTemplate, KafkaProducer kafkaProducer) {
        this.redisTemplate = redisTemplate;
        this.kafkaProducer = kafkaProducer;
    }

    private String getValidUrl(String url) {
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

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    private boolean isHtml(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            String contentType = connection.getHeaderField("Content-Type");
            return contentType != null && contentType.contains("text/html");
        } catch (IOException e) {
            return false;
        }
    }


    public void findLinks(String url, int depth, int index) {
        if (index > depth) {
            return;
        }
        String urlValue = redisTemplate.opsForValue().get(url);
        if (Strings.isNotBlank(urlValue)) {
//            logger.info("Exist link - " + url);
            redisTemplate.opsForValue().set(url, "");//TODO - remove
            return;
        }
        try {
            String validUrl = getValidUrl(url);
            if (validUrl == null) {
//                logger.warn("Invalid link: " + url);
                return;
            }
//            if (!isHtml(validUrl)) {
//                logger.warn("Not HTML link: " + url);
//                return;
//            }
            Document doc = Jsoup.connect(validUrl).get();
            String html = doc.html();
            redisTemplate.opsForValue().set(validUrl, html);

            Document htmlDoc = Jsoup.parse(html);
            Elements links = htmlDoc.select("a[href]");

            int totalLinks = links.size();
            int sameDomainLinks = 0;
            String host = new URL(validUrl).getHost();

            for (Element link : links) { // bulk links message
                String href = link.attr("abs:href");
                if (Strings.isNotBlank(href)) {
//                    logger.info((" ".repeat(index)) + index + ": Link: " + href);
                    kafkaProducer.sendMessage(new LinkData(href, depth, index + 1));
                    if (href.contains(host)) {
                        sameDomainLinks++;
                    }
                }
            }
            double rank = (double) sameDomainLinks / totalLinks;
            logger.info(index + ": URL: " + url + " Depth: " + index + " Ratio: " + rank);
        } catch (Exception e) {
//            logger.error(index + ": Error in link - " + url + " Message - " + e.getMessage());
        }
    }
}
