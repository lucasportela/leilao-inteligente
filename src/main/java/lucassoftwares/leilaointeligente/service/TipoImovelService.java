package lucassoftwares.leilaointeligente.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lucassoftwares.leilaointeligente.model.TipoImovel;
import lucassoftwares.leilaointeligente.repository.TipoImovelRepository;

@Service
public class TipoImovelService {

	@Autowired
	private TipoImovelRepository tipoImovelRepository;
	
	public Optional<TipoImovel> getTipoImovel(Long id) {
		return tipoImovelRepository.findById(id);
	}
	
	public List<TipoImovel> getAll() {
		return tipoImovelRepository.findAll();
	}
}
