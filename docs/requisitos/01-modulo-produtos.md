# Requisitos — Cadastro de Produtos

## Estado da implementação em 2 de setembro de 2026

| Item | Estado | Evidência atual |
|---|---|---|
| Entidade `Produto` e enum `TipoProduto` | Implementado | Classes Java e mapeamento JPA |
| Tabela `produto` | Implementado | Migração `V2__criar_tabela_produto.sql` |
| Acesso ao banco | Implementado | `ProdutoRepository` estende `JpaRepository` |
| Cadastro pelo serviço | Implementado | `ProdutoService.cadastrar()` e teste unitário |
| Recusa de código duplicado | Implementado e testado | `existsByCodigoIgnoreCase` e `deveRecusarCadastroQuandoCodigoJaExiste` |
| Listagem por nome | Implementado e testado | `ProdutoService.listar()` e `deveListarProdutosOrdenadosPorNome` |
| Criação como ativo e inativação | Implementado e testado | Métodos e testes da entidade `Produto` |
| Normalização do código | Implementado e testado | Construtor de `Produto` e `deveNormalizarCodigoDoProduto` |
| Validação dos campos obrigatórios | Implementado e testado | Construtor e testes de código, nome e tipo |
| Objeto de entrada do formulário | Implementado | `ProdutoForm` com validações Jakarta |
| Telas e rotas web de Produtos | Pendente | `ProdutoController` e templates ainda não existem |
| Consulta, edição e inativação pelo navegador | Pendente | Depende das rotas e telas |

`Implementado` significa presente no código atual. Não significa que o módulo esteja validado ou liberado para produção.

## 1. Objetivo

Permitir o cadastro e a consulta dos produtos controlados pelo Sistema de Rastreabilidade. O cadastro será a base para os módulos futuros de lotes, recebimento, estoque, expedição, devolução e rastreabilidade.

## 2. Escopo da primeira versão

Esta versão deverá permitir:

- cadastrar um produto;
- listar os produtos cadastrados;
- consultar um produto;
- identificar seu tipo;
- informar se ele exige controle de lote;
- informar se ele exige controle de validade;
- inativar um produto sem apagar seu histórico.

Não fazem parte desta primeira versão:

- saldo de estoque;
- cadastro de lotes e números de série;
- fabricantes e fornecedores em tabelas próprias;
- documentos anexos;
- registro completo de auditoria;
- aprovação eletrônica do cadastro;
- exclusão física de produtos.

## 3. Usuários envolvidos

| Papel | Responsabilidade inicial |
|---|---|
| Cadastrador | Preencher os dados do produto |
| Qualidade | Revisar regras e campos regulatórios |
| Administrador | Inativar cadastros quando autorizado |
| Consultor | Pesquisar e visualizar produtos |

Os perfis serão implementados em um módulo posterior. Nesta etapa, eles servem para orientar as decisões de projeto.

## 4. Requisitos funcionais

| Código | Requisito | Prioridade |
|---|---|---:|
| REQ-PRO-001 | O sistema deve permitir cadastrar um produto | Alta |
| REQ-PRO-002 | Cada produto deve possuir um código interno único | Alta |
| REQ-PRO-003 | Código, nome e tipo devem ser obrigatórios | Alta |
| REQ-PRO-004 | O sistema deve informar se o produto exige controle de lote | Alta |
| REQ-PRO-005 | O sistema deve informar se o produto exige controle de validade | Alta |
| REQ-PRO-006 | O sistema deve listar os produtos cadastrados | Alta |
| REQ-PRO-007 | O sistema deve permitir consultar um produto pelo identificador | Alta |
| REQ-PRO-008 | O sistema deve impedir dois produtos com o mesmo código | Alta |
| REQ-PRO-009 | O sistema deve permitir inativar um produto | Média |
| REQ-PRO-010 | O sistema não deve excluir fisicamente um produto utilizado | Alta |
| REQ-PRO-011 | O código deve ser armazenado sem espaços nas extremidades e em letras maiúsculas | Média |
| REQ-PRO-012 | O sistema deve registrar quando o produto foi criado e atualizado | Alta |

