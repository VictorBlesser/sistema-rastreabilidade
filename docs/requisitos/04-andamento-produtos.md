# Registro de andamento — Módulo de Produtos

Este documento registra o avanço técnico do módulo. Ele complementa o histórico do Git, mas não substitui protocolos formais de validação, revisão da Qualidade ou evidências de uso em produção.

## Situação consolidada em 3 de setembro de 2026

| Componente | Situação |
|---|---|
| `TipoProduto` | Criado com os três tipos iniciais |
| `Produto` | Entidade JPA criada; normaliza código, valida campos obrigatórios, inicia ativa e pode ser inativada |
| Migração V2 | Tabela `produto` criada com código único |
| `ProdutoRepository` | Acesso JPA criado e consulta de existência por código adicionada |
| `ProdutoService` | Cadastro, duplicidade, listagem por nome, consulta por identificador e inativação implementados |
| `ProdutoForm` | Criado com campos, getters, setters e validações de entrada |
| `ProdutoController` | Rotas de listagem, abertura do formulário, cadastro, consulta e inativação implementadas |
| `ProdutoTest` | Criação como ativo, inativação, normalização e campos obrigatórios testados |
| `ProdutoServiceTest` | Cadastro, duplicidade, listagem, consulta por identificador e inativação testados |
| `ProdutoControllerTest` | Nove cenários das rotas web testados com MockMvc |
| Templates de Produtos | Telas responsivas de listagem, cadastro e detalhes criadas e renderizadas em testes de integração |
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
| 03/09/2026 | Consulta de produto por identificador no serviço | REQ-PRO-007 | Testes de produto encontrado e identificador inexistente |
| 03/09/2026 | Ambiente de desenvolvimento atualizado | Requisito técnico | Projeto compilado e 20 testes executados com JDK 25.0.4.1 |
| 03/09/2026 | Rota de consulta de produto por identificador | REQ-PRO-007 | Testes `deveExibirDetalhesDoProduto` e `deveRedirecionarQuandoProdutoNaoExiste` |
| 03/09/2026 | Tela de detalhes integrada ao banco de testes | REQ-PRO-007 | Teste `deveRenderizarDetalhesDeProduto` executado com JDK 25.0.4.1 |
| 03/09/2026 | Inativação de produto pelo serviço | RN-PRO-003 e REQ-PRO-009 | Teste `deveInativarProdutoPeloServico` |
| 03/09/2026 | Rota web de inativação | RN-PRO-003 e REQ-PRO-009 | Dois testes de controlador para sucesso e produto inexistente |
| 03/09/2026 | Inativação concluída na interface | RN-PRO-003 e REQ-PRO-009 | Botão protegido por CSRF e teste integrado confirma `ativo = false` no banco |
| 03/09/2026 | Primeira versão do módulo Produtos concluída | REQ-PRO-001 a REQ-PRO-011, exceto requisito temporal REQ-PRO-012 | Suíte completa com 27 testes, sem falhas ou erros, no JDK 25.0.4.1 |

## Próxima atividade

Revisar a formatação do código e preparar o commit da primeira versão do módulo Produtos. O requisito REQ-PRO-012 e a edição de produtos permanecem planejados para uma evolução posterior.

Depois dessa regra, a sequência planejada é:

1. implementar edição e inativação pela interface;
2. executar os testes completos e revisar a documentação antes do commit.

## Indicadores de acompanhamento

- projeto completo: 20% estimados;
- módulo Produtos: 100% da primeira versão;
- testes existentes no código: vinte e sete.

Os percentuais são indicadores motivacionais de planejamento. A conclusão real será determinada pelos critérios de aceitação e pela revisão técnica.
