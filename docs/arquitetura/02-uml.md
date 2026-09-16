# UML — domínio e comportamento

Modelos simplificados de `5153f62`, revisão 16/09/2026. Diagramas Mermaid de classes, sequência e estados representam visões UML. Nem todos os campos/getters são exibidos. Fontes: [código](../../src/main/java/com/portfolio/rastreabilidade/) e migrações V2–V11.

## Classes operacionais

```mermaid
classDiagram
    class Produto {
        Long id
        String codigo
        boolean fracionavel
        UnidadeMedida unidadeMedida
        boolean ativo
        validarQuantidade(BigDecimal quantidade)
        inativar()
    }
    class Lote {
        Long id
        String numero
        LocalDate dataFabricacao
        LocalDate dataValidade
        boolean ativo
    }
    class Recebimento {
        Long id
        String fornecedor
        String documento
        StatusRecebimento status
        confirmar(Usuario usuario)
    }
    class RecebimentoItem {
        Long id
        BigDecimal quantidade
    }
    class Expedicao {
        Long id
        String destinatario
        String documento
        StatusExpedicao status
        confirmar(Usuario usuario)
    }
    class ExpedicaoItem {
        Long id
        BigDecimal quantidade
        validar()
    }
    class MovimentacaoEstoque {
        Long id
        TipoMovimentacao tipo
        BigDecimal quantidade
        OffsetDateTime registradoEm
    }
    class Usuario {
        Long id
        String login
        boolean ativo
    }
    Produto "1" -- "0..*" Lote
    Recebimento "1" *-- "0..*" RecebimentoItem
    Expedicao "1" *-- "0..*" ExpedicaoItem
    Produto "1" -- "0..*" RecebimentoItem
    Produto "1" -- "0..*" ExpedicaoItem
    Lote "0..1" -- "0..*" RecebimentoItem
    Lote "0..1" -- "0..*" ExpedicaoItem
    RecebimentoItem "0..1" -- "0..1" MovimentacaoEstoque : origem entrada
    ExpedicaoItem "0..1" -- "0..1" MovimentacaoEstoque : origem saida
    Produto "1" -- "0..*" MovimentacaoEstoque
    Lote "0..1" -- "0..*" MovimentacaoEstoque
    Usuario "1" -- "0..*" MovimentacaoEstoque : registradoPor
    Usuario "0..1" -- "0..*" Recebimento : confirmadoPor
    Usuario "0..1" -- "0..*" Expedicao : confirmadoPor
```

Cada movimentação tem **exatamente uma origem**, recebimento_item ou expedicao_item, conforme seu tipo. A exclusividade é garantida por `ck_movimentacao_origem` na V8, não apenas pelas multiplicidades. Rascunhos podem ter zero itens; confirmação exige itens. Composição expressa pertencimento, não autorização para apagar histórico.

## Classes de acesso

```mermaid
classDiagram
    class Usuario {
        Long id
        String login
        String senhaHash
        PerfilUsuario perfil
        boolean ativo
        getCodigosPermissoes() Set
    }
    class PerfilAcesso {
        Long id
        String codigo
        String nome
        boolean sistema
    }
    class Permissao {
        Long id
        String codigo
        String descricao
    }
    class UsuarioDetailsService {
        loadUserByUsername(String login) UserDetails
    }
    class UsuarioPoliticaAcesso {
        validarCadastro(Usuario operador, PerfilAcesso destino)
        validarAlteracaoStatus(Usuario operador, Usuario destino)
    }
    class UsuarioService {
        ativar(Long id) Usuario
        inativar(Long id) Usuario
    }
    class UsuarioAcessoTelaService {
        perfisPermitidos() List
        podeAlterarStatus(Long id) boolean
    }
    Usuario "0..*" -- "0..*" PerfilAcesso : usuario_perfil
    PerfilAcesso "0..*" -- "0..*" Permissao : perfil_permissao
    UsuarioDetailsService ..> Usuario : carrega acessos
    UsuarioService ..> UsuarioPoliticaAcesso : autoriza gravacao
    UsuarioAcessoTelaService ..> UsuarioPoliticaAcesso : filtra interface
```

`Usuario.perfil` legado ainda existe e aparece nas telas. O login usa vínculos `perfis`, sem fallback para o legado. A política também considera o legado ao proteger o destinatário. Cadastro usa enum; edição de múltiplos vínculos e perfis personalizados ainda não está disponível.

## Sequência de confirmação de expedição

```mermaid
sequenceDiagram
    actor Operador
    participant Sec as Spring Security
    participant C as ExpedicaoController
    participant S as ExpedicaoService
    participant DB as Banco relacional
    Operador->>Sec: POST confirmar com sessao e CSRF
    Sec->>Sec: Exigir EXPEDICAO_CONFIRMAR
    Sec->>C: Requisicao autorizada
    C->>S: confirmar(id)
    S->>DB: Iniciar transacao e travar documento
    S->>S: Exigir rascunho, itens e usuario ativo
    S->>DB: Travar produtos em ordem de ID
    S->>DB: Consultar saldo por produto e lote
    S->>S: Validar itens, validade e demanda agregada
    alt Dados validos e saldo suficiente
        S->>DB: Atualizar documento e inserir saidas
        DB-->>S: Commit
        S-->>C: Sucesso
        C-->>Operador: Redirecionar com mensagem
    else Regra violada ou falha de persistencia
        S->>DB: Rollback
        S-->>C: Excecao
        C-->>Operador: Mensagem de regra ou resposta de erro
    end
```

O controller trata erros de regra (`IllegalArgumentException`); nem toda falha de persistência é convertida nessa mensagem. Autorização HTTP e transação de negócio são controles distintos.

## Estados atuais dos documentos

```mermaid
stateDiagram-v2
    [*] --> RASCUNHO : criar
    RASCUNHO --> RASCUNHO : adicionar item valido
    RASCUNHO --> CONFIRMADO : confirmar com validacoes
    CONFIRMADO --> [*]
```

Recebimento e expedição têm esses estados. Não há estados implementados de aprovação, cancelamento, separação, quarentena ou entrega. O final do diagrama encerra o fluxo, sem excluir o registro. Falha de confirmação mantém o rascunho por rollback.

## Fluxos futuros

Compra: solicitação → cotação → pedido → aprovação → fornecedor → recebimento → conferência → entrada. Venda: orçamento → pedido → aprovação → reserva → faturamento → separação → expedição → entrega. São requisitos, não fluxos implementados. Diagramas detalhados dependerão das regras de transição e responsabilidades aprovadas.
