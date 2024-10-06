package lucassoftwares.leilaointeligente.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import lucassoftwares.leilaointeligente.model.Imovel;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Component
public class OCRPDFReader {

    private Tesseract tesseract;
    private Map<String, StringBuilder> mapMatriculaImovel = new HashMap<>();

    public OCRPDFReader() {
        tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata/"); // Set this to your tessdata directory
        tesseract.setLanguage("por"); // Change to "por" for Portuguese, if needed
        //tesseract.setTessVariable("textord_min_linesize", "2");
    }

    public boolean containsTextInPDF(String pdfUrl, String searchText) {
    	StringBuilder fullText = new StringBuilder();
    	if(mapMatriculaImovel.containsKey(pdfUrl)) {
    		StringBuilder textoPDF = mapMatriculaImovel.get(pdfUrl);
    		fullText = textoPDF;
    	} else {
    		try (InputStream inputStream = new URL(pdfUrl).openStream();
	            PDDocument document = PDDocument.load(inputStream)) {
	            PDFRenderer pdfRenderer = new PDFRenderer(document);

	            for (int page = 0; page < document.getNumberOfPages(); page++) {
	                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // Render at 300 DPI
	                String resultText = tesseract.doOCR(bim);
	                fullText.append(resultText).append("\n");
	            }
	            
	            System.out.println(fullText.toString());

	            mapMatriculaImovel.put(pdfUrl, fullText);
	        } catch (IOException | TesseractException e) {
	            e.printStackTrace();
	            return false;
	        }
    	}
    	
    	return fullText.toString().contains(searchText);
    }
    
    public void imovelInPDF(Imovel imovel) {
        if (imovel == null) {
            System.out.println("Imóvel não instanciado.");
            return;
        }

        StringBuilder fullText = new StringBuilder();

        // Check if the property is already cached
        if (mapMatriculaImovel.containsKey(imovel.getIdImovelSite())) {
            fullText = mapMatriculaImovel.get(imovel.getIdImovelSite());
        } else {
            InputStream inputStream = null;
            PDDocument document = null;

            try {
            	inputStream = getClass().getResourceAsStream("/pdf/matricula/" + imovel.getNumeroImovel().replace("-", "") + ".pdf");
                if (inputStream == null) {
                    System.out.println("PDF não encontrado nos recursos." + imovel.getNumeroImovel().replace("-", ""));
                    if (imovel.getLinkMatricula() != null && imovel.getLinkMatricula().startsWith("http")) {
                        inputStream = new URL(imovel.getLinkMatricula()).openStream();
                    }
                } else {
                	System.out.println(imovel.getNumeroImovel().replace("-", "") + " encontrado nos recursos. Usando cache.");
                }

                // Load the PDF document
                document = PDDocument.load(inputStream);
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // Render at 300 DPI
                    String resultText = tesseract.doOCR(bim);
                    fullText.append(resultText).append("\n");
                }

                //System.out.println(fullText.toString());

                // Cache the extracted text for future use
                mapMatriculaImovel.put(imovel.getIdImovelSite(), fullText);

            } catch (IOException | TesseractException e) {
                e.printStackTrace();
            } finally {
                // Ensure the PDF document is closed
                try {
                    if (document != null) {
                        document.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!fullText.isEmpty()) {
            // Clean up the text
            String textoLimpo = fullText.toString().replace("\\|", "").toLowerCase();

            // Find area values with context (common area, total area, etc.)
            Double areaPrivativa = extractAreaWithKeyword(textoLimpo, "privativ");
            Double areaComum = extractAreaWithKeyword(textoLimpo, "comum");
            Double areaTotal = extractAreaWithKeyword(textoLimpo, "total");

            // Set the values in the Imovel object based on extracted results
            if (areaPrivativa != null) {
                //imovel.setAreaPrivativa(areaPrivativa);
                System.out.println("Área Privativa: " + areaPrivativa);
            }
            if (areaComum != null) {
                //imovel.setAreaUso(areaComum);
                System.out.println("Área Comum: " + areaComum);
            }
            if (areaTotal != null) {
                //imovel.setAreaTotal(areaTotal);
                System.out.println("Área Total: " + areaTotal);
            }
            
            String cpf = extractCPFWithKeyword(textoLimpo, "CPF");
            if (cpf != null) {
                imovel.setCpf(cpf);
                System.out.println("CPF: " + cpf);
            }
        }
    }
    
    private static Double extractAreaWithKeyword(String text, String keyword) {
        String regex = keyword + ".*?(\\d{1,3}(?:[.,]\\d{1,2})?)\\s?(m|m2|m²|mº|m\\?|m\\?\\?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String numericValue = matcher.group(1).replace(",", ".");
            try {
                return Double.parseDouble(numericValue);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing area value: " + numericValue);
            }
        }
        return null;
    }
    
    private static String extractCPFWithKeyword(String text, String keyword) {
        String regex = keyword + ".*?(\\d{1,3}(?:[.,]\\d{1,2})?)\\s?(m|m2|m²|mº|m\\?|m\\?\\?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    public static void main(String[] args) {
        OCRPDFReader reader = new OCRPDFReader();
        Imovel imovel = new Imovel();
        imovel.setLinkMatricula("https://venda-imoveis.caixa.gov.br/editais/matricula/DF/8787704614310.pdf");
        imovel.setNumeroImovel("8787704614310");
        
        reader.imovelInPDF(imovel);
    }
}
