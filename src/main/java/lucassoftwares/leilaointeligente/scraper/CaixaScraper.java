package lucassoftwares.leilaointeligente.scraper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lucassoftwares.leilaointeligente.model.Cidade;
import lucassoftwares.leilaointeligente.model.Imovel;
import lucassoftwares.leilaointeligente.model.TipoImovel;
import lucassoftwares.leilaointeligente.repository.ImovelRepository;
import lucassoftwares.leilaointeligente.repository.TipoImovelRepository;
import lucassoftwares.leilaointeligente.service.CidadeService;
import lucassoftwares.leilaointeligente.util.OCRPDFReader;

@Component
public class CaixaScraper implements CommandLineRunner {
	
	private boolean isProdu = true;
	
	@Autowired
    private ImovelRepository imovelRepository;
	@Autowired
    private TipoImovelRepository tipoImovelRepository;
	//@Autowired
    //private ModalidadeVendaRepository modalidadeVendaRepository;
	@Autowired
    private CidadeService cidadeService;
	@Autowired
    private OCRPDFReader ocrPDFReader;

    @Override
    public void run(String... args) throws Exception {
        //scrape();
    	//obterInformacoesMatriculaPDF();
    	//verificarItensAtivos();
    }

    public void scrape() {
        String url = "https://venda-imoveis.caixa.gov.br/sistema/carregaPesquisaImoveis.asp";
        String uf = "DF"; // DF - GO
        int codCidade = 1809; // 1809 - Brasília / 2404 - Valparaiso / 2327 - Planaltina

        try {
        	Document document = null;
        	if(isProdu) {
        		document = Jsoup.connect(url)
                        .method(org.jsoup.Connection.Method.POST)
                        .data("hdn_estado", uf) 
                        .data("hdn_cidade", String.valueOf(codCidade)) 
                        .post();
        	} else {
        		String html = "<html>" +
                        "<head></head>" +
                        "<body>" +
                        "<input type=\"hidden\" name=\"hdnImov1\" id=\"hdnImov1\" value=\"08787708768460||00000010170139\"> " +
                        "<input type=\"hidden\" name=\"hdnImov2\" id=\"hdnImov2\" value=\"00000010187462||00000010187463||00000010143174||00000010143176||00000010143177||00000010143175||08555508309533||01444406819443||08240703000220||01444407466099\"> " +
                        "<input type=\"hidden\" name=\"hdnImov3\" id=\"hdnImov3\" value=\"01444415997839||08787700138874||08555535830524||08444409675874||08555526011370||08444413181821||01444416026977||08787701213160||08555523184711||08555528926481\"> " +
                        "<input type=\"hidden\" name=\"hdnImov4\" id=\"hdnImov4\" value=\"08555527701354||01444416234847||08555535770939||01555533218734||08555537664095||01444408639108\"> " +
                        "<input type=\"hidden\" name=\"hdnFiltro\" id=\"hdnFiltro\" value=\"DF||1809||||||4||0,01||999999999,99||0||||0||||||||||||0||999999\"> " +
                        "<input type=\"hidden\" name=\"hdnQtdPag\" id=\"hdnQtdPag\" value=\"4\">" +
                        "<input type=\"hidden\" name=\"hdnPagNum\" id=\"hdnPagNum\" value=\"1\">" +
                        "<input type=\"hidden\" name=\"hdnQtdRegistros\" id=\"hdnQtdRegistros\" value=\"36\">" +
                        "<span class=\"legend-desc lighter milli\">Foram encontrados 36 imóveis.</span>" +
                        "</body>" +
                        "</html>";

                document = Jsoup.parse(html);
        	}
        	
            List<String> imoveis = new ArrayList<>();
            
            Elements imovElements = document.select("input[id^=hdnImov]");

            for (Element imovElement : imovElements) {
                String value = imovElement.attr("value");

                // Split the value by '||' and add each part to the 'imoveis' list
                String[] imoveisArray = value.split("\\|\\|");
                int contador = 0;
                for (String imovel : imoveisArray) {
                    imoveis.add(imovel);
                    if(!isProdu) {
                    	if(contador == 1) {
                            break;
                        }
                        contador++;
                    }
                }
                
                if(!isProdu) {
                	// Apenas 1  como teste
                    //break;
                }
            }

            System.out.println("Lista de imóveis:");
            
            for (String idImovel : imoveis) {
                System.out.println(idImovel);
                
                Cidade cidade = cidadeService.getCidadeByCodigo(codCidade);
                
                Imovel imovel = null;
                imovel = imovelRepository.findByIdImovelSite(idImovel);
                if (imovel == null) {
                	imovel = new Imovel();
                	imovel.setDataCadastro(Timestamp.from(Instant.now()));
                } else {
                	imovel.setDataAtualizacao(Timestamp.from(Instant.now()));
                }
                
                imovel.setIdImovelSite(idImovel);
                imovel.setIdCidade(cidade.getId());
                
                imovel.setAtivo(true);

                getImovelDetails(imovel);
            }
            
            System.out.println("Total de imóveis encontrados: " + imoveis.size());
            
            // Desativar os que não aparecem mais na lista
            List<Imovel> listaImoveisAtivos = imovelRepository.findAllByAtivo(true);
            listaImoveisAtivos.forEach(imovel -> {
                boolean isAtivo = imoveis.stream()
                    .anyMatch(idImovel -> imovel.getIdImovelSite().equals(idImovel));

                imovel.setAtivo(isAtivo);
                imovelRepository.save(imovel);
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getImovelDetails(Imovel imovel) throws IOException {
        String detailUrl = "https://venda-imoveis.caixa.gov.br/sistema/detalhe-imovel.asp";
        
        Document detailDocument = null;
    	if(isProdu) {
	        detailDocument = Jsoup.connect(detailUrl)
	                .method(org.jsoup.Connection.Method.POST)
	                .data("hdnimovel", imovel.getIdImovelSite())
	                .post();
    	} else {
    		String html = htmlDetailTeste();
            detailDocument = Jsoup.parse(html);
    	}
        
        System.out.println("Detalhes do Imóvel ID: " + imovel.getIdImovelSite());

        Element nameElement = detailDocument.selectFirst("h5");
        if (nameElement != null) {
        	if(nameElement.text().contains("O imóvel que você procura")) {
        		System.out.println("----------");
        		System.out.println("DESATIVADO");
        		System.out.println("----------");
                imovel.setAtivo(false);
                
                imovelRepository.save(imovel);
                return;
        	} else {
        		System.out.println("Nome: " + nameElement.text());
                imovel.setNome(nameElement.text());
        	}
            
        }

        Element valueElement = detailDocument.selectFirst("p[style=font-size:14pt]");
        if (valueElement != null) {
            System.out.println("Informações: " + valueElement.text());
            parseValoresImovel(imovel, valueElement.text());
        }

        Elements detailItems = detailDocument.select(".control-item.control-span-6_12 p span");
        for (Element detailItem : detailItems) {
            System.out.println(detailItem.text());
            if(detailItem.text().contains("Tipo de imóvel")) {
            	String tipoImovel = detailItem.text().replace("Tipo de imóvel: ", "");
        		TipoImovel regTipoImovel = tipoImovelRepository.findByDescricao(tipoImovel);
                imovel.setIdTipoImovel(regTipoImovel.getId());
            } else if(detailItem.text().contains("Quartos")) {
            	int quartos = Integer.parseInt(detailItem.text().replace("Quartos: ", ""));
                imovel.setQuartos(quartos);
            } else if(detailItem.text().contains("Garagem")) {
            	int vagas = Integer.parseInt(detailItem.text().replace("Garagem: ", ""));
                imovel.setVagas(vagas);
            } else if(detailItem.text().contains("Número do imóvel")) {
         		String numeroImovel = detailItem.text().replace("Número do imóvel: ", "");
                imovel.setNumeroImovel(numeroImovel.replace("-", ""));
            } else if(detailItem.text().contains("Matrícula(s)")) {
                imovel.setMatricula(detailItem.text().replace("Matrícula(s): ", ""));
            } else if(detailItem.text().contains("Comarca")) {	
                imovel.setComarca(detailItem.text().replace("Comarca: ", ""));
        	} else if(detailItem.text().contains("Ofício")) {
                imovel.setOficio(detailItem.text().replace("Ofício: ", ""));
        	} else if(detailItem.text().contains("Inscrição imobiliária")) {
                imovel.setInscricaoImobiliaria(detailItem.text().replace("Inscrição imobiliária: ", ""));
        	} else if(detailItem.text().contains("Averbação dos ")) {
                imovel.setAverbacao(detailItem.text().replace("Averbação dos leilões negativos: ", ""));
        	} else if(detailItem.text().contains("Área privativa")) {
        		String areaPrivativaText = detailItem.text()
                        .replace("Área privativa = ", "")
                        .replace("m2", "")
                        .trim();

				areaPrivativaText = areaPrivativaText.replace(".", "").replace(",", ".");
				
				double areaPrivativa = Double.parseDouble(areaPrivativaText);
				imovel.setAreaPrivativa(areaPrivativa);
            } else if(detailItem.text().contains("Área total ")) {
        		String areaTotalText = detailItem.text()
                        .replace("Área total = ", "")
                        .replace("m2", "")
                        .trim();

        		areaTotalText = areaTotalText.replace(".", "").replace(",", ".");
				
				double areaTotal = Double.parseDouble(areaTotalText);
				imovel.setAreaTotal(areaTotal);
            } else if(detailItem.text().contains("Área do terreno ")) {
        		String areaTotalText = detailItem.text()
                        .replace("Área do terreno = ", "")
                        .replace("m2", "")
                        .trim();

        		areaTotalText = areaTotalText.replace(".", "").replace(",", ".");
				
				double areaTotal = Double.parseDouble(areaTotalText);
				imovel.setAreaTotal(areaTotal);
            }
        }

        Elements relatedBoxItems = detailDocument.select(".related-box span");
        for (Element relatedBoxItem : relatedBoxItems) {
        	System.out.println(relatedBoxItem.text());
        	if(relatedBoxItem.text().contains("Edital")) {
        		String edital = relatedBoxItem.text().replace("Edital: ", "");
        		if(edital != null) {
        			imovel.setEdital(edital);
        			if(imovel.getEdital().contains("Edital Único")) {
                    	imovel.setIdModalidadeVenda(4);
                    } else if(imovel.getEdital().contains("Licitação Aberta")) {
                    	imovel.setIdModalidadeVenda(5);
                    } else {
                    	imovel.setIdModalidadeVenda(7);
                    }
        		} else {
        			imovel.setIdModalidadeVenda(7);
        		}
        	} else if(relatedBoxItem.text().contains("Número do item")) {
        		imovel.setNumeroItem(Integer.parseInt(relatedBoxItem.text().replace("Número do item: ", "")));
        	} else if(relatedBoxItem.text().contains("Leiloeiro")) {
        		imovel.setLeiloeiro(relatedBoxItem.text().replace("Leiloeiro(a): ", ""));
        	} else if(relatedBoxItem.text().contains("Data da Licitação")) {
        		setRelatedInfo(imovel, relatedBoxItem.text());
        	} else if(relatedBoxItem.text().contains("Data do 1º Leilão")) {
        		setRelatedInfo(imovel, relatedBoxItem.text());
        	} else if(relatedBoxItem.text().contains("Data do 2º Leilão")) {
        		setRelatedInfo(imovel, relatedBoxItem.text());
        	} else if(relatedBoxItem.text().contains("Tempo restante ")) {
        		imovel.setIdModalidadeVenda(7);
        		imovel.setEdital(null);
        		imovel.setLinkEdital(null);
        		imovel.setLinkLeilao(null);
        		imovel.setDataLeilao1(null);
        		imovel.setDataLeilao2(null);
        		imovel.setNumeroItem(0);
        	} else {
        		
        	}
        }
        
        // Venda online
        Element relatedBoxContador = detailDocument.selectFirst(".related-box #divContador");
        if (relatedBoxContador != null) {
        	imovel.setIdModalidadeVenda(7);
    		imovel.setEdital(null);
    		imovel.setLinkEdital(null);
    		imovel.setLinkLeilao(null);
    		imovel.setDataLeilao1(null);
    		imovel.setDataLeilao2(null);
    		imovel.setNumeroItem(0);
        }
        
        Element relatedBox = detailDocument.selectFirst(".related-box");
        if (relatedBox != null) {
            getLinksJusBrasil(imovel, relatedBox.text());
        }

        Element addressElement = detailDocument.selectFirst("p strong:contains(Endereço)");
        if (addressElement != null) {
            String address = addressElement.parent().text().replace("Endereço: ", "");
            System.out.println("Endereço: " + address);
            imovel.setEndereco(address);

            String googleMapsLink = generateGoogleMapsLink(imovel, address);
            System.out.println("Google Maps Link: " + googleMapsLink);
        }

        Element descriptionElement = detailDocument.selectFirst("p strong:contains(Descrição)");
        if (descriptionElement != null) {
            System.out.println("Descrição: " + descriptionElement.parent().text());
            imovel.setDescricao(descriptionElement.parent().text());
            
            if(imovel.getDescricao().contains("Codhab")) {
            	imovel.setPrecisaAutorizacaoCodhab(true);
            }
        }

        // Informações de permissões de financiamento/FGTS
        Elements infoIcons = detailDocument.select("i.fa.fa-info-circle");
        StringBuilder observacoes = new StringBuilder();
        
        imovel.setPermiteConsorcio(false);
        imovel.setPermiteFgts(false);
        imovel.setPermiteFinanciamento(false);
        imovel.setPermiteParcelamento(false);
        
        for (Element icon : infoIcons) {
            String infoText = icon.nextSibling().toString().trim().replace("&nbsp;", "");
            System.out.println(infoText);
            observacoes.append(infoText + "<br>\n");
            
            if(infoText.contains("NÃO aceita utilização de FGTS")) {
            	imovel.setPermiteFgts(false);
            } else if(infoText.contains("NÃO aceita financiamento")) {
            	imovel.setPermiteFinanciamento(false);
            } else if(infoText.contains("NÃO aceita parcelamento")) {
            	imovel.setPermiteParcelamento(false);
            } else if(infoText.contains("NÃO aceita consórcio")) {
            	imovel.setPermiteConsorcio(false);
            } else if(infoText.contains("Permite financiamento")) {
            	imovel.setPermiteFinanciamento(true);
            } else if(infoText.contains("Permite ") && infoText.contains("FGTS")) {
            	imovel.setPermiteFgts(true);
            }
        }
        
        imovel.setObservacoes(observacoes.toString());

        getLinks(imovel, detailDocument);
        System.out.println("=================================");
        
        imovel.toString();
        
        imovelRepository.save(imovel);
    }
    
    private void setRelatedInfo(Imovel imovel, String relatedInfo) {
        Pattern dataLeilao1Pattern = Pattern.compile("Data do 1º Leilão - ([^ ]+) - (\\d+h\\d+)");
        Pattern dataLeilao2Pattern = Pattern.compile("Data do 2º Leilão - ([^ ]+) - (\\d+h\\d+)");
        Pattern dataLicitacaoPattern = Pattern.compile("Data da Licitação Aberta - ([^ ]+) - (\\d+h\\d+)");

        Matcher dataLeilao1Matcher = dataLeilao1Pattern.matcher(relatedInfo);
        if (dataLeilao1Matcher.find()) {
        	String originalDateString = dataLeilao1Matcher.group(1) + " " + dataLeilao1Matcher.group(2).replace("h", ":");

        	SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        	originalFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        	
        	SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	targetFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        	try {
        	    Date date = originalFormat.parse(originalDateString);
        	    String formattedDateString = targetFormat.format(date);
        	    imovel.setDataLeilao1(Timestamp.valueOf(formattedDateString));
        	} catch (ParseException e) {
        	    e.printStackTrace();
        	}
        }

        Matcher dataLeilao2Matcher = dataLeilao2Pattern.matcher(relatedInfo);
        if (dataLeilao2Matcher.find()) {
        	String originalDateString = dataLeilao2Matcher.group(1) + " " + dataLeilao2Matcher.group(2).replace("h", ":");

        	SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        	originalFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        	
        	SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	targetFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        	try {
        	    Date date = originalFormat.parse(originalDateString);
        	    String formattedDateString = targetFormat.format(date);
        	    imovel.setDataLeilao2(Timestamp.valueOf(formattedDateString));
        	} catch (ParseException e) {
        	    e.printStackTrace();
        	}
        }
        
        Matcher dataLicitacaoMatcher = dataLicitacaoPattern.matcher(relatedInfo);
        if (dataLicitacaoMatcher.find()) {
        	String originalDateString = dataLicitacaoMatcher.group(1) + " " + dataLicitacaoMatcher.group(2).replace("h", ":");

        	SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        	originalFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        	
        	SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	targetFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        	try {
        	    Date date = originalFormat.parse(originalDateString);
        	    String formattedDateString = targetFormat.format(date);
        	    imovel.setDataLeilao1(Timestamp.valueOf(formattedDateString));
        	} catch (ParseException e) {
        	    e.printStackTrace();
        	}
        }
    }
    
    private void parseValoresImovel(Imovel imovel, String imovelInfo) {
        String regexValorAvaliacao = "Valor de avaliação: R\\$ ([\\d.,]+)";
        String regexValorMinLeilao1 = "Valor mínimo de venda(?: 1º Leilão)?: R\\$ ([\\d.,]+)";
        String regexValorMinLeilao2 = "Valor mínimo de venda 2º Leilão: R\\$ ([\\d.,]+)";

        double valorAvaliacao = parseMonetaryValue(imovelInfo, regexValorAvaliacao);
        double valorMinLeilao1 = parseMonetaryValue(imovelInfo, regexValorMinLeilao1);
        double valorMinLeilao2 = parseMonetaryValue(imovelInfo, regexValorMinLeilao2);

        if (valorMinLeilao1 == 0 && valorMinLeilao2 == 0) {
            valorMinLeilao1 = parseMonetaryValue(imovelInfo, "Valor mínimo de venda: R\\$ ([\\d.,]+)");
        }

        imovel.setValorAvaliacao(valorAvaliacao);
        imovel.setValorMinLeilao1(valorMinLeilao1);
        imovel.setValorMinLeilao2(valorMinLeilao2);
    }

    // Helper method to parse a value using a regex
    private double parseMonetaryValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String value = matcher.group(1).replace(".", "").replace(",", "."); // Convert to standard number format
            return Double.parseDouble(value);
        }
        return 0;
    }

    private String generateGoogleMapsLink(Imovel imovel, String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;
            imovel.setLinkMaps(url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to create Google Maps link";
        }
    }

    private void getLinks(Imovel imovel, Document document) {
        Elements links = document.select("a[onclick*='editais']");
        Elements linkLeilao = document.select("button[onclick*='SiteLeiloeiro']");

        for (Element link : links) {
            String text = link.attr("onclick").intern();
            String url = "https://venda-imoveis.caixa.gov.br" + text.substring(text.indexOf("'") + 1, text.lastIndexOf("'"));
            if (text.contains("matricula")) {
                System.out.println("Link Matrícula: " + url);
                imovel.setLinkMatricula(url);
            } else {
                System.out.println("Link Edital: " + url);
                imovel.setLinkEdital(url);
            }
        }

        for (Element link : linkLeilao) {
            String text = link.attr("onclick").intern();
            String url = text.substring(text.indexOf("\"") + 1, text.lastIndexOf("\""));
            System.out.println("Link Leilão: " + url);
            imovel.setLinkLeilao(url);
        }
    }

    private void getLinksJusBrasil(Imovel imovel, String informacoes) {
        if (informacoes.contains("judicial") && imovel.getLinkJusbrasil() != null) {
        	String url = "https://www.jusbrasil.com.br/busca?q=" + informacoes.substring(informacoes.indexOf("judicial:") + 10);
            System.out.println("Processo: " + url);
            imovel.setLinkJusbrasil(url);
        }
    }
    
    private String htmlDetailTeste() {
    	return "<html lang=\"en-US\" class=\"js flexbox flexboxlegacy no-touch geolocation rgba hsla multiplebgs backgroundsize borderimage borderradius boxshadow textshadow opacity cssanimations csscolumns cssgradients cssreflections csstransforms csstransforms3d csstransitions fontface generatedcontent svg inlinesvg smil svgclippaths cssvwunit desktop\" style=\"\"><head>\r\n"
    			+ "	<meta charset=\"utf-8\">\r\n"
    			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\r\n"
    			+ "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\r\n"
    			+ "	\r\n"
    			+ "	\r\n"
    			+ "	<!-- Google Tag Manager -->\r\n"
    			+ "	<script type=\"text/javascript\" async=\"\" src=\"https://www.google-analytics.com/analytics.js\"></script><script type=\"text/javascript\" async=\"\" src=\"https://www.googletagmanager.com/gtag/js?id=G-PD5EBJFQ7X&amp;l=dataLayer&amp;cx=c\"></script><script async=\"\" src=\"https://www.googletagmanager.com/gtm.js?id=GTM-NDBHSL\"></script><script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':\r\n"
    			+ "	new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],\r\n"
    			+ "	j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=\r\n"
    			+ "	'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);\r\n"
    			+ "	})(window,document,'script','dataLayer','GTM-NDBHSL');</script>\r\n"
    			+ "	<!-- End Google Tag Manager -->	\r\n"
    			+ "	\r\n"
    			+ "	\r\n"
    			+ "    <link rel=\"shortcut icon\" href=\"favicon.ico\">\r\n"
    			+ "    <link rel=\"stylesheet\" href=\"assets/css/normalize.css\" media=\"screen\">\r\n"
    			+ "    <link rel=\"stylesheet\" href=\"assets/css/base.css?\" media=\"screen\">\r\n"
    			+ "    <!--link rel=\"stylesheet\" href=\"assets/css/icons.css\" media=\"screen\"/-->\r\n"
    			+ "    <link rel=\"stylesheet\" href=\"assets/css/wider.css\" media=\"screen and (min-width: 500px)\">\r\n"
    			+ "    <link rel=\"stylesheet\" href=\"assets/css/form-flow.css\" media=\"screen\">\r\n"
    			+ "    <link rel=\"stylesheet\" href=\"assets/css/jquery-ui-1.8.11.custom.css?v=1.0\">	\r\n"
    			+ "	<link rel=\"stylesheet\" href=\"assets/css/imoveisavenda.css\" media=\"screen\">\r\n"
    			+ "    <link rel=\"stylesheet\" href=\"assets/css/products.css\" media=\"screen\">\r\n"
    			+ "	<link rel=\"stylesheet\" href=\"assets/css/global.css\" media=\"screen\">\r\n"
    			+ "	<link rel=\"stylesheet\" href=\"assets/css/caixa.css\" media=\"screen\">\r\n"
    			+ "	<link rel=\"stylesheet\" href=\"assets/css/icon-aewsome.css\" media=\"screen\">\r\n"
    			+ "	\r\n"
    			+ "    <title>Caixa - Imóveis à venda</title>\r\n"
    			+ "	\r\n"
    			+ "	<script src=\"assets/js/jquery-1.10.2.min.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/jquery-ui.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/global.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/jquery.mask.min.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/jquery.maskMoney.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/modernizr.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/products.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/simovUtil.js\"></script>\r\n"
    			+ "	<script src=\"assets/js/simov_navegacao.js?v=1.0\"></script>\r\n"
    			+ "	<script src=\"assets/js/form-flow.js\"></script>\r\n"
    			+ "	\r\n"
    			+ "    <style type=\"text/css\">\r\n"
    			+ "      .thumbnails img {\r\n"
    			+ "        height: 80px;\r\n"
    			+ "        border: 1px solid #eee;\r\n"
    			+ "        padding: 1px;\r\n"
    			+ "        margin: 0 10px 10px 0;\r\n"
    			+ "      }\r\n"
    			+ "\r\n"
    			+ "      .thumbnails img:hover {\r\n"
    			+ "        border: 1px solid #ff7200;\r\n"
    			+ "        cursor:pointer;\r\n"
    			+ "      }\r\n"
    			+ "\r\n"
    			+ "      .preview img {\r\n"
    			+ "        border: 1px solid #eee;\r\n"
    			+ "        padding: 1px;\r\n"
    			+ "        max-width: 95%;\r\n"
    			+ "      }\r\n"
    			+ "    </style>\r\n"
    			+ "	\r\n"
    			+ "	<script>\r\n"
    			+ "	\r\n"
    			+ "		jQuery(document).ready(\r\n"
    			+ "			function() {\r\n"
    			+ "				jQuery('.foto').click(function(e) {	\r\n"
    			+ "					e.preventDefault();\r\n"
    			+ "				});\r\n"
    			+ "				//jQuery('#minhalista').click(function(e) {	\r\n"
    			+ "				//	e.preventDefault();\r\n"
    			+ "				//});\r\n"
    			+ "				\r\n"
    			+ "				jQuery('#btn_buscarimoveis').click(function(e) {\r\n"
    			+ "					$('#frm_detalhe').attr('action','busca-imovel.asp').trigger('submit');\r\n"
    			+ "				});\r\n"
    			+ "				\r\n"
    			+ "				jQuery('#btn_disputas').click(function(e) {\r\n"
    			+ "					$('#frm_detalhe').attr('action','venda-online/disputas.asp').trigger('submit');\r\n"
    			+ "				});	\r\n"
    			+ "				\r\n"
    			+ "				jQuery('#btn_resultados').click(function(e) {\r\n"
    			+ "					$('#frm_detalhe').attr('action','venda-online/resultados.asp').trigger('submit');\r\n"
    			+ "				});\r\n"
    			+ "				jQuery('#btn_favoritos').click(function(e) {\r\n"
    			+ "					$('#frm_detalhe').attr('action','venda-online/favoritos.asp?acessodireto=1').trigger('submit');\r\n"
    			+ "				});	\r\n"
    			+ "				\r\n"
    			+ "				jQuery('#btn_dados').click(function(e) {\r\n"
    			+ "					$('#frm_detalhe').attr('action','venda-online/dados-proponente.asp?hdnimovel=8787700138874').trigger('submit');\r\n"
    			+ "				});\r\n"
    			+ "				\r\n"
    			+ "		});	\r\n"
    			+ "	\r\n"
    			+ "	</script>\r\n"
    			+ "	\r\n"
    			+ "</head>\r\n"
    			+ "\r\n"
    			+ "<body class=\"produto imoveis-a-venda desktop\" cz-shortcut-listen=\"true\" style=\"\">\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "<!-- Google Tag Manager (noscript) -->\r\n"
    			+ "<noscript><iframe src=\"https://www.googletagmanager.com/ns.html?id=GTM-NDBHSL\"\r\n"
    			+ "height=\"0\" width=\"0\" style=\"display:none;visibility:hidden\"></iframe></noscript>\r\n"
    			+ "<!-- End Google Tag Manager (noscript) -->\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "<div class=\"wrapper\">\r\n"
    			+ "    \r\n"
    			+ "	<div class=\"section noprint\">                                                                                                                                        \r\n"
    			+ "            <header id=\"header-home\" class=\"main-header\"><!-- Inicio header menu -->                                                                                 \r\n"
    			+ "                <div id=\"main-logo\" tabindex=\"2\" class=\"noindex\">                                                                                                                                 \r\n"
    			+ "                    <a href=\"http://www.caixa.gov.br/\" title=\"\" class=\"ir\"></a>                                                                                                             \r\n"
    			+ "                </div>                                                                                                                                               \r\n"
    			+ "                <div class=\"header-content noindex\">                                                                                                                         \r\n"
    			+ "                    <nav class=\"hotlinks clearfix\"><!-- hotlinks -->\r\n"
    			+ "                        <ul class=\"helper\">\r\n"
    			+ "                            <li><a href=\"http://www.caixa.gov.br/atendimento/canais-digitais/banking-caixa/primeiro-acesso/usuario-senha/Paginas/default.aspx?pk_campaign=Acolhimento&amp;pk_kwd=02_027_Passo-1_AJ_acessar2\" tabindex=\"13\" title=\"Ajuda para Acessar\" class=\"highlight\">Ajuda para Acessar ›</a></li>\r\n"
    			+ "                        </ul>\r\n"
    			+ "                        <ul class=\"languages\">\r\n"
    			+ "                            <li><a href=\"http://www20.caixa.gov.br/Paginas/Default.aspx\" tabindex=\"10\" title=\"Imprensa\">Imprensa</a> /</li>\r\n"
    			+ "                            <li><a href=\"http://www.caixa.gov.br/site/english\" tabindex=\"11\" title=\"EN\">EN</a> /</li>\r\n"
    			+ "                            <li class=\"rybena\"><a class=\"ico_libras_2011\" tabindex=\"12\" title=\"Imprensa\">Imprensa</a></li>\r\n"
    			+ "                        </ul>\r\n"
    			+ "                        <ul>\r\n"
    			+ "                            <li><a href=\"http://www.caixa.gov.br/seguranca/Paginas/default.aspx\" tabindex=\"7\" title=\"Segurança\">Segurança</a></li>\r\n"
    			+ "                            <li><a href=\"http://www.caixa.gov.br/site/paginas/downloads.aspx\" tabindex=\"8\" title=\"Downloads\">Downloads</a></li>\r\n"
    			+ "                            <li><a href=\"http://www.caixa.gov.br/sobre-a-caixa\" tabindex=\"9\" title=\"Sobre a Caixa\">Sobre a Caixa</a></li>\r\n"
    			+ "                        </ul>\r\n"
    			+ "                    </nav><!-- hotlinks -->\r\n"
    			+ "                    <nav id=\"main-nav\" class=\"clearfix\"><!-- mainnav -->                                                                                             \r\n"
    			+ "                        <div class=\"main-menu noindex\"><!-- main-menu -->\r\n"
    			+ "                            <a name=\"menuPrincipal\" id=\"menuPrincipal\" class=\"hide\" accesskey=\"2\">Menu Principal</a>\r\n"
    			+ "<ul id=\"menu-principal\"><!-- Menu principal -->\r\n"
    			+ "<li class=\"has-submenu\">\r\n"
    			+ "<a href=\"#\" tabindex=\"3\" title=\"Produtos\">Produtos</a>\r\n"
    			+ "<div class=\"submenu submenu-produtos clearfix\"><!-- Sub Menu -->\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Para Você </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/contas\" title=\"http://www.caixa.gov.br/voce/contas\">Contas</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/habitacao\" title=\"http://www.caixa.gov.br/voce/habitacao\">Habitação</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/poupanca-e-investimentos\" title=\"http://www.caixa.gov.br/voce/poupanca-e-investimentos\">Poupança e Investimentos</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/cartoes\" title=\"http://www.caixa.gov.br/voce/cartoes\">Cartões</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/credito-financiamento\" title=\"http://www.caixa.gov.br/voce/credito-financiamento\">Empréstimo e Financiamento</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/Seguros\" title=\"http://www.caixa.gov.br/voce/Seguros\">Seguros</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/previdencia\" title=\"http://www.caixa.gov.br/voce/previdencia\">Previdência Privada</a></li>\r\n"
    			+ "<li><a href=\"http://loterias.caixa.gov.br/\" title=\"http://loterias.caixa.gov.br/\">Loterias</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/promocoes\" title=\"http://www.caixa.gov.br/voce/promocoes\">Promoções</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/credito-financiamento/renegociacao-dividas/Paginas/default.aspx\" title=\"http://www.caixa.gov.br/voce/credito-financiamento/renegociacao-dividas/Paginas/default.aspx\">Negocie sua dívida</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/voce\" title=\"Todos os produtos para você\">Todos os produtos para você ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Empresa </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/contas\" title=\"http://www.caixa.gov.br/empresa/contas\">Contas</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/fundos-investimento/empresa/poupanca-e-investimentos\" title=\"http://www.caixa.gov.br/fundos-investimento/empresa/poupanca-e-investimentos\">Investimentos</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/cartoes\" title=\"http://www.caixa.gov.br/empresa/cartoes\">Cartões</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/credito-financiamento\" title=\"http://www.caixa.gov.br/empresa/credito-financiamento\">Crédito e Financiamento</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/credito-financiamento/imoveis\" title=\"http://www.caixa.gov.br/empresa/credito-financiamento/imoveis\">Imóveis</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/poder-publico/apoio-poder-publico/servicos-caixa/servicos-judiciarios\" title=\"http://www.caixa.gov.br/poder-publico/apoio-poder-publico/servicos-caixa/servicos-judiciarios\">Serviços para o Judiciário</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/credito-financiamento/credito-rural\" title=\"http://www.caixa.gov.br/empresa/credito-financiamento/credito-rural\">Crédito Rural</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/conectividade-social\" title=\"http://www.caixa.gov.br/empresa/conectividade-social\">Conectividade Social</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/empresa/fgts-empresas\" title=\"http://www.caixa.gov.br/empresa/fgts-empresas\">FGTS</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/empresa\" title=\"Todos os produtos para empresas\">Todos os produtos para empresas ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "</div>\r\n"
    			+ "</li>\r\n"
    			+ "<li class=\"has-submenu\">\r\n"
    			+ "<a href=\"#\" tabindex=\"4\" title=\"Benefícios e Programas\">Benefícios e Programas</a>\r\n"
    			+ "<div class=\"submenu submenu-beneficios clearfix\"><!-- Sub Menu -->\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Benefícios do trabalhador </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/beneficios-trabalhador/fgts\" title=\"http://www.caixa.gov.br/beneficios-trabalhador/fgts\">FGTS</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/beneficios-trabalhador/pis\" title=\"http://www.caixa.gov.br/beneficios-trabalhador/pis\">PIS</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/beneficios-trabalhador/inss\" title=\"http://www.caixa.gov.br/beneficios-trabalhador/inss\">INSS</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/beneficios-trabalhador/seguro-desemprego\" title=\"http://www.caixa.gov.br/beneficios-trabalhador/seguro-desemprego\">Seguro-Desemprego</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/beneficios-trabalhador\" title=\"Tudo sobre os benefícios do trabalhador\">Tudo sobre os benefícios do trabalhador ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Programas sociais </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/voce/habitacao/minha-casa-minha-vida\" title=\"http://www.caixa.gov.br/voce/habitacao/minha-casa-minha-vida\">Minha Casa Minha Vida</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/programas-sociais/minha-casa-melhor\" title=\"http://www.caixa.gov.br/programas-sociais/minha-casa-melhor\">Minha Casa Melhor</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/programas-sociais/bolsa-familia\" title=\"http://www.caixa.gov.br/programas-sociais/bolsa-familia\">Bolsa Família</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/programas-sociais/fies\" title=\"http://www.caixa.gov.br/programas-sociais/fies\">FIES</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/programas-sociais\" title=\"Todos os programas\">Todos os programas ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Cadastros do governo </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/cadastros/cartao-cidadao\" title=\"http://www.caixa.gov.br/cadastros/cartao-cidadao\">Cartão do Cidadão</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/cadastros/cpf\" title=\"http://www.caixa.gov.br/cadastros/cpf\">CPF</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/cadastros/cadastro-unico\" title=\"http://www.caixa.gov.br/cadastros/cadastro-unico\">Cadastro Único</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/cadastros/nis\" title=\"http://www.caixa.gov.br/cadastros/nis\">Cadastro NIS Empresa</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/cadastros\" title=\"Todos os cadastros\">Todos os cadastros ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "</div>\r\n"
    			+ "</li>\r\n"
    			+ "<li class=\"has-submenu\">\r\n"
    			+ "<a href=\"#\" tabindex=\"5\" title=\"Atendimento\">Atendimento</a>\r\n"
    			+ "<div class=\"submenu submenu-atendimento clearfix\"><!-- Sub Menu -->\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Canais </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx\" title=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx\">Atendimento para você e para empresa</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#telefones-caixa\" title=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#telefones-caixa\">Telefones</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#encontre\" title=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#encontre\">Endereços</a></li>\r\n"
    			+ "<li><a href=\"http://fale-conosco.caixa.gov.br/\" title=\"http://fale-conosco.caixa.gov.br/\">Fale Conosco</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Canais Digitais </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/atendimento/aplicativos\" title=\"http://www.caixa.gov.br/atendimento/aplicativos\">Aplicativos</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/site/Paginas/downloads.aspx\" title=\"http://www.caixa.gov.br/site/Paginas/downloads.aspx\">Downloads</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/atendimento/2-via-boleto\" title=\"http://www.caixa.gov.br/atendimento/2-via-boleto\">2ª via de boletos</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/atendimento/canais-digitais/Paginas/default.aspx?pk_campaign=canaisset15&amp;pk_kwd=link_menu\" title=\"Todos os serviços e canais digitais\">Todos os serviços e canais digitais ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "   <div class=\"submenu-column\">\r\n"
    			+ "      <p class=\"submenu-title\">Institucional </p>\r\n"
    			+ "<ul>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/sustentabilidade/responsabilidade-social/agencia-barco\" title=\"http://www.caixa.gov.br/sustentabilidade/responsabilidade-social/agencia-barco\">Agência-Barco</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/sustentabilidade\" title=\"http://www.caixa.gov.br/sustentabilidade\">Sustentabilidade</a></li>\r\n"
    			+ "<li><a href=\"http://www.caixa.gov.br/sobre-a-caixa/trabalhe-na-caixa\" title=\"http://www.caixa.gov.br/sobre-a-caixa/trabalhe-na-caixa\">Trabalhe na Caixa</a></li>\r\n"
    			+ "<li><a href=\"http://www20.caixa.gov.br/Paginas/Default.aspx\" title=\"http://www20.caixa.gov.br/Paginas/Default.aspx\">Imprensa</a></li>\r\n"
    			+ "<li class=\"see-more\"><a href=\"http://www.caixa.gov.br/sobre-a-caixa\" title=\"Tudo sobre a Caixa\">Tudo sobre a Caixa ›</a></li>\r\n"
    			+ "</ul>\r\n"
    			+ "</div>\r\n"
    			+ "</div>\r\n"
    			+ "</li>\r\n"
    			+ "                                <li>\r\n"
    			+ "                                    <a href=\"http://www.caixa.gov.br/poder-publico/\" tabindex=\"6\" title=\"Poder Público\" class=\"soft\">Poder Público</a>\r\n"
    			+ "                                    <nav class=\"menu-hotlinks clearfix hidden-desktop\"><!-- hotlinks  apenas mobile-->\r\n"
    			+ "                                        <ul>\r\n"
    			+ "                                            <li><a href=\"http://www.caixa.gov.br/seguranca/Paginas/default.aspx\" title=\"Segurança\">Segurança</a></li>\r\n"
    			+ "                                            <li><a href=\"http://www.caixa.gov.br/site/paginas/downloads.aspx\" title=\"Downloads\">Downloads</a></li>\r\n"
    			+ "                                            <li><a href=\"http://www.caixa.gov.br/sobre-a-caixa\" title=\"Sobre a Caixa\">Sobre a Caixa</a></li>\r\n"
    			+ "                                        </ul>                                                                                                                        \r\n"
    			+ "                                        <ul class=\"languages\">                                                                                                       \r\n"
    			+ "                                            <li><a href=\"http://www20.caixa.gov.br/Paginas/Default.aspx\" title=\"Imprensa\">Imprensa</a> /</li>\r\n"
    			+ "                                            <li><a href=\"http://www.caixa.gov.br/site/english\" title=\"EN\">EN</a> /</li>\r\n"
    			+ "                                            <li><a href=\"http://www.caixa.gov.br/\" title=\"Tradutor de libras\">Tradutor de libras</a> /</li>\r\n"
    			+ "                                            <li><a class=\"rybena\" title=\"Imprensa\">Imprensa</a></li>\r\n"
    			+ "                                        </ul>\r\n"
    			+ "                                    </nav><!-- hotlinks -->\r\n"
    			+ "                                </li> \r\n"
    			+ "</ul><!-- fim menu principal -->\r\n"
    			+ "                            <div class=\"search-box noindex\"><!-- search-box -->                                                                                              \r\n"
    			+ "                                    <label class=\"hide\" for=\"q\">Pesquisar no Portal Caixa</label>\r\n"
    			+ "                                    <input type=\"text\" class=\"search-input\" id=\"q\" name=\"q\" onkeypress=\"enterPress()\" placeholder=\"Busque na Caixa\" autocomplete=\"off\">                      \r\n"
    			+ "                                      <button type=\"button\" class=\"ir\" onclick=\"buscarPesquisa()\"><span>Buscar</span></button>   \r\n"
    			+ "                                <div class=\"suggest-box\">                                                                                                            \r\n"
    			+ "                                    <ul class=\"suggest-list no-bullets\"></ul>                                                                                        \r\n"
    			+ "                                    <div class=\"suggest-all\">                                                                                                        \r\n"
    			+ "                                        <a href=\"#\" title=\"\" class=\"suggest-all-link\">Ver todos os resultados ›</a>                                                  \r\n"
    			+ "                                    </div>                                                                                                                           \r\n"
    			+ "                                </div>                                                                                                                               \r\n"
    			+ "                            </div><!-- fim search-box -->                                                                                                            \r\n"
    			+ "                        </div><!-- fim main-menu -->                                                                                                                 \r\n"
    			+ "                        <div class=\"account noindex\"><!-- login -->                                                                                                          \r\n"
    			+ "                            <!-- form action=\"#\" method=\"get\">                                                                                                       \r\n"
    			+ "                                <input type=\"text\" placeholder=\"Usuário\" name=\"usuario\" />                                                                           \r\n"
    			+ "                                <input type=\"text\" placeholder=\"Senha\" name=\"senha\" />                                                                               \r\n"
    			+ "                                <button type=\"submit\"><span class=\"icon\"></span> Acessar</button>                                                                    \r\n"
    			+ "                            </form -->                                                                                                                               \r\n"
    			+ "                            <a href=\"https://internetbanking.caixa.gov.br/sinbc/#!nb/login\" title=\"Acessar minha conta\" tabindex=\"1\" id=\"AcessoAConta\" name=\"AcessoAConta\" accesskey=\"4\" class=\"btn orange non-fluid\"><span class=\"icon\"></span> Acessar minha conta </a>                                          \r\n"
    			+ "                        </div><!-- fim login -->                                                                                                                     \r\n"
    			+ "                        <!-- mobile -->\r\n"
    			+ "                        <div class=\"mobile-menu noindex hidden-desktop\">                                                                                                     \r\n"
    			+ "                            <a href=\"#\" title=\"\">Menu</a>                                                                                                           \r\n"
    			+ "                        </div>\r\n"
    			+ "                        <div class=\"mobile-account noindex visible-mobile\">                                                                                               \r\n"
    			+ "                            <a href=\"https://internetbanking.caixa.gov.br/sinbc/#!nb/login\" title=\"https://internetbanking.caixa.gov.br/sinbc/#!nb/login\">Conta</a>\r\n"
    			+ "                        </div>\r\n"
    			+ "                        <!-- div class=\"mobile-account-access visible-mobile\">                                                                                       \r\n"
    			+ "                            <p class=\"zeta\">Acesse sua conta</p>                                                                                                     \r\n"
    			+ "                            <form>                                                                                                                                   \r\n"
    			+ "                                <input type=\"text\" class=\"field-d\" placeholder=\"Usuário\" />                                                                          \r\n"
    			+ "                                <input type=\"password\" class=\"field-d\" placeholder=\"Senha\" />                                                                        \r\n"
    			+ "                                                                                                                                                                     \r\n"
    			+ "                                <button type=\"submit\" class=\"btn blue non-fluid btn-big\"><i class=\"font-icon i-lock\"></i> Acessar</button>                           \r\n"
    			+ "                            </form>                                                                                                                                  \r\n"
    			+ "                        </div -->                                                                                                                                    \r\n"
    			+ "                        <!-- fim mobile -->                                                                                                                          \r\n"
    			+ "                    </nav><!-- mainnav -->                                                                                                                           \r\n"
    			+ "                </div>                                                                                                                                               \r\n"
    			+ "            </header><!-- fim header menu -->                                                                                                                        \r\n"
    			+ "      </div>\r\n"
    			+ "	\r\n"
    			+ "    <div class=\"content-hero content-section section-index noprint\">\r\n"
    			+ "        <p class=\"breadcrumb\">\r\n"
    			+ "            <a href=\"http://www.caixa.gov.br/Paginas/home-caixa.aspx\">Início</a> › <a href=\"http://www.caixa.gov.br/voce/Paginas/default.aspx\">Produtos para você</a> › <a href=\"index.asp\">Imóveis à venda</a> › Detalhe\r\n"
    			+ "        </p>\r\n"
    			+ "		<br>\r\n"
    			+ "		<div class=\"special2\" style=\"padding-left: 40px; padding-right:40px;\">\r\n"
    			+ "			<div class=\"section-index2\">\r\n"
    			+ "				<ul class=\"menu-simov-direita no-bullets\">\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_buscarimoveis\">Buscar<br>imóveis</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_disputas\">Minhas<br>disputas</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_resultados\">Meus<br>resultados</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_favoritos\">Meus<br>favoritos</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_dados\">Dados<br>cadastrais</button>\r\n"
    			+ "					</li>					\r\n"
    			+ "				</ul>\r\n"
    			+ "			</div>\r\n"
    			+ "		</div>			\r\n"
    			+ "    </div>\r\n"
    			+ "	<form method=\"post\" id=\"frm_detalhe\">\r\n"
    			+ "		<div id=\"dadosImovel\" class=\"content-section section-text with-box\" style=\"padding-top: 20px;\">\r\n"
    			+ "			<a href=\"#\" class=\"top-link visible-mobile\">Topo</a>\r\n"
    			+ "\r\n"
    			+ "			<div class=\"content-wrapper clearfix\">\r\n"
    			+ "			\r\n"
    			+ "					<div class=\"control-item control-span-12_12\">\r\n"
    			+ "						<h5 style=\"margin-bottom: 0.5rem; color: #006bae;\">\r\n"
    			+ "							COND. IPE AMARELO\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnNumTipoVenda\" id=\"hdnNumTipoVenda\" value=\"0\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_tp_imovel\" id=\"hdn_tp_imovel\" value=\"Selecione\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_quartos\" id=\"hdn_quartos\" value=\"Selecione\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_vg_garagem\" id=\"hdn_vg_garagem\" value=\"Selecione\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_area_util\" id=\"hdn_area_util\" value=\"Selecione\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_faixa_vlr\" id=\"hdn_faixa_vlr\" value=\"Selecione\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_vlr_maximo\" id=\"hdn_vlr_maximo\" value=\"\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_estado\" id=\"hdn_estado\" value=\"DF\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_cidade\" id=\"hdn_cidade\" value=\"1809\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_bairro\" id=\"hdn_bairro\" value=\"\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_nobairro\" id=\"hdn_nobairro\" value=\"\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_nocidade\" id=\"hdn_nocidade\" value=\"BRASILIA\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnImov1\" id=\"hdnImov1\" value=\"08787704614310||08787710759006||01444418278205||08787708768460||08555503350713||01444418752802||00000010170139||00000010193265||00000010193264||00000010190605\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnQtdPag\" id=\"hdnQtdPag\" value=\"4\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnPagNum\" id=\"hdnPagNum\" value=\"3\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnQtdRegistros\" id=\"hdnQtdRegistros\" value=\"36\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdninteresse\" id=\"hdninteresse\" value=\"\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnimovel\" id=\"hdnimovel\" value=\"8787700138874\">\r\n"
    			+ "\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnNumLicit\" id=\"hdnNumLicit\" value=\"\">\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnSgComissao\" id=\"hdnSgComissao\" value=\"\">\r\n"
    			+ "							<input type=\"hidden\" name=\"frmVendaOnline\" id=\"frmVendaOnline\" value=\"\">							\r\n"
    			+ "							<input type=\"hidden\" name=\"hdnorigem\" id=\"hdnorigem\" value=\"buscaimovel\">\r\n"
    			+ "							\r\n"
    			+ "							<input type=\"hidden\" name=\"hdn_modalidade\" id=\"hdn_modalidade\" value=\"Selecione\">\r\n"
    			+ "							\r\n"
    			+ "						</h5>\r\n"
    			+ "					</div>\r\n"
    			+ "					\r\n"
    			+ "				<div class=\"content\">\r\n"
    			+ "					<p style=\"font-size:14pt\">Valor de avaliação: R$ 464.167,30<br><b>Valor mínimo de venda: R$ 292.239,73</b> ( desconto de 37,04%)</p>\r\n"
    			+ "					\r\n"
    			+ "					<div class=\"control-item control-span-6_12\">\r\n"
    			+ "						<p>\r\n"
    			+ "							<span>Tipo de imóvel: <strong>Apartamento</strong></span><br>\r\n"
    			+ "							<!--span>Situação: <strong>Desocupado</strong></span><br-->\r\n"
    			+ "							<span>Quartos: <strong>2</strong></span><br><span>Garagem: <strong>1</strong></span><br>\r\n"
    			+ "							<span>Número do imóvel: <strong>878770013887-4</strong></span><br>\r\n"
    			+ "							<span>Matrícula(s): <strong>70623</strong></span><br>\r\n"
    			+ "							<span>Comarca: <strong>GUARA-DF</strong></span><br>\r\n"
    			+ "							<span>Ofício: <strong>04</strong></span><br>\r\n"
    			+ "\r\n"
    			+ "							\r\n"
    			+ "							\r\n"
    			+ "								<span>Inscrição imobiliária: <strong>52666212</strong></span><br>\r\n"
    			+ "							\r\n"
    			+ "								<span>Averbação dos leilões negativos: <strong>\r\n"
    			+ "								Averbado\r\n"
    			+ "								</strong></span><br>\r\n"
    			+ "							\r\n"
    			+ "						</p>\r\n"
    			+ "					</div>\r\n"
    			+ "					\r\n"
    			+ "					<div class=\"control-item control-span-6_12\">\r\n"
    			+ "						<p>\r\n"
    			+ "							<span>Área total = <strong>97,71m2</strong></span><br><span>Área privativa = <strong>50,10m2</strong></span><br>\r\n"
    			+ "						</p>\r\n"
    			+ "					</div>\r\n"
    			+ "					\r\n"
    			+ "					<div class=\"control-item control-span-6_12\">\r\n"
    			+ "						<p>\r\n"
    			+ "						</p>\r\n"
    			+ "					</div>					\r\n"
    			+ "					\r\n"
    			+ "					\r\n"
    			+ "				</div>\r\n"
    			+ "\r\n"
    			+ "				<div class=\"related-box\" style=\"padding: 20px;\">\r\n"
    			+ "				<div id=\"divContador\"><div style=\"position:relative\"><div class=\"control-span-12_12\"><span style=\"color: #4c556c;\"><strong>Tempo restante na venda online:</strong></span></div>    <div class=\"control-span-12_12\" style=\"padding-top: 5px;\"><div class=\"time-part delta2\" id=\"dias0\">&nbsp;00&nbsp;<span class=\"time-part-label time-part-label-dias\">DIAS</span></div>&nbsp;    <div class=\"time-part delta2\" id=\"horas0\">&nbsp;00&nbsp;<span class=\"time-part-label time-part-label-horas\">HORAS</span></div>&nbsp;    <div class=\"time-part delta2\" id=\"minutos0\">&nbsp;00&nbsp;<span class=\"time-part-label time-part-label-minutos\">MINUTOS</span></div>&nbsp;    <div class=\"time-part delta2\" id=\"segundos0\">&nbsp;00&nbsp;<span class=\"time-part-label time-part-label-minutos\">SEGUNDOS</span></div></div></div></div><p style=\"margin-bottom: 0.5em;\"><strong>Endereço:</strong><br>QUADRA QN 22  APTO. 301 BLOCO 10 LOTES 1 A 5 CONJUNTO 03, RIACHO FUNDO II - CEP: 71881-778, BRASILIA - DISTRITO FEDERAL</p><br><span><a href=\"#\" class=\"\" onclick=\"javascript:ExibeDoc('/editais/matricula/DF/8787700138874.pdf')\">Baixar matrícula do imóvel</a></span><p style=\"margin-bottom: 0.5em;\"><strong>Descrição:</strong><br>2  Quartos, 1 Vaga na Garagem,  Área de Serviço,  Wc,  Sala,  Cozinha.&nbsp;o Atual Cessionário Possui Apenas Direito Real de Uso, Necessitando de Expressar Autorização da Codhab e União Federal Para Uso do Bem Por Terceiros.</p><p><i class=\"fa fa-info-circle\"></i>&nbsp;Imóvel NÃO aceita financiamento habitacional.<br><i class=\"fa fa-info-circle\"></i> Imóvel NÃO aceita parcelamento.<br><i class=\"fa fa-info-circle\"></i> Imóvel NÃO aceita consórcio.<br><i class=\"fa fa-info-circle\"></i>&nbsp;As despesas de tributos, até a data da venda, inferiores a R$ 1.000,00 (mil reais) ficarão a cargo do comprador.<br></p></div>\r\n"
    			+ "					<div class=\"control-item control-span-6_12\" style=\"padding-top:10px;\"><ul class=\"form-set no-bullets\" style=\"margin-top: 0px; margin-botton: 5px;width:80% !important;\">\r\n"
    			+ "							<li class=\"\" style=\"margin-top: 0px; margin-botton: 5px;\">	<span style=\"font-size:10pt;\"><a href=\"javascript:regrasVendaOnline();\" style=\"cursor:hand;\"><img src=\"venda-online/images/icon-condicoes.jpg?v1.0\" width=\"30px\">&nbsp;<strong>Regras da Venda Online</strong></a></span>&nbsp;	<span style=\"font-size:10pt;\"><a href=\"javascript:formasPagamento();\" style=\"cursor:hand;\"><img src=\"venda-online/images/icon-pagamento.jpg?v1.0\" width=\"30px\">&nbsp;<strong>Formas de pagamento</strong></a></span>	</li>	<li class=\"\">		<button type=\"button\" class=\"submit-d submit-orange\" onclick=\"javascript:Proposta()\">Fazer uma proposta</button>&nbsp;&nbsp;    	<span>&nbsp;ou&nbsp;<a href=\"javascript:Retornar();\" style=\"cursor:hand;\">Voltar</a></span>	</li>\r\n"
    			+ "						</ul>\r\n"
    			+ "					</div>				\r\n"
    			+ "				</div></div>\r\n"
    			+ "		\r\n"
    			+ "		<div id=\"galeria-imagens\" class=\"content-section section-slider\">\r\n"
    			+ "			<a href=\"#\" class=\"top-link visible-mobile\">Topo</a>\r\n"
    			+ "			<h2>Galeria de fotos</h2>\r\n"
    			+ "			<!--div class=\"slider\">\r\n"
    			+ "				<div class=\"slider-wrapper2\">\r\n"
    			+ "					<ul class=\"slider-itens\">\r\n"
    			+ "						\r\n"
    			+ "					</ul>\r\n"
    			+ "				</div>\r\n"
    			+ "			</div-->\r\n"
    			+ "			\r\n"
    			+ "			<div class=\"thumbnails\" align=\"center\">\r\n"
    			+ "				<img onclick=\"preview.src=&quot;/fotos/F878770013887421.jpg&quot;\" src=\"/fotos/F878770013887421.jpg\" alt=\"Foto do imóvel\" title=\"Foto do imóvel\" )=\"\">\r\n"
    			+ "				<!--img onclick=\"preview.src='F000000000000141.jpg'\" id=\"img3\" src=\"F000000000000142.jpg\" alt=\"Image Not Loaded\"/>\r\n"
    			+ "				<img onclick=\"preview.src='F818860012403021.jpg'\" id=\"img3\" src=\"F818860012403021.jpg\" alt=\"Image Not Loaded\"/>\r\n"
    			+ "				<img onclick=\"preview.src='F818860012403022.jpg'\" id=\"img3\" src=\"F818860012403022.jpg\" alt=\"Image Not Loaded\"/>\r\n"
    			+ "				<img onclick=\"preview.src='F820131217308821.jpg'\" id=\"img3\" src=\"F820131217308821.jpg\" alt=\"Image Not Loaded\"/-->\r\n"
    			+ "\r\n"
    			+ "				</div>\r\n"
    			+ "				<br>\r\n"
    			+ "\r\n"
    			+ "				<div class=\"preview\" align=\"center\">\r\n"
    			+ "					\r\n"
    			+ "					<!--img onclick=\"javascript:window.open(this.src, 'Foto', 'scrollbars=yes,width=700,height=600');\" id=\"preview\" src=\"/fotos/\" alt=\"\"/-->\r\n"
    			+ "					<img id=\"preview\" src=\"/fotos/F878770013887421.jpg\" alt=\"Foto do imóvel\">\r\n"
    			+ "					\r\n"
    			+ "				</div>\r\n"
    			+ "				<br>			\r\n"
    			+ "			</div>\r\n"
    			+ "		</form></div>\r\n"
    			+ "	\r\n"
    			+ "\r\n"
    			+ "    <div class=\"content-section section-related\">\r\n"
    			+ "\r\n"
    			+ "		<h2>Outros produtos Caixa</h2>\r\n"
    			+ "\r\n"
    			+ "        <ul class=\"related-content\">\r\n"
    			+ "            <li>\r\n"
    			+ "                <h3 class=\"zeta\"><a href=\"http://www.caixa.gov.br/voce/credito-financiamento\">Melhor crédito para você</a></h3>\r\n"
    			+ "                <p>Se você acredita que pode mais, a Caixa acredita com você.\r\n"
    			+ "                </p>\r\n"
    			+ "                <p class=\"see-more\"><a href=\"http://www.caixa.gov.br/voce/credito-financiamento\">Veja as opções ›</a></p>\r\n"
    			+ "            </li>\r\n"
    			+ "            <li>\r\n"
    			+ "                <h3 class=\"zeta\"><a href=\"http://www.caixa.gov.br/voce/habitacao\">Casa própria</a></h3>\r\n"
    			+ "                <p>Venha para a Caixa e a realize o sonho da casa própria.</p>\r\n"
    			+ "                <p class=\"see-more\"><a href=\"http://www.caixa.gov.br/voce/habitacao\">Compre sua casa própria ›</a></p>\r\n"
    			+ "            </li>\r\n"
    			+ "            <li>\r\n"
    			+ "                <h3 class=\"zeta\"><a href=\"http://www.caixa.gov.br/voce/cartoes\">Cartões Caixa</a></h3>\r\n"
    			+ "                <p>A Caixa tem sempre um cartão perfeito para você.</p>\r\n"
    			+ "                <p class=\"see-more\"><a href=\"http://www.caixa.gov.br/voce/cartoes\">Escolha agora o seu ›</a></p>\r\n"
    			+ "            </li>\r\n"
    			+ "            <li>\r\n"
    			+ "                <h3 class=\"zeta\"><a href=\"http://www.caixa.gov.br/voce/contas\">Contas Caixa</a></h3>\r\n"
    			+ "                <p>As contas Caixa têm as menores tarifas do mercado.</p>\r\n"
    			+ "                <p class=\"see-more\"><a href=\"http://www.caixa.gov.br/voce/contas\">Abra sua conta ›</a></p>\r\n"
    			+ "            </li>\r\n"
    			+ "        </ul>\r\n"
    			+ "    </div>	\r\n"
    			+ "	\r\n"
    			+ "    <footer id=\"footer\" class=\"noprint\">\r\n"
    			+ "    <!-- Inicio footer -->                                                                   \r\n"
    			+ "    <div class=\"footer-content clearfix\">                                                    \r\n"
    			+ "        <div class=\"footer-column noindex\">                                                          \r\n"
    			+ "            <p>\r\n"
    			+ "                <span class=\"fone\">0800 726 0207 </span><br>\r\n"
    			+ "                <span class=\"soft\">Caixa Cidadão</span>\r\n"
    			+ "            </p>\r\n"
    			+ "            <p>\r\n"
    			+ "                <span class=\"fone\">0800 726 0101</span><br>\r\n"
    			+ "                <span class=\"soft\">SAC</span>\r\n"
    			+ "            </p>\r\n"
    			+ "        </div>                                                                               \r\n"
    			+ "        <div class=\"footer-column noindex\">                                                          \r\n"
    			+ "            <ul>                                                                             \r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#telefones-caixa\" title=\"Todos os telefones\">Todos os telefones</a></li>\r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#telefones-caixa\" title=\"Deficiente Auditivo\">Deficiente Auditivo</a></li>\r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/caixa-a-z/Paginas/default.aspx\" title=\"Caixa de A - Z\">Caixa de A - Z</a></li>\r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/atendimento/Paginas/default.aspx#encontre\" title=\"Encontre uma Agência\">Encontre uma Agência</a></li>\r\n"
    			+ "            </ul>                                                                            \r\n"
    			+ "        </div>                                                                               \r\n"
    			+ "        <div class=\"footer-column noindex\">                                                          \r\n"
    			+ "            <ul>                                                                             \r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/sobre-a-caixa\" title=\"Sobre a Caixa\">Sobre a Caixa</a></li>\r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/sobre-a-caixa/trabalhe-na-caixa\" title=\"Trabalhe na Caixa\">Trabalhe na Caixa</a></li>\r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/atendimento/aplicativos\" title=\"Aplicativos\">Aplicativos</a></li>\r\n"
    			+ "                <li><a href=\"http://www.caixa.gov.br/acesso-a-informacao\" title=\"Acesso à Informação\">Acesso à Informação</a></li>\r\n"
    			+ "            </ul>                                                                            \r\n"
    			+ "        </div>                                                                               \r\n"
    			+ "        <div class=\"footer-column noindex\">                                                          \r\n"
    			+ "            <ul>                                                                             \r\n"
    			+ "                <li><a href=\"http://www.twitter.com/caixa\" title=\"Twitter\"><i class=\"icon twitter\"></i>Twitter</a></li>\r\n"
    			+ "                <li><a href=\"http://www.facebook.com/caixa&quot;\" title=\"Facebook\"><i class=\"icon facebook\"></i>Facebook</a></li>\r\n"
    			+ "                <li><a href=\"http://www.youtube.com/canalcaixa\" title=\"Youtube\"><i class=\"icon youtube\"></i>Youtube</a></li>\r\n"
    			+ "		<li><a href=\"https://plus.google.com/u/1/101382192914122349840/posts\" title=\"Google\"><i class=\"icon gplus\"></i>Google</a></li>\r\n"
    			+ "            </ul>\r\n"
    			+ "        </div>\r\n"
    			+ "    </div>\r\n"
    			+ "    <div class=\"hotlinks noindex clearfix\">\r\n"
    			+ "        <ul class=\"languages\">\r\n"
    			+ "            <li><a href=\"http://www.caixa.gov.br/site/english\" title=\"English\">English</a></li>\r\n"
    			+ "        </ul>\r\n"
    			+ "        <ul>\r\n"
    			+ "            <li><a href=\"http://www.caixa.gov.br/atendimento\" title=\"Ouvidoria\">Ouvidoria</a></li>\r\n"
    			+ "            <li><a href=\"http://www.caixa.gov.br/politica-de-privacidade\" title=\"Politica de privacidades\">Politica de privacidades</a></li>\r\n"
    			+ "            <li><a href=\"http://www.caixa.gov.br/termos-de-uso\" title=\"Termos de uso\">Termos de uso</a></li>\r\n"
    			+ "        </ul>\r\n"
    			+ "        <ul>\r\n"
    			+ "            <li><a href=\"http://www.caixa.gov.br/caixa-a-z/Paginas/default.aspx\" title=\"Caixa A-Z\">Caixa A-Z</a></li>                                   \r\n"
    			+ "            <li><a href=\"http://www.caixa.gov.br/seguranca\" title=\"Segurança\">Segurança</a></li>                                   \r\n"
    			+ "            <li><a href=\"http://www20.caixa.gov.br/Paginas/Default.aspx\" title=\"Imprensa\">Imprensa</a></li>                                   \r\n"
    			+ "        </ul>\r\n"
    			+ "        <ul class=\"ainfo\">\r\n"
    			+ "               <li><a href=\"http://www.caixa.gov.br/acesso-a-informacao\" title=\"\"><img src=\"\"></a></li>\r\n"
    			+ "        </ul>\r\n"
    			+ "    </div>\r\n"
    			+ "</footer>\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "<div class=\"grid-overlay\">\r\n"
    			+ "    <div class=\"grid-overlay__wrapper clearfix\">\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "        <div class=\"col-1\"></div>\r\n"
    			+ "    </div>\r\n"
    			+ "</div>\r\n"
    			+ "\r\n"
    			+ "<script type=\"text/javascript\">\r\n"
    			+ "\r\n"
    			+ "function regrasVendaOnline(){\r\n"
    			+ "	//window.open(\"venda-online/comocomprar.pdf?v=011\");\r\n"
    			+ "	window.open(\"/editais/regras-VOL/comocomprar.pdf?v=01\");\r\n"
    			+ "	\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function formasPagamento(){\r\n"
    			+ "	window.open(\"https://habitacao.caixa.gov.br/siopiweb-web/simulaOperacaoInternet.do?method=inicializarCasoUso\", \"Simulador\", \"scrollbars=yes,width=1001,height=800\");\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function Imovel_Ocupado()\r\n"
    			+ "{\r\n"
    			+ "	alert(\"Não é possível efetuar agendamento de visita para um imóvel ocupado.\");\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function exibefoto(nome){\r\n"
    			+ "	alert(\"<img src='\" + nome + \"'>\");\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function ExibeDoc(documento){\r\n"
    			+ "	window.open(documento,'Edital ' + documento);\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "exibeLances = function(msg) {\r\n"
    			+ "\r\n"
    			+ "	$('#div_dialog_simov').html(msg)\r\n"
    			+ "	$( \"#div_dialog_simov\" ).dialog({\r\n"
    			+ "		resizable: false,\r\n"
    			+ "		dialogClass: \"no_titlebar\",\r\n"
    			+ "		height:580,\r\n"
    			+ "		width:440,\r\n"
    			+ "		modal: true,\r\n"
    			+ "		buttons: {\r\n"
    			+ "			\"OK\": function() {\r\n"
    			+ "				$( this ).dialog( \"close\" );\r\n"
    			+ "				$('#div_dialog_simov').html('');\r\n"
    			+ "				//timerTela = setTimeout( refreshTela, 1000);\r\n"
    			+ "			}\r\n"
    			+ "		}\r\n"
    			+ "	});\r\n"
    			+ "};	\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "function visualizarLances(nuProposta){\r\n"
    			+ "\r\n"
    			+ "	NU_PROPOSTA = nuProposta;\r\n"
    			+ "	\r\n"
    			+ "	$.post(\"venda-online/carregaLances.asp\",\r\n"
    			+ "	{\r\n"
    			+ "		p_hdnProposta: \"\",\r\n"
    			+ "		p_hdnImovel: \"8787700138874\"\r\n"
    			+ "	},\r\n"
    			+ "	function(data, status){\r\n"
    			+ "		if (status==\"success\") {\r\n"
    			+ "			if (consultaRealizada(data)) {\r\n"
    			+ "				//habilitaTimer = false;\r\n"
    			+ "				exibeLances(data);\r\n"
    			+ "			//} else {\r\n"
    			+ "			//	clearTimeout(timerTela);\r\n"
    			+ "			//	$('#listaPropostas').html('<div class=\"feedback feedback-warning\"><span class=\"feedback-text\">Nenhuma disputa em andamento.</span></div>');\r\n"
    			+ "			//	$('#listaPropostas').show();		\r\n"
    			+ "			}\r\n"
    			+ "		}\r\n"
    			+ "	});\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function consultaRealizada(retorno){\r\n"
    			+ "	if (retorno.indexOf(\"Nenhum\") > -1 || retorno.indexOf(\"Não há dados\") > -1 || retorno.indexOf(\"Ocorreu\") > -1 || retorno.indexOf(\"Fechar a seção\") > -1){\r\n"
    			+ "		closePrompt();\r\n"
    			+ "		//if (retorno.indexOf(\"Fechar a seção\") > -1) {\r\n"
    			+ "			//limpasessao();\r\n"
    			+ "			//logout2();\r\n"
    			+ "		//} else {\r\n"
    			+ "			if (retorno.indexOf(\"Não há dados\") > -1 ) \r\n"
    			+ "				alert(\"<h6>Não há lances registrados para o imóvel.</h6>\");\r\n"
    			+ "		//}\r\n"
    			+ "		//habilitaTimer = false;\r\n"
    			+ "		return false;\r\n"
    			+ "	} else {\r\n"
    			+ "		return true;\r\n"
    			+ "	}\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "confirmacao = function() {\r\n"
    			+ "	jQuery('#div_dialog_simov').html(\"<span class='ui-icon ui-icon-alert' style='float:left; margin:0 7px 20px 0;'></span><h5>Este imóvel foi adicionado a sua lista com sucesso.</h5>Selecione uma das opções abaixo:<br>\")\r\n"
    			+ "	$( \"#div_dialog_simov\" ).dialog({\r\n"
    			+ "		resizable: false,\r\n"
    			+ "		dialogClass: \"no_titlebar\",\r\n"
    			+ "		height:320,\r\n"
    			+ "		width:440,\r\n"
    			+ "		modal: true,\r\n"
    			+ "		buttons: {\r\n"
    			+ "			\"Ir para a minha lista\": function() {\r\n"
    			+ "				$( this ).dialog( \"close\" );\r\n"
    			+ "				jQuery('#div_dialog_simov').html('');\r\n"
    			+ "				$('#frm_detalhe').attr('action','minha-lista.asp').trigger('submit');\r\n"
    			+ "			},\r\n"
    			+ "			\"Ver mais imóveis\": function() {\r\n"
    			+ "				$( this ).dialog( \"close\" );\r\n"
    			+ "				jQuery('#div_dialog_simov').html('');\r\n"
    			+ "			}\r\n"
    			+ "		}\r\n"
    			+ "	});\r\n"
    			+ "};	\r\n"
    			+ "\r\n"
    			+ "function Incluir_Lista(NumImovel)\r\n"
    			+ "{\r\n"
    			+ "	var iPos\r\n"
    			+ "	var sSelecao = jQuery('#hdninteresse').val();\r\n"
    			+ "	var sBase = \"||\" + sSelecao + \"||\";\r\n"
    			+ "\r\n"
    			+ "	iPos = sBase.indexOf(\"||\" + NumImovel + \"||\");\r\n"
    			+ "    if (iPos == -1)\r\n"
    			+ "	{\r\n"
    			+ "		if (sSelecao != \"\")\r\n"
    			+ "		{  \r\n"
    			+ "			sSelecao = sSelecao + \"||\";  \r\n"
    			+ "		}\r\n"
    			+ "		jQuery('#hdninteresse').val(sSelecao + NumImovel);\r\n"
    			+ "	}\r\n"
    			+ "\r\n"
    			+ "	jQuery('#hdnimovel').val(NumImovel);\r\n"
    			+ "	confirmacao();\r\n"
    			+ "\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "window.onload = function () {\r\n"
    			+ "	carregaContador();\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "function Agendar_Visita(NumImovel)\r\n"
    			+ "{\r\n"
    			+ "	var iPos\r\n"
    			+ "	var sSelecao = jQuery('#hdninteresse').val();\r\n"
    			+ "	var sBase = \"||\" + sSelecao + \"||\";\r\n"
    			+ "\r\n"
    			+ "	iPos = sBase.indexOf(\"||\" + NumImovel + \"||\");\r\n"
    			+ "    if (iPos == -1)\r\n"
    			+ "	{\r\n"
    			+ "		if (sSelecao != \"\")\r\n"
    			+ "		{  \r\n"
    			+ "			sSelecao = sSelecao + \"||\";  \r\n"
    			+ "		}\r\n"
    			+ "		jQuery('#hdninteresse').val(sSelecao + NumImovel);\r\n"
    			+ "	}\r\n"
    			+ "	jQuery('#hdnimovel').val(NumImovel);\r\n"
    			+ "	$('#frm_detalhe').attr('action','agendar-visita.asp').trigger('submit');\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "function Interesse(NumImovel)\r\n"
    			+ "{\r\n"
    			+ "	jQuery('#hdnimovel').val(NumImovel);\r\n"
    			+ "	$('#frm_detalhe').attr('action','tenho-interesse.asp').trigger('submit');\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function Retornar()\r\n"
    			+ "{\r\n"
    			+ "	if (jQuery('#hdnorigem').val() == 'index')\r\n"
    			+ "		$('#frm_detalhe').attr('action','index.asp#destaques').trigger('submit')\r\n"
    			+ "	else {\r\n"
    			+ "		if (jQuery('#hdnorigem').val() == 'minhalista')\r\n"
    			+ "			$('#frm_detalhe').attr('action','minha-lista.asp').trigger('submit')\r\n"
    			+ "		else {\r\n"
    			+ "			if (jQuery('#hdnorigem').val() == 'favoritos')\r\n"
    			+ "				$('#frm_detalhe').attr('action','venda-online/favoritos.asp?hdnorigem=buscaimovel&hdnimovel=8787700138874&hdn_estado=DF&hdn_cidade=1809&hdn_bairro=&hdnQtdPag=4&hdnPagNum=3').trigger('submit')\r\n"
    			+ "			else {\r\n"
    			+ "				//jQuery('#hdnNumTipoVenda').val('0');\r\n"
    			+ "				//$('#frm_detalhe').attr('action','busca-imovel.asp?hdnNumTipoVenda=0').trigger('submit');\r\n"
    			+ "				\r\n"
    			+ "					$('#frm_detalhe').attr('action','busca-imovel.asp?hdnLocalidade=1809&hdnModalidade=Selecione&hdnValorSimulador=&hdnAceitaFGTS=&hdnAceitaFinanciamento=').trigger('submit');\r\n"
    			+ "				\r\n"
    			+ "			}\r\n"
    			+ "		}\r\n"
    			+ "	}\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function Proposta()\r\n"
    			+ "{\r\n"
    			+ "	$('#frm_detalhe').attr('action','venda-online/index.asp?hdnimovel=8787700138874&hdn_estado=DF&hdn_cidade=1809&hdn_bairro=&hdnQtdPag=4&hdnPagNum=3').trigger('submit');\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "function Preferencia()\r\n"
    			+ "{\r\n"
    			+ "	$('#frm_detalhe').attr('action','venda-online/index-mcmv.asp?hdnimovel=8787700138874&hdn_estado=DF&hdn_cidade=1809&hdn_bairro=&hdnQtdPag=4&hdnPagNum=3').trigger('submit');\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "SiteLeiloeiro = function(site) {\r\n"
    			+ "	jQuery('#div_dialog_simov').html(\"<span class='ui-icon ui-icon-alert' style='float:left; margin:0 7px 20px 0;'></span><h5>Atenção</h5>Você será direcionado(a) para o site do Leiloeiro previsto no edital.\")\r\n"
    			+ "	$( \"#div_dialog_simov\" ).dialog({\r\n"
    			+ "		resizable: false,\r\n"
    			+ "		dialogClass: \"no_titlebar\",\r\n"
    			+ "		height:320,\r\n"
    			+ "		width:440,\r\n"
    			+ "		modal: true,\r\n"
    			+ "		buttons: {\r\n"
    			+ "			\"Ok\": function() {\r\n"
    			+ "				\r\n"
    			+ "				$( this ).dialog( \"close\" );\r\n"
    			+ "				jQuery('#div_dialog_simov').html('');\r\n"
    			+ "				\r\n"
    			+ "				if (site.indexOf('http') == -1) {\r\n"
    			+ "					window.open('http://'+site);\r\n"
    			+ "				} else {\r\n"
    			+ "					window.open(site);\r\n"
    			+ "				}\r\n"
    			+ "			},\r\n"
    			+ "			\"Cancelar\": function() {\r\n"
    			+ "				$( this ).dialog( \"close\" );\r\n"
    			+ "				jQuery('#div_dialog_simov').html('');\r\n"
    			+ "			}\r\n"
    			+ "		}\r\n"
    			+ "	});\r\n"
    			+ "};	\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "var contaRefresh = 30;\r\n"
    			+ "function carregaContador(){\r\n"
    			+ "	\r\n"
    			+ "	if (contaRefresh == 30) {\r\n"
    			+ "		contaRefresh = 0;\r\n"
    			+ "		$.post(\"venda-online/carregaContador.asp\",\r\n"
    			+ "		{\r\n"
    			+ "			strLista: \"1@@\" + \"\" + \"@@\" + \"\" + \"||\"\r\n"
    			+ "		},\r\n"
    			+ "		function(data, status){\r\n"
    			+ "			if (status==\"success\") {\r\n"
    			+ "				if (data != 'Fechar a seção') {\r\n"
    			+ "					$('#divContador').html(data.replace(\"||\", \"\"));\r\n"
    			+ "					//if (\"\" == \"\")\r\n"
    			+ "					//	$('#divContador').html(data.replace(\"||\", \"\"))\r\n"
    			+ "					//else\r\n"
    			+ "					//	$('#divContador').html(\"<div style='font-size: 10pt; color: #fff; background: #ff7200; padding: 5px; position: absolute; Top: \" + ($('#divContador').position().top+20) + \"px; Left: \" + ($('#divContador').position().left+320) + \"px;'><b>Imóvel em disputa</b></div>\" + data.replace(\"||\", \"\"));\r\n"
    			+ "				} else {\r\n"
    			+ "					logout2();\r\n"
    			+ "				}\r\n"
    			+ "			}\r\n"
    			+ "		});\r\n"
    			+ "	} else {\r\n"
    			+ "		var dias = $('#dias0').text().replace('DIAS', '').trim();\r\n"
    			+ "		var horas = $('#horas0').text().replace('HORAS', '').trim();\r\n"
    			+ "		var minutos = $('#minutos0').text().replace('MINUTOS', '').trim();\r\n"
    			+ "		var segundos = $('#segundos0').text().replace('SEGUNDOS', '').trim();\r\n"
    			+ "		\r\n"
    			+ "		//console.log(dias + ' - ' + horas + ' - ' + minutos + ' - ' + segundos);\r\n"
    			+ "		\r\n"
    			+ "		var prazo = (Number(dias) * 86400) + (Number(horas) * 3600) + (Number(minutos) * 60) + Number(segundos);\r\n"
    			+ "		var resto = 0;\r\n"
    			+ "		\r\n"
    			+ "		//console.log(prazo);\r\n"
    			+ "		prazo -= 1;\r\n"
    			+ "		//console.log(prazo);\r\n"
    			+ "		\r\n"
    			+ "		if (prazo > 0){\r\n"
    			+ "			if (prazo >= 86400){\r\n"
    			+ "				dias = prazo / 86400;\r\n"
    			+ "				resto = prazo % 86400;\r\n"
    			+ "			} else {\r\n"
    			+ "				resto = prazo;\r\n"
    			+ "			}\r\n"
    			+ "			//console.log(resto);\r\n"
    			+ "			if (resto >= 3600) {\r\n"
    			+ "				horas = resto / 3600;\r\n"
    			+ "				resto = resto % 3600;\r\n"
    			+ "			}\r\n"
    			+ "			//console.log(resto);\r\n"
    			+ "			if (resto >= 60) {\r\n"
    			+ "				minutos = resto / 60;\r\n"
    			+ "				resto = resto % 60;\r\n"
    			+ "			}\r\n"
    			+ "			//console.log(resto);\r\n"
    			+ "			segundos = resto\r\n"
    			+ "			\r\n"
    			+ "			//console.log(parseInt(dias) + ' - ' + parseInt(horas) + ' - ' + parseInt(minutos) + ' - ' + parseInt(segundos));\r\n"
    			+ "		} else {\r\n"
    			+ "			dias = 0;\r\n"
    			+ "			horas = 0;\r\n"
    			+ "			minutos = 0;\r\n"
    			+ "			segundos = 0;\r\n"
    			+ "		}\r\n"
    			+ "		\r\n"
    			+ "		var dias_s = '0' + String(parseInt(dias));\r\n"
    			+ "		var horas_s = '0' + String(parseInt(horas));\r\n"
    			+ "		var minutos_s = '0' + String(parseInt(minutos));\r\n"
    			+ "		var segundos_s = '0' + String(parseInt(segundos));\r\n"
    			+ "\r\n"
    			+ "		$('#dias0').html('&nbsp' + dias_s.substring(dias_s.length-2, dias_s.length) + '&nbsp;<span class=\"time-part-label time-part-label-dias\">DIAS</span>');\r\n"
    			+ "		$('#horas0').html('&nbsp' + horas_s.substring(horas_s.length-2, horas_s.length) + '&nbsp;<span class=\"time-part-label time-part-label-horas\">HORAS</span>');\r\n"
    			+ "		$('#minutos0').html('&nbsp' + minutos_s.substring(minutos_s.length-2, minutos_s.length) + '&nbsp;<span class=\"time-part-label time-part-label-minutos\">MINUTOS</span>');\r\n"
    			+ "		$('#segundos0').html('&nbsp' + segundos_s.substring(segundos_s.length-2, segundos_s.length) + '&nbsp;<span class=\"time-part-label time-part-label-minutos\">SEGUNDOS</span>');\r\n"
    			+ "	}\r\n"
    			+ "	\r\n"
    			+ "	\r\n"
    			+ "	contaRefresh++;\r\n"
    			+ "}\r\n"
    			+ "\r\n"
    			+ "logout2 = function() {\r\n"
    			+ "	jQuery('#div_dialog_simov').html(\"<h5>Sua sessão expirou.</h5>Refaça a pesquisa de imóveis.\");\r\n"
    			+ "	$( \"#div_dialog_simov\" ).dialog({\r\n"
    			+ "		resizable: false,\r\n"
    			+ "		dialogClass: \"no_titlebar\",\r\n"
    			+ "		height:300,\r\n"
    			+ "		width:340,\r\n"
    			+ "		modal: true,\r\n"
    			+ "		buttons: {\r\n"
    			+ "			\"OK\": function() {\r\n"
    			+ "				$( this ).dialog( \"close\" );\r\n"
    			+ "				jQuery('#div_dialog_simov').html('');\r\n"
    			+ "				$('#frm_detalhe').attr('action','busca-imovel.asp?hdnNumTipoVenda=0').trigger('submit');\r\n"
    			+ "			}\r\n"
    			+ "		}\r\n"
    			+ "	});\r\n"
    			+ "};\r\n"
    			+ "\r\n"
    			+ "</script>\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "\r\n"
    			+ "<div class=\"content-hero content-section section-index noprint section-index-fixed section-index-visible\">\r\n"
    			+ "        <p class=\"breadcrumb\">\r\n"
    			+ "            <a href=\"http://www.caixa.gov.br/Paginas/home-caixa.aspx\">Início</a> › <a href=\"http://www.caixa.gov.br/voce/Paginas/default.aspx\">Produtos para você</a> › <a href=\"index.asp\">Imóveis à venda</a> › Detalhe\r\n"
    			+ "        </p>\r\n"
    			+ "		<br>\r\n"
    			+ "		<div class=\"special2\" style=\"padding-left: 40px; padding-right:40px;\">\r\n"
    			+ "			<div class=\"section-index2\">\r\n"
    			+ "				<ul class=\"menu-simov-direita no-bullets\">\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_buscarimoveis\">Buscar<br>imóveis</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_disputas\">Minhas<br>disputas</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_resultados\">Meus<br>resultados</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_favoritos\">Meus<br>favoritos</button>\r\n"
    			+ "					</li>\r\n"
    			+ "					<li>\r\n"
    			+ "						<button type=\"button\" class=\"submit-d submit-orange submit-small\" id=\"btn_dados\">Dados<br>cadastrais</button>\r\n"
    			+ "					</li>					\r\n"
    			+ "				</ul>\r\n"
    			+ "			</div>\r\n"
    			+ "		</div>			\r\n"
    			+ "    </div><div class=\"ui-dialog ui-widget ui-widget-content ui-corner-all ui-front no_titlebar no_background ui-draggable\" tabindex=\"-1\" role=\"dialog\" aria-describedby=\"div_processando\" aria-labelledby=\"ui-id-1\" style=\"display: none;\"><div class=\"ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix\"><span id=\"ui-id-1\" class=\"ui-dialog-title\">&nbsp;</span><button class=\"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close\" role=\"button\" aria-disabled=\"false\" title=\"close\"><span class=\"ui-button-icon-primary ui-icon ui-icon-closethick\"></span><span class=\"ui-button-text\">close</span></button></div><div id=\"div_processando\" class=\"alinha_centro fonte_laranja ui-dialog-content ui-widget-content\" style=\"background: transparent !important; border: 0px !important; display: none;\"><img border=\"0\" title=\"Processando. Aguarde!\" src=\"images/loading.gif?v=1.0\"></div></div><div id=\"div_dialog_simov\" style=\"display: none;\"></div><div id=\"afi-root-container\" style=\"position: fixed; top: 0px; right: 0px; z-index: 2147483647;\"></div><script type=\"text/javascript\" id=\"\">function encontrarDadosSensiveis(a){var b=/\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}/g,c=/\\d{2}\\.\\d{3}\\.\\d{3}-\\d/g,d=/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}/g;b=a.match(b);c=a.match(c);d=a.match(d);return b||c||d?\"dados sens\\u00edveis\":a}function collectPesquisaInterna(){var a=document.querySelector(\"[name\\x3d'pesquisaHeader']\");a.addEventListener(\"focusout\",function(){var b=a.value;b=encontrarDadosSensiveis(b);dataLayer.push({event:\"search\",search_term:b})})}collectPesquisaInterna();</script></body></html>";
    }
    
    private void checkImovelAtivo(Imovel imovel) {
    	boolean isAtivo = false;
    	String detailUrl = "https://venda-imoveis.caixa.gov.br/sistema/detalhe-imovel.asp";
        
        Document detailDocument = null;
    	
        try {
			detailDocument = Jsoup.connect(detailUrl)
			        .method(org.jsoup.Connection.Method.POST)
			        .data("hdnimovel", imovel.getIdImovelSite())
			        .post();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			isAtivo = false;
		}
    	
        Element nameElement = detailDocument.selectFirst("h5");
        if (nameElement != null) {
        	if(nameElement.text().contains("O imóvel que você procura")) {
        		System.out.println("----------");
        		System.out.println("DESATIVADO");
        		System.out.println("----------");
                isAtivo = false;
        	} else {
        		System.out.println("ATIVO - Nome: " + nameElement.text());
                isAtivo = true;
        	}
            
        }
        
        imovel.setAtivo(isAtivo);
        imovelRepository.save(imovel);
    }
    
    public void obterInformacoesMatriculaPDF() {
    	List<Imovel> listaImoveis = imovelRepository.findAll();
    	for (Imovel imovel : listaImoveis) {
    		if(imovel.getAreaTotal() == 0) {
    			ocrPDFReader.imovelInPDF(imovel);
        		imovelRepository.save(imovel);
    		}
		}
    }
    
    public void verificarItensAtivos() {
    	List<Imovel> listaImoveis = imovelRepository.findAll();
    	for (Imovel imovel : listaImoveis) {
    		if(imovel.isAtivo()) {
    			checkImovelAtivo(imovel);
    		}
		}
    }

}
