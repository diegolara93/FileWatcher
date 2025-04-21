package org.bandit;

import java.util.function.Consumer;

public class ConverterImpl implements Converter {
    private String inputFile;
    private String outputFile;
    private FileType fileType;

    ConverterImpl(String inputFile, String outputFile, FileType fileType) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.fileType = fileType;
    }

    public String getInputFile() {
        return inputFile;
    }
    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }
    public String getOutputFile() {
        return outputFile;
    }
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
    public FileType getFileType() {
        return fileType;
    }
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    @Override
    public void convertToPdf(String inputFile, String outputFile) {

    }

    @Override
    public void convertToTxt(String inputFile, String outputFile) {

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

    @Override
    public void readFileWithRetry(String path) {

    }
}
