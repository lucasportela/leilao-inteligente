package lucassoftwares.leilaointeligente.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lucassoftwares.leilaointeligente.model.Imovel;

@Repository
public interface ImovelRepository extends JpaRepository<Imovel, Integer> {

	Imovel findByIdImovelSite(String idImovelSite);
	List<Imovel> findAllByAtivo(boolean ativo);
}