## 5. Tipos de produto iniciais

| Valor interno | Exibição esperada |
|---|---|
| MEDICAMENTO | Medicamento |
| PRODUTO_MEDICO | Produto médico |
| DIAGNOSTICO_IN_VITRO | Diagnóstico in vitro |

Esses valores deverão ser definidos pelo `enum` `TipoProduto`, evitando textos diferentes para o mesmo conceito.

## 6. Dicionário de dados

| Campo | Tipo Java sugerido | Obrigatório | Regra |
|---|---|---:|---|
| id | `Long` | Gerado | Identificador interno imutável |
| codigo | `String` | Sim | Único, sem espaços externos e em maiúsculas |
| nome | `String` | Sim | Nome utilizado pela empresa |
| descricao | `String` | Não | Informação complementar |
| tipo | `TipoProduto` | Sim | Um dos valores definidos no enum |
| controlaLote | `boolean` | Sim | Indica necessidade de rastreabilidade por lote |
| controlaValidade | `boolean` | Sim | Indica controle de vencimento |
| ativo | `boolean` | Sim | Novo produto começa ativo |
| criadoEm | `Instant` | Automático | Data e hora da criação |
| atualizadoEm | `Instant` | Automático | Data e hora da última alteração |

## 7. Regras de negócio

### RN-PRO-001 — Código único

Antes de salvar, o sistema deve verificar, sem diferenciar letras maiúsculas e minúsculas, se já existe outro produto com o mesmo código. A tabela também deve manter uma restrição de unicidade como segunda proteção.

### RN-PRO-002 — Normalização do código

O código deve ter os espaços externos removidos e ser convertido para letras maiúsculas. Exemplo: ` cat-001 ` torna-se `CAT-001`.

### RN-PRO-003 — Inativação em vez de exclusão

Um produto não deve desaparecer do histórico. Quando deixar de ser utilizado, deverá passar de ativo para inativo.

### RN-PRO-004 — Responsabilidade das camadas

O `Controller` recebe a ação do navegador. O `Service` executa as regras de negócio. O `Repository` acessa o banco. A entidade `Produto` representa os dados persistidos.

## 8. Critérios de aceitação

| Cenário | Resultado esperado |
|---|---|
| Cadastrar código, nome e tipo válidos | Produto salvo como ativo |
| Tentar cadastrar sem nome | Cadastro recusado e mensagem apresentada |
| Tentar cadastrar código repetido | Cadastro recusado e duplicidade informada |
| Informar código com espaços e letras minúsculas | Código normalizado antes de ser salvo |
| Inativar produto | Produto permanece no banco com `ativo = false` |
| Consultar identificador inexistente | Sistema informa que o produto não foi encontrado |

## 9. Rastreabilidade dos testes atuais

| Teste | Regra ou requisito verificado |
|---|---|
| `deveCriarProdutoAtivo` | Novo produto inicia com `ativo = true` |
| `deveInativarProduto` | RN-PRO-003 e REQ-PRO-009 |
| `deveCadastrarProdutoQuandoCodigoNaoExiste` | REQ-PRO-001 |
| `deveRecusarCadastroQuandoCodigoJaExiste` | RN-PRO-001, REQ-PRO-002 e REQ-PRO-008 |
| `deveListarProdutosOrdenadosPorNome` | REQ-PRO-006 |
| `deveNormalizarCodigoDoProduto` | RN-PRO-002 e REQ-PRO-011 |
| `deveRecusarCodigoEmBranco` | REQ-PRO-003 |
| `deveRecusarNomeEmBranco` | REQ-PRO-003 |
| `deveRecusarTipoNulo` | REQ-PRO-003 |
| `contextLoads` | A aplicação Spring inicia no perfil de testes e valida as migrações |

## 10. Referências do projeto

- RDC 430/2020;
- RDC 665/2022;
- Guia Anvisa nº 33/2020;
- procedimentos internos e requisitos regulatórios aplicáveis.

A correspondência entre cada requisito do sistema, os artigos aplicáveis e os procedimentos internos será detalhada em uma futura matriz de rastreabilidade regulatória.
