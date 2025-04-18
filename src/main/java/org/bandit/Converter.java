package org.bandit;

public interface Converter {
    void convertToPdf(String inputFile, String outputFile);

    void convertToTxt(String inputFile, String outputFile);

    void convertToDocx(String inputFile, String outputFile);

    void convertToHtml(String inputFile, String outputFile);

    void convertToJSON(String inputFile, String outputFile);

    void readFileWithRetry(String path);
}
