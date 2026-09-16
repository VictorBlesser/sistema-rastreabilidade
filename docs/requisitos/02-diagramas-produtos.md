# Diagramas — Produtos

Revisão 16/09/2026. Complementa o [UML do ERP](../arquitetura/02-uml.md).

```mermaid
classDiagram
    class Produto {
        Long id
        String codigo
        String nome
        String descricao
        TipoProduto tipo
        boolean controlaLote
        boolean controlaValidade
        boolean fracionavel
        UnidadeMedida unidadeMedida
        boolean ativo
        validarQuantidade(BigDecimal quantidade)
        inativar()
    }
    class TipoProduto {
        <<enumeration>>
        MEDICAMENTO
        PRODUTO_MEDICO
        DIAGNOSTICO_IN_VITRO
    }
    class UnidadeMedida {
        <<enumeration>>
        UN
        CX
        PCT
        KG
        G
        L
        ML
        M
    }
    class ProdutoForm {
        toProduto() Produto
    }
    class ProdutoController
    class ProdutoService {
        cadastrar(Produto produto) Produto
        listar() List
        buscarPorId(Long id) Produto
        inativar(Long id) Produto
    }
    class ProdutoRepository {
        <<interface>>
        existsByCodigoIgnoreCase(String codigo) boolean
        buscarParaMovimentacao(Long id) Optional
    }
    Produto --> TipoProduto
    Produto --> UnidadeMedida
    ProdutoController --> ProdutoForm
    ProdutoController --> ProdutoService
    ProdutoForm ..> Produto : converte entrada
    ProdutoService --> ProdutoRepository
    ProdutoRepository ..> Produto
```

`ProdutoRepository` herda operações de `JpaRepository`; a consulta `buscarParaMovimentacao` aplica trava pessimista na confirmação de expedição. A listagem de campos é simplificada, não uma cópia completa das assinaturas.

```mermaid
sequenceDiagram
    actor Usuario
    participant Seg as Spring Security
    participant C as ProdutoController
    participant S as ProdutoService
    participant R as ProdutoRepository
    Usuario->>Seg: POST inativar com CSRF
    Seg->>Seg: Exigir PRODUTO_INATIVAR
    Seg->>C: Encaminhar requisicao
    C->>S: inativar(id)
    S->>R: findById(id)
    R-->>S: Produto
    S->>S: Produto.inativar()
    S->>R: save(produto)
    S-->>C: Produto inativo
    C-->>Usuario: Redirecionar com mensagem
```

Produto inativo continua referenciado pelo histórico. Esconder um botão não substitui a regra de rota. O produto não possui `criadoEm`/`atualizadoEm` implementados nesta versão.
