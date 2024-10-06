package lucassoftwares.leilaointeligente.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import lucassoftwares.leilaointeligente.model.ModalidadeVenda;
import lucassoftwares.leilaointeligente.service.ModalidadeVendaService;

@RestController
public class ModalidadeVendaController {
	
	private ModalidadeVendaService modalidadeVendaService;
	
	@Autowired
	public ModalidadeVendaController(ModalidadeVendaService mvs) {
		this.modalidadeVendaService = mvs;
	}
	
	public List<ModalidadeVenda> getAll() {
		return modalidadeVendaService.getAll();
	}
	
	public Optional<ModalidadeVenda> getModalidadeVenda(Long id) {
		return modalidadeVendaService.getModalidadeVenda(id);
	}

}
