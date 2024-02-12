package com.example.webcrawler;

import com.example.model.UrlData;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TsvBatchWriter {
    // also need to call the writeBatch method at the end of the process

    final String tsvFile = "output";
    final String tsvFileExtension = ".tsv";
    final static String tsvFileSeparator = "\t";
    final static String tsvFileNewLine = "\n";
    final int batchSize = 10; // config value

    private List<UrlData> urlDataList = new ArrayList<>();

    public void addData(String url, int depth, double sameDomainRatio) {
        urlDataList.add(new UrlData(url, depth, sameDomainRatio));
        if (urlDataList.size() >= batchSize) {
            writeBatch();
        }
    }


    public void writeBatch() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter( tsvFile + System.currentTimeMillis() + tsvFileExtension, true))) {
            writer.write("URL\tDepth\tSameDomainRatio\n"); // Writing the header

            for (UrlData urlData : urlDataList) {
                String line = urlData.getUrl() + tsvFileSeparator + urlData.getDepth() + tsvFileSeparator + urlData.getRatio() + tsvFileNewLine;
                writer.write(line);
            }
            writer.flush(); // Flush the buffer after every batch
            urlDataList.clear(); // Clear the list after writing the batch
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}