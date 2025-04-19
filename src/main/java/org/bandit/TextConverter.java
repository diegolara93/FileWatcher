package org.bandit;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

public class TextConverter extends ConverterImpl {
    TextConverter(String inputFile, String outputFile, FileType fileType) {
        super(inputFile, outputFile, fileType);
    }

    @Override
    public void convertToPdf(String inputFile, String outputFile) {
        Document document = new Document();
        int maxRetries = 5;
        int retryDelayMs = 100;

        for(int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                PdfWriter.getInstance(document, new FileOutputStream(outputFile));
                document.open();
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    document.add(new Paragraph(line));
                }
                reader.close();
                System.out.println("File converted to PDF successfully: " + outputFile);
                return;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    System.err.println("Error converting file to PDF after " + maxRetries + " attempts: " + e.getMessage());
                } else {
                    System.out.println("Error converting file to PDF, retrying in " + retryDelayMs + "ms");
                    try {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                e.printStackTrace();
            } finally {
                document.close();
            }
        }
    }
    @Override
    public void convertToTxt(String inputFile, String outputFile) {
        System.out.print("Converting text to text, exiting...");
    }

    @Override
    public void convertToDocx(String inputFile, String outputFile) {

    }
    @Override
    public void convertToHtml(String inputFile, String outputFile) {

    }
    @Override
    public void convertToJSON(String inputFile, String outputFile) {

    }
}
