package lucassoftwares.leilaointeligente.repository;

import lucassoftwares.leilaointeligente.model.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Integer> {

    List<Cidade> findByIdEstado(int idEstado);

    Cidade findByCodigo(int codigo);
}
