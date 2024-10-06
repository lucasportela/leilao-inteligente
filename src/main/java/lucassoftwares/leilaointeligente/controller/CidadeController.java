package lucassoftwares.leilaointeligente.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import lucassoftwares.leilaointeligente.model.Cidade;
import lucassoftwares.leilaointeligente.service.CidadeService;

@RestController
public class CidadeController {
	
	private CidadeService cidadeService;
	
	@Autowired
	public CidadeController(CidadeService cs) {
		this.cidadeService = cs;
	}
	
	public List<Cidade> getAll() {
		return cidadeService.getAll();
	}
	
	public Optional<Cidade> getCidade(Long id) {
		return cidadeService.getCidadeById(id);
	}

}
