# Diagramas — Módulo de Produtos

## 1. Organização das pastas

### Arquivos existentes

```text
src/main/java/br/com/japrodutos/sistema/
└── produto/
    ├── TipoProduto.java
    ├── Produto.java
    ├── ProdutoRepository.java
    ├── ProdutoService.java
    └── ProdutoForm.java

src/main/resources/
└── db/migration/
    └── V2__criar_tabela_produto.sql

src/test/java/br/com/japrodutos/sistema/
└── produto/
    ├── ProdutoTest.java
    └── ProdutoServiceTest.java
```

### Arquivos planejados para concluir o módulo

```text
src/main/java/br/com/japrodutos/sistema/produto/
└── ProdutoController.java

src/main/resources/templates/produtos/
├── lista.html
├── formulario.html
└── detalhe.html

src/test/java/br/com/japrodutos/sistema/produto/
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
        +Produto(String codigo, String nome, TipoProduto tipo, boolean controlaLote, boolean controlaValidade)
        +inativar() void
        +getCodigo() String
        +getNome() String
        +getTipo() TipoProduto
        +isAtivo() boolean
    }

    class ProdutoRepository {
        <<interface>>
        +existsByCodigoIgnoreCase(String codigo) boolean
        +save(Produto produto) Produto
        +findAll(Sort ordenacao) List~Produto~
    }

    class ProdutoService {
        -ProdutoRepository repository
        +cadastrar(Produto produto) Produto
        +listar() List~Produto~
    }

    class ProdutoForm {
        -String codigo
        -String nome
        -String descricao
        -TipoProduto tipo
        -boolean controlaLote
        -boolean controlaValidade
    }

    Produto --> TipoProduto : possui um tipo
    ProdutoRepository --> Produto : persiste
    ProdutoService --> ProdutoRepository : utiliza
    ProdutoService --> Produto : aplica regras
    ProdutoForm --> TipoProduto : recebe o tipo selecionado
```

Este diagrama mostra somente o que já existe. `save` e `findAll` são herdados de `JpaRepository`; não precisam ser escritos novamente na interface.

Durante a construção de `Produto`, o código recebido é normalizado com `trim()` e `toUpperCase(Locale.ROOT)` antes de ser armazenado no campo `codigo`.

## 3. Fluxo implementado no serviço

```mermaid
flowchart LR
    A[Código informado] -->|remover espaços e converter para maiúsculas| N[Objeto Produto]
    N -->|cadastrar| B[ProdutoService]
    B -->|consultar código sem diferenciar maiúsculas| C[ProdutoRepository]
    C -->|existe| D[Recusar cadastro]
    C -->|não existe| E[Salvar produto]
    E --> F[(Tabela produto)]
```

O fluxo pelo navegador será acrescentado quando `ProdutoController` e as telas forem implementados.

## 4. Diagrama de objetos

O diagrama de classes mostra os moldes. O diagrama de objetos mostra exemplos que existem durante a execução.

```mermaid
flowchart LR
    S["produtoService : ProdutoService"]
    R["produtoRepository : ProdutoRepository"]
    P["produtoA : Produto<br/>id = 1<br/>codigo = CAT-001<br/>nome = Cateter<br/>tipo = PRODUTO_MEDICO<br/>controlaLote = true<br/>ativo = true"]
    T["PRODUTO_MEDICO : TipoProduto"]

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
Produto produtoA = new Produto(
        "CAT-001",
        "Cateter",
        TipoProduto.PRODUTO_MEDICO,
        true,
        true
);
```

`Produto` é a classe. `produtoA` é a variável que referencia um objeto dessa classe. `new Produto(...)` chama o construtor e cria o objeto.
