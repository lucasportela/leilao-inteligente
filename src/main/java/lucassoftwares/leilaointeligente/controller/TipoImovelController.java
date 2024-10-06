package lucassoftwares.leilaointeligente.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lucassoftwares.leilaointeligente.model.TipoImovel;
import lucassoftwares.leilaointeligente.service.TipoImovelService;

@RestController
public class TipoImovelController {

	private TipoImovelService tipoImovelService;
	
	@Autowired
	public TipoImovelController(TipoImovelService tis) {
		this.tipoImovelService = tis;
	}
	
	@GetMapping("/tipo-imovel")
	public List<TipoImovel> getAll() {
		return tipoImovelService.getAll();
	}
	
	@GetMapping("/tipo-imovel/{id}")
	public TipoImovel get(@PathVariable Long id) {
		return tipoImovelService.getTipoImovel(id).orElseThrow();
	}
	
}