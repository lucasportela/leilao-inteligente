package lucassoftwares.leilao_inteligente;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LeilaoInteligenteApplicationTests {

	@Test
	void contextLoads() {
		String text = "Valor de avaliação: R$ 150.000,00\n"
				+ "Valor mínimo de venda 1º Leilão: R$ 174.299,99\n"
				+ "Valor mínimo de venda 2º Leilão: R$ 120.967,86";
		
		String valorAvaliacao = text.substring(text.indexOf("$") + 2, text.indexOf("Valor mínimo")).trim().replaceAll("[^\\d.]", "");
	}

}
