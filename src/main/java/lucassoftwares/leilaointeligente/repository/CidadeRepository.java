package lucassoftwares.leilaointeligente.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lucassoftwares.leilaointeligente.model.Cidade;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {

    List<Cidade> findByIdEstado(int idEstado);

    Cidade findByCodigo(int codigo);
}
