package lucassoftwares.leilaointeligente.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lucassoftwares.leilaointeligente.model.Estado;
import lucassoftwares.leilaointeligente.repository.EstadoRepository;

@Service
public class EstadoService {

    @Autowired
    private EstadoRepository estadoRepository;

    public List<Estado> getAll() {
        return estadoRepository.findAll();
    }
    
    public Optional<Estado> getEstadoById(Long id) {
        return estadoRepository.findById(id);
    }
}
