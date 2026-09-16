# Guia Java e Spring do projeto

Revisão 16/09/2026. Referência de leitura para o código atual; exemplos conceituais não instruem a sobrescrever classes existentes.

## Estrutura e tipos

| Sintaxe | Uso |
|---|---|
| `package` / `import` | Organização lógica e referência a tipos |
| `class` / construtor | Define dados/comportamentos e inicializa instâncias |
| `private` | Limita acesso direto ao campo/método |
| `final` | Impede reatribuir a referência; não torna o objeto inteiro imutável |
| `Long` | ID que pode ser null antes da persistência |
| `boolean` | Estado lógico, como ativo/fracionável |
| `BigDecimal` | Quantidades decimais; construir a partir de texto quando necessário |
| `LocalDate` | Data de negócio sem horário |
| `OffsetDateTime` | Registro de instante com deslocamento de fuso |
| `enum` | Conjunto fechado de valores, como ENTRADA e SAIDA |
| `record` | Transporta dados com componentes finais, como MovimentoTela |

Comparação de texto usa `.equals()`, não `==`. `==` em objetos compara referências. Para valor decimal, `compareTo` compara magnitude; `equals` também considera escala. Assim, 1.0 e 1.00 podem ter igualdade numérica por `compareTo` sem igualdade por `equals`.

## Coleções e fluxo

`List<T>` mantém sequência; `Set<T>` representa valores sem duplicação. `containsAll` verifica se um conjunto contém todos os elementos do outro, usado para impedir concessão de acessos superiores. `List.copyOf` cria cópia não modificável; não torna todos os elementos profundamente imutáveis.

`Optional<T>` representa resultado possivelmente ausente; `orElseThrow` converte ausência em exceção explícita. Em streams, `filter` seleciona, `map` transforma, `flatMap` reúne coleções e `anyMatch` responde se alguma condição foi atendida. `String...` permite quantidade variável de argumentos.

`condicao ? valorA : valorB` escolhe um resultado. `&&`, `||` e `!` significam E, OU e negação. Curto-circuito permite testar null antes de acessar métodos.

## Spring/JPA

| Recurso | Efeito no projeto |
|---|---|
| `@Controller` | Recebe rotas e seleciona templates |
| `@GetMapping` / `@PostMapping` | Mapeia método HTTP e caminho |
| `@ModelAttribute` | Vincula formulário ou fornece dado à tela |
| `@Valid` / BindingResult | Valida entrada e reúne erros |
| `@Service` / `@Component` | Disponibiliza componente para injeção |
| Injeção pelo construtor | Explicita dependências necessárias |
| `@Transactional` | Delimita transação quando chamado através do proxy Spring |
| `@Entity`, `@Id`, `@Column` | Mapeiam entidade, identidade e coluna |
| `@ManyToMany`, `@JoinTable` | Representam vínculos de usuários/perfis/permissões |
| `@EntityGraph` | Define associações necessárias na consulta |
| `@Lock` | Solicita bloqueio de banco na consulta correspondente |

Chamadas internas na mesma instância não criam automaticamente uma nova transação por anotação. `saveAndFlush` sincroniza SQL, mas não é sinônimo de commit: falha posterior ainda pode reverter a transação.

## Segurança e interface

`SecurityContextHolder` fornece a autenticação da execução atual; não aceitar login do operador vindo de campo do formulário. Role é expressa como autoridade `ROLE_...`; permissões como `USUARIO_CRIAR` são autoridades específicas. `allOf` exige todos os controles; `hasAnyRole` aceita uma das roles listadas.

`AccessDeniedException` interrompe acesso não autorizado. `sec:authorize` controla o HTML gerado, mas não substitui autorização no servidor. `th:if` aplica condição; `th:text` exibe texto; `th:action`/`th:href` montam URLs; `th:block` agrupa sem produzir tag própria.

## Testes e erros já encontrados

- `@Test` identifica cenário; `@BeforeEach` prepara; `@AfterEach` limpa contexto manual.
- `@WithUserDetails` usa usuário carregado pelo serviço; `@WithMockUser` simula autenticação. São evidências diferentes.
- Mockito: preparar outro mock antes de iniciar `when(...).thenReturn(...)`; criar/configurar um mock dentro do argumento de `thenReturn` pode causar `UnfinishedStubbing`.
- `assertThrows` exige a exceção; `verify(..., never())` verifica ausência de chamada, não substitui conferir estado persistido em integração.
- MockMvc `xpath` espera XML. HTML válido com tags vazias pode não ser XML válido; não deformar o HTML para satisfazer um parser inadequado.
- Classes de produção ficam em `src/main/java`; testes em `src/test/java`. Duplicar uma classe de produção com o mesmo pacote/nome nos testes pode mascarar o comportamento real.
- Uma classe terminada em Test, porém sem cenários, não fornece cobertura. `clean test` elimina bytecode antigo antes da contagem.

O projeto já possui templates e vários módulos; a etapa atual não é mais criar a primeira tela de Produtos. [Roadmap atualizado](../01-estado-e-roadmap.md).
