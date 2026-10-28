-- Dados de demonstração do Axis: seis ambientes com foto e comodidades.
-- Rode depois que a aplicação subir pela primeira vez, quando o Hibernate
-- (ddl-auto=update) já criou as tabelas.

-- Sem isso, o cliente do container conversa em latin1 e grava os acentos errados.
SET NAMES utf8mb4;

INSERT INTO ambientes (nome, descricao, imagem_nome, capacidade, valor_hora, ativo, data_criacao, data_atualizacao)
VALUES ('Coworking Aberto', 'Mesas compartilhadas junto à fachada de vidro, com plantas entre os postos de trabalho.', 'img_15.jpeg', 20, 15.00, TRUE, NOW(), NOW());
SET @ambiente = LAST_INSERT_ID();
INSERT INTO comodidades_ambiente (ambiente_id, comodidade) VALUES (@ambiente, 'AR_CONDICIONADO'), (@ambiente, 'CAFE');

INSERT INTO ambientes (nome, descricao, imagem_nome, capacidade, valor_hora, ativo, data_criacao, data_atualizacao)
VALUES ('Sala Privativa', 'Sala fechada para equipes pequenas, com mesa única e cadeiras ergonômicas.', 'img_18.jpeg', 6, 60.00, TRUE, NOW(), NOW());
SET @ambiente = LAST_INSERT_ID();
INSERT INTO comodidades_ambiente (ambiente_id, comodidade) VALUES (@ambiente, 'AR_CONDICIONADO'), (@ambiente, 'QUADRO_BRANCO'), (@ambiente, 'TV');

INSERT INTO ambientes (nome, descricao, imagem_nome, capacidade, valor_hora, ativo, data_criacao, data_atualizacao)
VALUES ('Lounge', 'Sofás e pufes para reuniões informais e conversas com clientes.', 'img_09.jpeg', 12, 30.00, TRUE, NOW(), NOW());
SET @ambiente = LAST_INSERT_ID();
INSERT INTO comodidades_ambiente (ambiente_id, comodidade) VALUES (@ambiente, 'CAFE'), (@ambiente, 'TV');

INSERT INTO ambientes (nome, descricao, imagem_nome, capacidade, valor_hora, ativo, data_criacao, data_atualizacao)
VALUES ('Bancada da Janela', 'Bancada alta de frente para a janela, para quem precisa de poucas horas.', 'img_14.jpeg', 4, 10.00, TRUE, NOW(), NOW());
SET @ambiente = LAST_INSERT_ID();
INSERT INTO comodidades_ambiente (ambiente_id, comodidade) VALUES (@ambiente, 'CAFE');

INSERT INTO ambientes (nome, descricao, imagem_nome, capacidade, valor_hora, ativo, data_criacao, data_atualizacao)
VALUES ('Salão de Eventos', 'Espaço amplo para workshops, palestras e confraternizações.', 'img_20.jpeg', 60, 180.00, TRUE, NOW(), NOW());
SET @ambiente = LAST_INSERT_ID();
INSERT INTO comodidades_ambiente (ambiente_id, comodidade) VALUES (@ambiente, 'PROJETOR'), (@ambiente, 'AR_CONDICIONADO'), (@ambiente, 'CAFE');

INSERT INTO ambientes (nome, descricao, imagem_nome, capacidade, valor_hora, ativo, data_criacao, data_atualizacao)
VALUES ('Terraço', 'Área externa com sofás, para eventos ao ar livre.', 'img_21.jpeg', 25, 90.00, TRUE, NOW(), NOW());
SET @ambiente = LAST_INSERT_ID();
INSERT INTO comodidades_ambiente (ambiente_id, comodidade) VALUES (@ambiente, 'CAFE');
