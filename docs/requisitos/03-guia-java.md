# Guia rápido de Java para o Sistema de Rastreabilidade

Este guia é uma lembrança de sintaxe. Os exemplos não foram adicionados às classes do sistema e podem ser adaptados durante a programação.

## 1. Estrutura básica de uma classe

```java
package com.portfolio.rastreabilidade.produto;

public class Produto {

    private Long id;
    private String nome;
    private boolean ativo;

    public Produto() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void inativar() {
        this.ativo = false;
    }
}
```

Partes importantes:

| Sintaxe | Significado |
|---|---|
| `package` | Pasta lógica da classe |
| `public class Produto` | Declara uma classe pública |
| `private String nome` | Declara um campo acessível apenas pela própria classe |
| `public Produto()` | Construtor da classe |
| `void` | Método que não devolve valor |
| `return nome` | Devolve um valor ao chamador |
| `this.nome` | Campo `nome` do objeto atual |
| `;` | Finaliza uma instrução |

## 2. Tipos utilizados no módulo

```java
String nome = "Cateter";
Long id = 1L;
boolean ativo = true;
Instant criadoEm = Instant.now();
```

- `String`: texto;
- `Long`: número inteiro que pode ser `null`, utilizado em identificadores;
- `long`: número inteiro que nunca é `null`;
- `boolean`: aceita somente `true` ou `false`;
- `Instant`: instante de data e hora adequado para registros técnicos.

Para utilizar `Instant`:

```java
import java.time.Instant;
```

## 3. Enum

Um `enum` limita os valores possíveis:

```java
package com.portfolio.rastreabilidade.produto;

public enum TipoProduto {
    MEDICAMENTO,
    PRODUTO_MEDICO,
    DIAGNOSTICO_IN_VITRO
}
```

Uso:

```java
TipoProduto tipo = TipoProduto.PRODUTO_MEDICO;
```

## 4. Método com parâmetro e retorno

```java
public Produto buscarPorId(Long id) {
    Produto produto = repository.findById(id).orElseThrow();
    return produto;
}
```

Leitura da assinatura:

```text
public       pode ser chamado por outras classes
Produto      tipo devolvido pelo método
buscarPorId  nome do método
Long id      parâmetro recebido
```

## 5. Condições

```java
if (nome == null || nome.isBlank()) {
    throw new IllegalArgumentException("O nome é obrigatório");
}
```

Operadores comuns:

| Operador | Significado |
|---|---|
| `==` | Igualdade de valores primitivos ou mesma referência |
| `!=` | Diferente |
| `&&` | E |
| `\|\|` | Ou |
| `!` | Negação |

Para comparar textos, utilize `.equals()`:

```java
if (codigo.equals("CAT-001")) {
    // Os textos são iguais.
}
```

Para evitar erro quando o primeiro texto pode ser `null`:

```java
if ("CAT-001".equals(codigo)) {
    // Comparação segura.
}
```

## 6. Listas

```java
import java.util.List;

List<Produto> produtos = repository.findAll();

for (Produto produto : produtos) {
    System.out.println(produto.getNome());
}
```

`List<Produto>` significa uma lista que aceita objetos da classe `Produto`.

## 7. Optional

Uma busca pode ou não encontrar um produto. O `Optional` representa essa possibilidade:

```java
import java.util.Optional;

Optional<Produto> resultado = repository.findById(id);
```

Exemplo direto:

```java
Produto produto = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
```

## 8. Interface e Spring Data

Uma interface define operações sem precisar implementar todas elas no mesmo arquivo:

```java
public interface ProdutoRepository {
    Produto salvar(Produto produto);
}
```

No projeto, o `ProdutoRepository` já estende uma interface do Spring:

```java
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
}
```

`Produto` indica a entidade administrada. `Long` indica o tipo do identificador. Métodos como `save` e `findAll` são herdados de `JpaRepository`.

O nome `existsByCodigoIgnoreCase` é interpretado pelo Spring:

```text
exists     verifica se existe
ByCodigo   utiliza o campo codigo
IgnoreCase ignora diferença entre maiúsculas e minúsculas
```

## 9. Anotações

Anotações começam com `@` e fornecem instruções ao Spring ou ao JPA:

```java
@Entity
public class Produto {
}
```

Exemplos que aparecerão no módulo:

| Anotação | Finalidade |
|---|---|
| `@Entity` | Indica uma entidade persistida |
| `@Id` | Identifica a chave primária |
| `@GeneratedValue` | Solicita geração automática do identificador |
| `@Service` | Identifica uma classe de regras de negócio |
| `@Controller` | Identifica uma classe que recebe ações web |
| `@GetMapping` | Mapeia uma consulta do navegador |
| `@PostMapping` | Mapeia o envio de um formulário |
| `@Valid` | Solicita validação dos dados recebidos |

## 10. Injeção pelo construtor

O `Service` precisa utilizar o `Repository`. A ligação pode ser recebida no construtor:

```java
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }
}
```

`final` significa que a referência deverá ser definida no construtor e não poderá ser substituída depois.

## 11. Convenções de nomes

```text
Classe e enum:       Produto, TipoProduto
Método e variável:   buscarPorId, produtoEncontrado
Constante:           TAMANHO_MAXIMO
Pacote:              produto
Arquivo:             mesmo nome da classe pública
```

Java diferencia maiúsculas e minúsculas. `Produto`, `produto` e `PRODUTO` são nomes diferentes.

## 12. Sequência de desenvolvimento

As etapas iniciais concluídas foram:

1. Crie `TipoProduto.java`.
2. Confirme que o projeto ainda compila.
3. Crie a estrutura inicial de `Produto.java`.
4. Adicione poucos campos por vez.
5. Corrija os avisos antes de continuar.
6. Avance para banco, repositório e serviço.

O projeto já concluiu essa sequência. A etapa atual é normalizar e validar os dados antes de criar o formulário e o controlador web.

Quando ocorrer um erro, leia primeiro a primeira mensagem que aponta para um arquivo do seu projeto. Anote o nome do arquivo, a linha e a mensagem; essas três informações normalmente são suficientes para investigar o problema.

## 13. Estrutura de um teste com Mockito

O teste do serviço substitui temporariamente o repositório real por um objeto controlado chamado `mock`:

```java
when(repository.existsByCodigoIgnoreCase("CAT-001"))
        .thenReturn(true);

IllegalArgumentException erro = assertThrows(
        IllegalArgumentException.class,
        () -> service.cadastrar(produto)
);

assertEquals("Código já cadastrado", erro.getMessage());
verify(repository, never()).save(produto);
```

Leitura do teste:

| Trecho | Significado |
|---|---|
| `when(...).thenReturn(true)` | Prepara a resposta simulada do repositório |
| `assertThrows` | Confirma que a operação lançou a exceção esperada |
| `assertEquals` | Compara o resultado esperado com o resultado obtido |
| `verify` | Confirma se um método foi ou não chamado |
| `never()` | Exige que `save` não seja executado |
