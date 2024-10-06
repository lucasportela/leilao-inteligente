package lucassoftwares.leilaointeligente.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lucassoftwares.leilaointeligente.model.ModalidadeVenda;

@Repository
public interface ModalidadeVendaRepository extends JpaRepository<ModalidadeVenda, Integer> {

	ModalidadeVenda findByDescricao(String descricao);
	
	List<ModalidadeVenda> findByDescricaoContainingIgnoreCase(String descricao);
	
}
