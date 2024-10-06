package lucassoftwares.leilaointeligente.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "imovel")
public class Imovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "id_imovel_site")
    private String idImovelSite;
    @Column(name = "id_cidade")
    private long idCidade;
    @Column(name = "id_tipo_imovel")
    private long idTipoImovel;
    @Column(name = "id_modalidade_venda")
    private long idModalidadeVenda;
    @Column(name = "nome")
    private String nome;
    @Column(name = "quartos")
    private int quartos;
    @Column(name = "vagas")
    private int vagas;
    @Column(name = "valor_avaliacao")
    private double valorAvaliacao;
    @Column(name = "valor_min_leilao_1")
    private double valorMinLeilao1;
    @Column(name = "valor_min_leilao_2")
    private double valorMinLeilao2;
    @Column(name = "valor_limite_arremate")
    private double valorLimiteArremate;
    @Column(name = "valor_arrematado")
    private double valorArrematado;
    @Column(name = "valor_venda_estimado")
    private double valorVendaEstimado;
    @Column(name = "valor_venda_final")
    private double valorVendaFinal;
    @Column(name = "valor_condominio")
    private double valorCondominio;
    @Column(name = "valor_iptu")
    private double valorIptu;
    @Column(name = "valor_debito_condominio")
    private double valorDebitoCondominio;
    @Column(name = "valor_debito_iptu")
    private double valorDebitoIptu;
    @Column(name = "valor_lucro_bruto")
    private double valorLucroBruto;
    @Column(name = "valor_lucro_liquido")
    private double valorLucroLiquido;
    @Column(name = "desconto")
    private double desconto;
    @Column(name = "area_privativa")
    private double areaPrivativa;
    @Column(name = "area_uso")
    private double areaUso;
    @Column(name = "area_total")
    private double areaTotal;
    @Column(name = "numero_imovel")
    private String numeroImovel;
    @Column(name = "matricula")
    private String matricula;
    @Column(name = "comarca")
    private String comarca;
    @Column(name = "oficio")
    private String oficio;
    @Column(name = "inscricao_imobiliaria")
    private String inscricaoImobiliaria;
    @Column(name = "averbacao")
    private String averbacao;
    @Column(name = "edital")
    private String edital;
    @Column(name = "numero_item")
    private int numeroItem;
    @Column(name = "leiloeiro")
    private String leiloeiro;
    @Column(name = "endereco")
    private String endereco;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "comentarios")
    private String comentarios;
    @Column(name = "observacoes")
    private String observacoes;
    @Column(name = "acao_judicial")
    private String acaoJudicial;
    @Column(name = "data_cadastro")
    private Timestamp dataCadastro;
    @Column(name = "data_atualizacao")
    private Timestamp dataAtualizacao;
    @Column(name = "data_leilao_1")
    private Timestamp dataLeilao1;
    @Column(name = "data_leilao_2")
    private Timestamp dataLeilao2;
    @Column(name = "link_edital")
    private String linkEdital;
    @Column(name = "link_matricula")
    private String linkMatricula;
    @Column(name = "link_leilao")
    private String linkLeilao;
    @Column(name = "link_maps")
    private String linkMaps;
    @Column(name = "link_jusbrasil")
    private String linkJusbrasil;
    @Column(name = "link_wimoveis")
    private String linkWimoveis;
    @Column(name = "precisa_autorizacao_codhab")
    private boolean precisaAutorizacaoCodhab;
    @Column(name = "permite_financiamento")
    private boolean permiteFinanciamento;
    @Column(name = "permite_fgts")
    private boolean permiteFgts;
    @Column(name = "permite_consorcio")
    private boolean permiteConsorcio;
    @Column(name = "permite_parcelamento")
    private boolean permiteParcelamento;
    @Column(name = "cpf")
    private String cpf;
    @Column(name = "ativo")
    private boolean ativo;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIdImovelSite() {
		return idImovelSite;
	}

	public void setIdImovelSite(String idImovelSite) {
		this.idImovelSite = idImovelSite;
	}

	public long getIdCidade() {
		return idCidade;
	}

	public void setIdCidade(long idCidade) {
		this.idCidade = idCidade;
	}

	public long getIdTipoImovel() {
		return idTipoImovel;
	}

	public void setIdTipoImovel(long idTipoImovel) {
		this.idTipoImovel = idTipoImovel;
	}

	public long getIdModalidadeVenda() {
		return idModalidadeVenda;
	}

	public void setIdModalidadeVenda(long idModalidadeVenda) {
		this.idModalidadeVenda = idModalidadeVenda;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getQuartos() {
		return quartos;
	}

	public void setQuartos(int quartos) {
		this.quartos = quartos;
	}

	public int getVagas() {
		return vagas;
	}

	public void setVagas(int vagas) {
		this.vagas = vagas;
	}

	public double getValorAvaliacao() {
		return valorAvaliacao;
	}

	public void setValorAvaliacao(double valorAvaliacao) {
		this.valorAvaliacao = valorAvaliacao;
	}

	public double getValorMinLeilao1() {
		return valorMinLeilao1;
	}

	public void setValorMinLeilao1(double valorMinLeilao1) {
		this.valorMinLeilao1 = valorMinLeilao1;
	}

	public double getValorMinLeilao2() {
		return valorMinLeilao2;
	}

	public void setValorMinLeilao2(double valorMinLeilao2) {
		this.valorMinLeilao2 = valorMinLeilao2;
	}

	public double getValorLimiteArremate() {
		return valorLimiteArremate;
	}

	public void setValorLimiteArremate(double valorLimiteArremate) {
		this.valorLimiteArremate = valorLimiteArremate;
	}

	public double getValorArrematado() {
		return valorArrematado;
	}

	public void setValorArrematado(double valorArrematado) {
		this.valorArrematado = valorArrematado;
	}

	public double getValorVendaEstimado() {
		return valorVendaEstimado;
	}

	public void setValorVendaEstimado(double valorVendaEstimado) {
		this.valorVendaEstimado = valorVendaEstimado;
	}

	public double getValorVendaFinal() {
		return valorVendaFinal;
	}

	public void setValorVendaFinal(double valorVendaFinal) {
		this.valorVendaFinal = valorVendaFinal;
	}

	public double getValorCondominio() {
		return valorCondominio;
	}

	public void setValorCondominio(double valorCondominio) {
		this.valorCondominio = valorCondominio;
	}

	public double getValorIptu() {
		return valorIptu;
	}

	public void setValorIptu(double valorIptu) {
		this.valorIptu = valorIptu;
	}

	public double getValorDebitoCondominio() {
		return valorDebitoCondominio;
	}

	public void setValorDebitoCondominio(double valorDebitoCondominio) {
		this.valorDebitoCondominio = valorDebitoCondominio;
	}

	public double getValorDebitoIptu() {
		return valorDebitoIptu;
	}

	public void setValorDebitoIptu(double valorDebitoIptu) {
		this.valorDebitoIptu = valorDebitoIptu;
	}

	public double getValorLucroBruto() {
		return valorLucroBruto;
	}

	public void setValorLucroBruto(double valorLucroBruto) {
		this.valorLucroBruto = valorLucroBruto;
	}

	public double getValorLucroLiquido() {
		return valorLucroLiquido;
	}

	public void setValorLucroLiquido(double valorLucroLiquido) {
		this.valorLucroLiquido = valorLucroLiquido;
	}

	public double getDesconto() {
		return desconto;
	}

	public void setDesconto(double desconto) {
		this.desconto = desconto;
	}

	public double getAreaPrivativa() {
		return areaPrivativa;
	}

	public void setAreaPrivativa(double areaPrivativa) {
		this.areaPrivativa = areaPrivativa;
	}

	public double getAreaUso() {
		return areaUso;
	}

	public void setAreaUso(double areaUso) {
		this.areaUso = areaUso;
	}

	public double getAreaTotal() {
		return areaTotal;
	}

	public void setAreaTotal(double areaTotal) {
		this.areaTotal = areaTotal;
	}

	public String getNumeroImovel() {
		return numeroImovel;
	}

	public void setNumeroImovel(String numeroImovel) {
		this.numeroImovel = numeroImovel;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public String getComarca() {
		return comarca;
	}

	public void setComarca(String comarca) {
		this.comarca = comarca;
	}

	public String getOficio() {
		return oficio;
	}

	public void setOficio(String oficio) {
		this.oficio = oficio;
	}

	public String getInscricaoImobiliaria() {
		return inscricaoImobiliaria;
	}

	public void setInscricaoImobiliaria(String inscricaoImobiliaria) {
		this.inscricaoImobiliaria = inscricaoImobiliaria;
	}

	public String getAverbacao() {
		return averbacao;
	}

	public void setAverbacao(String averbacao) {
		this.averbacao = averbacao;
	}

	public String getEdital() {
		return edital;
	}

	public void setEdital(String edital) {
		this.edital = edital;
	}

	public int getNumeroItem() {
		return numeroItem;
	}

	public void setNumeroItem(int numeroItem) {
		this.numeroItem = numeroItem;
	}

	public String getLeiloeiro() {
		return leiloeiro;
	}

	public void setLeiloeiro(String leiloeiro) {
		this.leiloeiro = leiloeiro;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getComentarios() {
		return comentarios;
	}

	public void setComentarios(String comentarios) {
		this.comentarios = comentarios;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public String getAcaoJudicial() {
		return acaoJudicial;
	}

	public void setAcaoJudicial(String acaoJudicial) {
		this.acaoJudicial = acaoJudicial;
	}

	public Timestamp getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Timestamp dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Timestamp getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Timestamp dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public Timestamp getDataLeilao1() {
		return dataLeilao1;
	}

	public void setDataLeilao1(Timestamp dataLeilao1) {
		this.dataLeilao1 = dataLeilao1;
	}

	public Timestamp getDataLeilao2() {
		return dataLeilao2;
	}

	public void setDataLeilao2(Timestamp dataLeilao2) {
		this.dataLeilao2 = dataLeilao2;
	}

	public String getLinkEdital() {
		return linkEdital;
	}

	public void setLinkEdital(String linkEdital) {
		this.linkEdital = linkEdital;
	}

	public String getLinkMatricula() {
		return linkMatricula;
	}

	public void setLinkMatricula(String linkMatricula) {
		this.linkMatricula = linkMatricula;
	}

	public String getLinkLeilao() {
		return linkLeilao;
	}

	public void setLinkLeilao(String linkLeilao) {
		this.linkLeilao = linkLeilao;
	}

	public String getLinkMaps() {
		return linkMaps;
	}

	public void setLinkMaps(String linkMaps) {
		this.linkMaps = linkMaps;
	}

	public String getLinkJusbrasil() {
		return linkJusbrasil;
	}

	public void setLinkJusbrasil(String linkJusbrasil) {
		this.linkJusbrasil = linkJusbrasil;
	}

	public String getLinkWimoveis() {
		return linkWimoveis;
	}

	public void setLinkWimoveis(String linkWimoveis) {
		this.linkWimoveis = linkWimoveis;
	}

	public boolean isPrecisaAutorizacaoCodhab() {
		return precisaAutorizacaoCodhab;
	}

	public void setPrecisaAutorizacaoCodhab(boolean precisaAutorizacaoCodhab) {
		this.precisaAutorizacaoCodhab = precisaAutorizacaoCodhab;
	}

	public boolean isPermiteFinanciamento() {
		return permiteFinanciamento;
	}

	public void setPermiteFinanciamento(boolean permiteFinanciamento) {
		this.permiteFinanciamento = permiteFinanciamento;
	}

	public boolean isPermiteFgts() {
		return permiteFgts;
	}

	public void setPermiteFgts(boolean permiteFgts) {
		this.permiteFgts = permiteFgts;
	}

	public boolean isPermiteConsorcio() {
		return permiteConsorcio;
	}

	public void setPermiteConsorcio(boolean permiteConsorcio) {
		this.permiteConsorcio = permiteConsorcio;
	}

	public boolean isPermiteParcelamento() {
		return permiteParcelamento;
	}

	public void setPermiteParcelamento(boolean permiteParcelamento) {
		this.permiteParcelamento = permiteParcelamento;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return "Imovel [id=" + id + ", " + (idImovelSite != null ? "idImovelSite=" + idImovelSite + ", " : "")
				+ "idCidade=" + idCidade + ", idTipoImovel=" + idTipoImovel + ", "
				+ (nome != null ? "nome=" + nome + ", " : "") + "quartos=" + quartos + ", vagas=" + vagas
				+ ", valorAvaliacao=" + valorAvaliacao + ", valorMinLeilao1=" + valorMinLeilao1 + ", valorMinLeilao2="
				+ valorMinLeilao2 + ", valorLimiteArremate=" + valorLimiteArremate + ", valorArrematado="
				+ valorArrematado + ", valorVendaEstimado=" + valorVendaEstimado + ", valorVendaFinal="
				+ valorVendaFinal + ", valorCondominio=" + valorCondominio + ", valorIptu=" + valorIptu
				+ ", valorDebitoCondominio=" + valorDebitoCondominio + ", valorDebitoIptu=" + valorDebitoIptu
				+ ", valorLucroBruto=" + valorLucroBruto + ", valorLucroLiquido=" + valorLucroLiquido + ", desconto="
				+ desconto + ", areaPrivativa=" + areaPrivativa + ", areaUso=" + areaUso + ", areaTotal=" + areaTotal
				+ ", " + (numeroImovel != null ? "numeroImovel=" + numeroImovel + ", " : "") + "matricula=" + matricula
				+ ", " + (comarca != null ? "comarca=" + comarca + ", " : "")
				+ (oficio != null ? "oficio=" + oficio + ", " : "")
				+ (inscricaoImobiliaria != null ? "inscricaoImobiliaria=" + inscricaoImobiliaria + ", " : "")
				+ (averbacao != null ? "averbacao=" + averbacao + ", " : "")
				+ (edital != null ? "edital=" + edital + ", " : "") + "numeroItem=" + numeroItem + ", "
				+ (leiloeiro != null ? "leiloeiro=" + leiloeiro + ", " : "")
				+ (endereco != null ? "endereco=" + endereco + ", " : "")
				+ (descricao != null ? "descricao=" + descricao + ", " : "")
				+ (comentarios != null ? "comentarios=" + comentarios + ", " : "")
				+ (observacoes != null ? "observacoes=" + observacoes + ", " : "")
				+ (acaoJudicial != null ? "acaoJudicial=" + acaoJudicial + ", " : "")
				+ (dataCadastro != null ? "dataCadastro=" + dataCadastro + ", " : "")
				+ (dataAtualizacao != null ? "dataAtualizacao=" + dataAtualizacao + ", " : "")
				+ (dataLeilao1 != null ? "dataLeilao1=" + dataLeilao1 + ", " : "")
				+ (dataLeilao2 != null ? "dataLeilao2=" + dataLeilao2 + ", " : "")
				+ (linkEdital != null ? "linkEdital=" + linkEdital + ", " : "")
				+ (linkMatricula != null ? "linkMatricula=" + linkMatricula + ", " : "")
				+ (linkLeilao != null ? "linkLeilao=" + linkLeilao + ", " : "")
				+ (linkMaps != null ? "linkMaps=" + linkMaps + ", " : "")
				+ (linkJusbrasil != null ? "linkJusbrasil=" + linkJusbrasil + ", " : "")
				+ (linkWimoveis != null ? "linkWimoveis=" + linkWimoveis + ", " : "") + "precisaAutorizacaoCodhab="
				+ precisaAutorizacaoCodhab + ", permiteFinanciamento=" + permiteFinanciamento + ", permiteFgts="
				+ permiteFgts + ", permiteConsorcio=" + permiteConsorcio + ", ativo=" + ativo + "]";
	}
    
}
