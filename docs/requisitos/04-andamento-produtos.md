# Registro de andamento — Módulo de Produtos

Este documento registra o avanço técnico do módulo. Ele complementa o histórico do Git, mas não substitui protocolos formais de validação, revisão da Qualidade ou evidências de uso em produção.

## Situação consolidada em 3 de setembro de 2026

| Componente | Situação |
|---|---|
| `TipoProduto` | Criado com os três tipos iniciais |
| `Produto` | Entidade JPA criada; normaliza código, valida campos obrigatórios, inicia ativa e pode ser inativada |
| Migração V2 | Tabela `produto` criada com código único |
| `ProdutoRepository` | Acesso JPA criado e consulta de existência por código adicionada |
| `ProdutoService` | Cadastro com verificação de duplicidade e listagem por nome implementados |
| `ProdutoForm` | Criado com campos, getters, setters e validações de entrada |
| `ProdutoController` | Rotas de listagem, abertura do formulário e cadastro implementadas |
| `ProdutoTest` | Criação como ativo, inativação, normalização e campos obrigatórios testados |
| `ProdutoServiceTest` | Cadastro permitido, duplicidade recusada e listagem ordenada testados |
| `ProdutoControllerTest` | Cinco cenários das rotas web testados com MockMvc |
| Templates de Produtos | Telas responsivas de listagem e cadastro criadas e renderizadas em testes de integração |
| Perfil de testes | Banco H2 em memória separado do banco de desenvolvimento |

## Evidências de desenvolvimento

| Data | Alteração | Requisitos relacionados | Evidência |
|---|---|---|---|
| Até 1º/09/2026 | Estrutura inicial da entidade e persistência | REQ-PRO-001 a REQ-PRO-005 | Entidade, enum, migração V2 e testes da entidade |
| 02/09/2026 | Serviço de cadastro e verificação de código duplicado | REQ-PRO-001, REQ-PRO-002 e REQ-PRO-008 | Testes `deveCadastrarProdutoQuandoCodigoNaoExiste` e `deveRecusarCadastroQuandoCodigoJaExiste` |
| 02/09/2026 | Listagem solicitada em ordem de nome | REQ-PRO-006 | Teste `deveListarProdutosOrdenadosPorNome` |
| 02/09/2026 | Normalização do código no construtor | RN-PRO-002 e REQ-PRO-011 | Teste `deveNormalizarCodigoDoProduto` |
| 02/09/2026 | Validação de código, nome e tipo obrigatórios | REQ-PRO-003 | Testes `deveRecusarCodigoEmBranco`, `deveRecusarNomeEmBranco` e `deveRecusarTipoNulo` |
| 02/09/2026 | Objeto de entrada para a futura tela | REQ-PRO-001 e REQ-PRO-003 | Classe `ProdutoForm` compilada com validações Jakarta |
| 03/09/2026 | Controlador web de Produtos | REQ-PRO-001, REQ-PRO-003, REQ-PRO-006 e REQ-PRO-008 | Cinco testes de rotas, validação, cadastro e duplicidade |
| 03/09/2026 | Telas de listagem e cadastro de Produtos | REQ-PRO-001, REQ-PRO-003, REQ-PRO-004, REQ-PRO-005 e REQ-PRO-006 | Testes de integração renderizam lista e formulário com Thymeleaf |

## Próxima atividade

Implementar consulta de um produto pelo identificador.

Depois dessa regra, a sequência planejada é:

1. implementar consulta de um produto;
2. implementar edição e inativação pela interface;
3. executar os testes completos e revisar a documentação antes do commit.

## Indicadores de acompanhamento

- projeto completo: 15% estimados;
- módulo Produtos: 90% estimados;
- testes existentes no código: dezoito.

Os percentuais são indicadores motivacionais de planejamento. A conclusão real será determinada pelos critérios de aceitação e pela revisão técnica.
