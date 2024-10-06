CREATE TABLE estado(
id int primary key auto_increment,
uf varchar(2) not null,
ativo tinyint(1) not null default 1
);

INSERT INTO estado(uf) VALUES
('AC'),
('AL'),
('AP'),
('AM'),
('BA'),
('CE'),
('DF'),
('ES'),
('GO'),
('MA'),
('MT'),
('MS'),
('MG'),
('PA'),
('PB'),
('PR'),
('PE'),
('PI'),
('RJ'),
('RN'),
('RS'),
('RO'),
('RR'),
('SC'),
('SP'),
('SE'),
('TO');

CREATE TABLE cidade(
id int primary key auto_increment,
id_estado int not null,
codigo int not null,
descricao varchar(100) not null,
ativo tinyint(1) not null default 1
);

INSERT INTO cidade(id_estado, codigo, descricao) VALUES
(7, 1809, 'BRASILIA'),
(7, 1819, 'CANDANGOLANDIA'),
(7, 1822, 'GAMA'),
(7, 1826, 'NUCLEO BANDEIRANTE'),
(7, 1831, 'SAMAMBAIA'),
(7, 1835, 'TAGUATINGA');

CREATE TABLE modalidade_venda(
id int primary key auto_increment,
descricao varchar(100) not null,
definicao text,
ativo tinyint(1) not null default 1
);

INSERT INTO modalidade_venda (descricao) VALUES
('1º Leilão'),
('2º Leilão'),
('Concorrência Pública'),
('Leilão - Edital Único'),
('Licitação Aberta'),
('Venda Direta FAR'),
('Venda Direta Online'),
('Venda Online');

CREATE TABLE tipo_imovel(
id int primary key auto_increment,
descricao varchar(100) not null,
ativo tinyint(1) not null default 1
);

INSERT INTO tipo_imovel (descricao) VALUES
('Casa'),
('Apartamento'),
('Loja'),
('Sala'),
('Terreno'),
('Outros'),
('Indiferente'),
('Comercial');

CREATE TABLE imovel (
id int primary key auto_increment,
id_imovel_site varchar(100),
id_cidade int not null,
id_tipo_imovel int not null,
id_modalidade_venda int,
nome varchar(255) not null,
quartos int not null default 0,
vagas int not null default 0,
valor_avaliacao double not null default 0.0,
valor_min_leilao_1 double not null default 0.0,
valor_min_leilao_2 double not null default 0.0,
valor_limite_arremate double not null default 0.0,
valor_arrematado double not null default 0.0,
valor_venda_estimado double not null default 0.0,
valor_venda_final double not null default 0.0,
valor_condominio double not null default 0.0,
valor_iptu double not null default 0.0,
valor_debito_condominio double not null default 0.0,
valor_debito_iptu double not null default 0.0,
valor_lucro_bruto double not null default 0.0,
valor_lucro_bruto_estimado double not null default 0.0,
valor_lucro_liquido double not null default 0.0,
valor_lucro_liquido_estimado double not null default 0.0,
valor_reforma_estimado double not null default 0.0,
valor_leiloeiro_estimado double not null default 0.0,
valor_itbi_estimado double not null default 0.0,
valor_corretor_vendas_estimado double not null default 0.0,
valor_custos_adicionais_estimado double not null default 0.0,
desconto double,
area_privativa double not null default 0.0,
area_uso double not null default 0.0,
area_total double not null default 0.0,
numero_imovel varchar(50),
matricula VARCHAR(50) not null default 0,
comarca varchar(100),
oficio varchar (50),
inscricao_imobiliaria varchar(100),
averbacao varchar(50),
edital varchar(255),
numero_item int,
leiloeiro varchar(255),
endereco text,
descricao text,
comentarios text,
observacoes text,
acao_judicial varchar(100),
data_cadastro timestamp not null default current_timestamp,
data_atualizacao timestamp,
data_leilao_1 timestamp,
data_leilao_2 timestamp,
link_edital text,
link_matricula text,
link_leilao text,
link_maps text,
link_jusbrasil text,
link_wimoveis text,
precisa_autorizacao_codhab tinyint(1) not null default 0,
permite_financiamento tinyint(1) not null default 0,
permite_fgts tinyint(1) not null default 0,
permite_consorcio tinyint(1) not null default 0,
permite_parcelamento tinyint(1) not null default 0,
cpf VARCHAR(14),
ativo tinyint(1) not null default 1
);

CREATE TABLE imagens (
id int primary key auto_increment,
id_imovel int not null,
link_imagem text not null,
ativo tinyint(1) not null default 1
);