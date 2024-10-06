package lucassoftwares.leilaointeligente.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import lucassoftwares.leilaointeligente.model.TipoImovel;

public interface TipoImovelRepository extends JpaRepository<TipoImovel, Long> {
	
    TipoImovel findByDescricao(String descricao);
    
}
