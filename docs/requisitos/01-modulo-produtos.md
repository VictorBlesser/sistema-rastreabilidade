# Requisitos — Produtos

Revisão 16/09/2026. Atualiza o escopo básico documentado em 03/09/2026. Fonte: [pacote produto](../../src/main/java/com/portfolio/rastreabilidade/produto/), migrações V2/V6 e testes do módulo.

## Objetivo e escopo

Cadastrar produtos que serão referenciados por lotes e movimentações. Cadastro, listagem, detalhes e inativação estão implementados. Edição geral/técnica, cadastro de fabricante/fornecedor, anexos e auditoria de alterações ainda não estão implementados.

## Requisitos preservados e ampliados

| ID | Regra | Estado |
|---|---|---|
| REQ-PRO-001 | Cadastrar produto | Implementado |
| REQ-PRO-002 | Código interno único | Implementado |
| REQ-PRO-003 | Código, nome e tipo obrigatórios | Implementado |
| REQ-PRO-004 | Indicar controle de lote | Implementado |
| REQ-PRO-005 | Indicar controle de validade | Implementado |
| REQ-PRO-006 | Listar produtos | Implementado |
| REQ-PRO-007 | Consultar por ID | Implementado |
| REQ-PRO-008 | Rejeitar código duplicado | Implementado no serviço e banco |
| REQ-PRO-009 | Inativar produto | Implementado |
| REQ-PRO-010 | Preservar cadastro utilizado | Sem rota de exclusão; proteção abrangente de histórico permanece requisito |
| REQ-PRO-011 | Remover espaços externos e converter código para maiúsculas | Implementado |
| REQ-PRO-012 | Registrar criação/atualização do produto | Pendente: campos temporais não existem na entidade atual |
| REQ-PRO-013 | Informar unidade e permitir/proibir fracionamento | Implementado na V6 e entidade |
| REQ-PRO-014 | Separar consulta, cadastro e inativação por permissão | Implementado nas rotas e interface |

## Dicionário atual

| Campo | Tipo Java | Regra |
|---|---|---|
| id | Long | Gerado pelo banco |
| codigo | String | Até 50, obrigatório, normalizado e único |
| nome | String | Até 150, obrigatório |
| descricao | String | Até 500, opcional |
| tipo | TipoProduto | MEDICAMENTO, PRODUTO_MEDICO ou DIAGNOSTICO_IN_VITRO |
| controlaLote / controlaValidade | boolean | Controles utilizados pelos módulos operacionais |
| fracionavel | boolean | false exige quantidade inteira |
| unidadeMedida | UnidadeMedida | UN, CX, PCT, KG, G, L, ML ou M |
| ativo | boolean | Novo produto inicia ativo |

Fracionável/não fracionável é uma classificação da quantidade, independente do tipo de produto. Não existe conversão automática entre caixa, unidade ou outras unidades. Quantidade usa BigDecimal, deve ser positiva; precisão/escala dos itens são também validadas pelos módulos de movimentação.

## Autorizações e aceite

GET de lista/detalhes exige `PRODUTO_VISUALIZAR`; cadastro exige `PRODUTO_CADASTRAR`; inativação exige `PRODUTO_INATIVAR`. Permissões técnicas catalogadas não liberam automaticamente o cadastro geral. Usuário somente de consulta não deve ver os controles de escrita nem conseguir executar POST direto.

Critérios: código ` cat-001 ` vira `CAT-001`; repetição é recusada; inativação preserva ID; quantidade 1,5 é recusada para produto não fracionável; a unidade permanece associada ao produto.

## Evidências

`ProdutoTest`, `ProdutoQuantidadeTest`, `ProdutoServiceTest`, `ProdutoControllerTest`, `ProdutoPermissaoWebTest`, `BotoesCadastroWebTest` e `SistemaRastreabilidadeApplicationTests`. Contagens e execução consolidada estão em [validação](../10-validacao-e-evidencias.md).

As referências regulatórias registradas na versão inicial são contexto histórico. Esta revisão técnica não verifica sua aplicabilidade ou vigência, nem declara o módulo validado para produção.
