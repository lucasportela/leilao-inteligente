package lucassoftwares.leilaointeligente.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import lucassoftwares.leilaointeligente.model.ModalidadeVenda;
import lucassoftwares.leilaointeligente.repository.ModalidadeVendaRepository;

@Service
public class ModalidadeVendaService {
	
	@Autowired
	private ModalidadeVendaRepository modalidadeVendaRepository;
	
	public List<ModalidadeVenda> getAll() {
		return modalidadeVendaRepository.findAll();
	}
	
	public Optional<ModalidadeVenda> getModalidadeVenda(@PathVariable Long id) {
		return modalidadeVendaRepository.findById(id);
	}

}
