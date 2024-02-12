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
import java.util.Objects;

@Service
public class WebPageReader {

    private final Logger logger = LoggerFactory.getLogger(WebPageReader.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaProducer kafkaProducer;
    private final TsvBatchWriter tsvBatchWriter;

    public static String[] PREFIXES = {"https://", "http://"};

    public WebPageReader(RedisTemplate<String, String> redisTemplate, KafkaProducer kafkaProducer
            , TsvBatchWriter tsvBatchWriter) {
        this.redisTemplate = redisTemplate;
        this.kafkaProducer = kafkaProducer;
        this.tsvBatchWriter = tsvBatchWriter;
    }

    private String getValidUrl(String urlString) {
        String url = getURL(urlString);
        if (Strings.isNotBlank(url)) {
            return url;
        }
        for (String prefix : PREFIXES) {
            url = getURL(prefix + urlString);
            if (Strings.isNotBlank(url)) {
                return url;
            }
        }
        return null;
    }

    private String getURL(String urlString) {
        try {
            URL url = new URL(urlString);
            String urlPath = (url.getPath().endsWith("/")) ? url.getPath().substring(0, url.getPath().length() - 1) : url.getPath();
            return url.getProtocol() + "://" + url.getHost() + urlPath;
        } catch (MalformedURLException e) {
            return null;
        }
    }


    public void findLinks(String url, int depth, int index) {
        try {
            if (index > depth) {
                return;
            }
            String validUrl = getValidUrl(url);
            if (Strings.isBlank(validUrl)) {
                return;
            }
            String urlValue = redisTemplate.opsForValue().get(validUrl);
            if (Strings.isNotBlank(urlValue)) {
                logger.info("Exist link - " + urlValue);
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
            int sameDomainLinks = 0;
            String host = new URL(validUrl).getHost();

            // Retrieve and print absolute links
            Elements absoluteLinks = htmlDoc.select("a[href]");
            for (Element link : absoluteLinks) {
                String absHref = link.attr("abs:href");
                sameDomainLinks += handleLink(depth, index, host, absHref);
            }

            // Retrieve and print relative links
            Elements relativeLinks = htmlDoc.select("a[href^=/], a[href^=./], a[href^=../]");
            for (Element link : relativeLinks) {
                String relHref = link.attr("href");
                sameDomainLinks += handleLink(depth, index, host, host + relHref);
            }

            int totalLinks = absoluteLinks.size() + relativeLinks.size();
            double rank = (double) sameDomainLinks / totalLinks;
            tsvBatchWriter.addData(validUrl, index, rank);
            logger.info(index + ": URL: " + validUrl + " Depth: " + index + " Ratio: " + rank);
        } catch (Exception e) {
            logger.error(index + ": Error in link - " + url, e);
        }
    }

    private int handleLink(int depth, int index, String host, String href) {
        int sameDomainLinks = 0;
        String validUrl = getValidUrl(href);
        if (Strings.isBlank(validUrl)) {
            return 0;
        }

        if (validUrl.contains(host)) {
            sameDomainLinks = 1;
        }

        String urlValue = redisTemplate.opsForValue().get(validUrl);
        if (Strings.isNotBlank(urlValue)) {
            logger.info("Exist link - " + urlValue);
            return sameDomainLinks;
        }
        kafkaProducer.sendMessage(new LinkData(validUrl, depth, index + 1));
        return sameDomainLinks;
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
}
