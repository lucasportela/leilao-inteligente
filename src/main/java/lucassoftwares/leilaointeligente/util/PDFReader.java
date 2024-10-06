package lucassoftwares.leilaointeligente.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
public class PDFReader {

    public boolean containsTextInPDF(String pdfUrl, String searchText) {
        try (InputStream inputStream = new URL(pdfUrl).openStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(document);
            return pdfText.contains(searchText);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        PDFReader reader = new PDFReader();
        String pdfUrl = "https://venda-imoveis.caixa.gov.br/editais/matricula/DF/8787708768460.pdf";
        String searchText = "Consolidação de Propriedade";

        boolean found = reader.containsTextInPDF(pdfUrl, searchText);
        System.out.println("Contains '" + searchText + "': " + found);
    }
}