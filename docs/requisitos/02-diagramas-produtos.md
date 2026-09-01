# Diagramas — Módulo de Produtos

## 1. Organização das pastas

```text
src/main/java/br/com/japrodutos/sistema/
└── produto/
    ├── TipoProduto.java
    ├── Produto.java
    ├── ProdutoRepository.java
    ├── ProdutoService.java
    └── ProdutoController.java

src/main/resources/
├── db/migration/
│   └── V2__criar_tabela_produto.sql
└── templates/produtos/
    ├── lista.html
    ├── formulario.html
    └── detalhe.html

src/test/java/br/com/japrodutos/sistema/
└── produto/
    ├── ProdutoServiceTest.java
    └── ProdutoControllerTest.java
```

## 2. Diagrama de classes

```mermaid
classDiagram
    class TipoProduto {
        <<enumeration>>
        MEDICAMENTO
        PRODUTO_MEDICO
        DIAGNOSTICO_IN_VITRO
    }

    class Produto {
        -Long id
        -String codigo
        -String nome
        -String descricao
        -TipoProduto tipo
        -boolean controlaLote
        -boolean controlaValidade
        -boolean ativo
        -Instant criadoEm
        -Instant atualizadoEm
        +normalizarCodigo() void
        +inativar() void
    }

    class ProdutoRepository {
        <<interface>>
        +findByCodigo(String codigo) Optional~Produto~
        +existsByCodigo(String codigo) boolean
        +findAllByOrderByNomeAsc() List~Produto~
    }

    class ProdutoService {
        -ProdutoRepository repository
        +cadastrar(Produto produto) Produto
        +listar() List~Produto~
        +buscarPorId(Long id) Produto
        +inativar(Long id) void
    }

    class ProdutoController {
        -ProdutoService service
        +listar(Model model) String
        +abrirFormulario(Model model) String
        +cadastrar(Produto produto) String
        +detalhar(Long id, Model model) String
    }

    Produto --> TipoProduto : possui um tipo
    ProdutoRepository --> Produto : persiste
    ProdutoService --> ProdutoRepository : utiliza
    ProdutoService --> Produto : aplica regras
    ProdutoController --> ProdutoService : solicita operacoes
```

As operações mostradas são uma referência de organização, não uma obrigação de copiar todos os métodos imediatamente.

## 3. Como uma requisição percorre o sistema

```mermaid
flowchart LR
    A[formulario.html] -->|POST /produtos| B[ProdutoController]
    B -->|cadastrar produto| C[ProdutoService]
    C -->|verificar codigo| D[ProdutoRepository]
    C -->|salvar| D
    D -->|INSERT ou SELECT| E[(Banco de dados)]
    C -->|produto salvo| B
    B -->|redirecionar| F[lista.html]
```

## 4. Diagrama de objetos

O diagrama de classes mostra os moldes. O diagrama de objetos mostra exemplos que existem durante a execução.

```mermaid
flowchart LR
    C["produtoController : ProdutoController"]
    S["produtoService : ProdutoService"]
    R["produtoRepository : ProdutoRepository"]
    P["produtoA : Produto<br/>id = 1<br/>codigo = CAT-001<br/>nome = Cateter<br/>tipo = PRODUTO_MEDICO<br/>controlaLote = true<br/>ativo = true"]
    T["PRODUTO_MEDICO : TipoProduto"]

    C -->|possui referencia| S
    S -->|possui referencia| R
    R -->|carrega e salva| P
    P -->|valor do campo tipo| T
```

## 5. Diferença entre classe e objeto

```text
Classe  = molde ou definição: Produto.java
Objeto  = um exemplar criado a partir do molde: produtoA
Campo   = característica do objeto: nome
Método  = comportamento do objeto: inativar()
```

Exemplo conceitual:

```java
Produto produtoA = new Produto();
```

`Produto` é a classe. `produtoA` é a variável que referencia um objeto dessa classe. `new Produto()` cria o objeto.
