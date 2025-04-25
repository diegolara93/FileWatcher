package org.bandit;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;

/*
    Handles all conversions from .docx -> .pdf, .txt, etc.
 */
public class DocxConverter extends ConverterImpl {
    DocxConverter(String inputFile, String outputFile, FileType fileType) {
        super(inputFile, outputFile, fileType);
    }

    @Override
    public void convertToPdf(String inputFile, String outputFile) {
        try {
            InputStream docxInputStream = new FileInputStream(inputFile);
            XWPFDocument document = new XWPFDocument(docxInputStream);

            PdfOptions options = PdfOptions.create();

            OutputStream pdfOutputStream = new FileOutputStream(outputFile);
            PdfConverter.getInstance().convert(document, pdfOutputStream, options);

            document.close();
            docxInputStream.close();
            pdfOutputStream.close();

            System.out.println("DOCX converted to PDF successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertToTxt(String inputFile, String outputFile) {
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            XWPFDocument document = new XWPFDocument(fis);

            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String text = extractor.getText();

            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(text.getBytes());

            extractor.close();
            fos.close();
            fis.close();

            System.out.println("Conversion completed successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convertToDocx(String inputFile, String outputFile) {
        System.out.print("Cannot convert docx to docx");
    }
}
