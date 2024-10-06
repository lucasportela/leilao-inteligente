package lucassoftwares.leilaointeligente.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import lucassoftwares.leilaointeligente.model.Estado;
import lucassoftwares.leilaointeligente.service.EstadoService;

@RestController
public class EstadoController {
	
	private EstadoService estadoService;
	
	@Autowired
	public EstadoController(EstadoService es) {
		this.estadoService = es;
	}
	
	public List<Estado> getAll() {
		return estadoService.getAll();
	}
	
	public Optional<Estado> getEstado(Long id) {
		return estadoService.getEstadoById(id);
	}

}
