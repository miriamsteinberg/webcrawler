package com.example.webcrawler;

import com.example.model.LinkData;
import com.example.producer.KafkaProducer;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
//        try {
//            URLConnection connection = new URL(url).openConnection();
//            String contentType = connection.getHeaderField("Content-Type");
//            return contentType != null && contentType.contains("text/html");
//        } catch (IOException e) {
//            return false;
//        }
        return false;
    }


    public void findLinks(String url, int depth, int index) {
        if (index > depth) {
            return;
        }
        String urlValue = redisTemplate.opsForValue().get(url);
        if (Strings.isNotBlank(urlValue)) {
            logger.info("Exist link - " + url);
//            redisTemplate.opsForValue().set(url, "");//TODO - remove
            return;
        }
        try {
            String validUrl = getValidUrl(url);
            if (validUrl == null) {
                return;
            }
//            if (!isHtml(validUrl)) {
//                logger.warn("Not HTML link: " + url);
//                return;
//            }
            Document doc = Jsoup.connect(validUrl).get();
            String html = doc.html();
            redisTemplate.opsForValue().set(validUrl, html, 60, java.util.concurrent.TimeUnit.SECONDS);
            Document htmlDoc = Jsoup.parse(html);
            int sameDomainLinks = 0;
            String host = new URL(validUrl).getHost();

            // Retrieve and print absolute links
            Elements absoluteLinks = htmlDoc.select("a[href]");
            for (Element link : absoluteLinks) {
                String absHref = link.attr("abs:href");
                sameDomainLinks = handleLink(depth, index, host,  absHref);
            }

            // Retrieve and print relative links
            Elements relativeLinks = htmlDoc.select("a[href^=/], a[href^=./], a[href^=../]");
            for (Element link : relativeLinks) {
                String relHref = link.attr("href");
                sameDomainLinks += handleLink(depth, index, host, host + relHref);
            }

            int totalLinks = absoluteLinks.size() + relativeLinks.size();
            double rank = (double) sameDomainLinks / totalLinks;
            logger.info(index + ": URL: " + validUrl + " Depth: " + index + " Ratio: " + rank);
        } catch (Exception e) {
            logger.error(index + ": Error in link - " + url + " Message - " + e.getMessage());
        }
    }

    private int handleLink(int depth, int index,  String host, String href) {
        int sameDomainLinks = 0;
        if (Strings.isNotBlank(href)) {
            kafkaProducer.sendMessage(new LinkData(href, depth, index + 1));
            if (href.contains(host)) {
                sameDomainLinks++;
            }
        }
        return sameDomainLinks;
    }
}
