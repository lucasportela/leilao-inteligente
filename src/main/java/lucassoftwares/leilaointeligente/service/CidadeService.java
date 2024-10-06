package lucassoftwares.leilaointeligente.service;

import lucassoftwares.leilaointeligente.model.Cidade;
import lucassoftwares.leilaointeligente.repository.CidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CidadeService {

    @Autowired
    private CidadeRepository cidadeRepository;

    public List<Cidade> getCidadesByEstado(int idEstado) {
        return cidadeRepository.findByIdEstado(idEstado);
    }

    public Cidade getCidadeByCodigo(int codigo) {
        return cidadeRepository.findByCodigo(codigo);
    }
}
