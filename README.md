# Sistema de Rastreabilidade — evolução para ERP

Projeto em desenvolvimento para se tornar um ERP completo, com vários módulos.

Aplicação Java/Spring Boot para produtos, lotes, recebimentos, estoque, expedições, rastreabilidade e controle de acesso. A interface usa Thymeleaf e a aplicação está organizada por módulos de negócio.

Revisão documental: **16/09/2026**. Base: commit `5153f62`. [Documentação completa](docs/README.md).

## Situação do projeto

O planejamento de usuários e permissões foi ampliado e essa etapa continua em desenvolvimento.

O conteúdo publicado no GitHub representa o desenvolvimento do projeto, não uma versão automaticamente aprovada para produção. Os requisitos de segurança continuam sendo estudados, implementados e revisados.

Nenhum módulo regulado entra em produção apenas porque funciona. Cada requisito deverá possuir avaliação de risco, teste e evidência de aprovação.

## Funcionalidades atuais

| Área | Entrega |
|---|---|
| Produtos e lotes | Cadastro, consulta, unidades, fracionamento, inativação de produtos e datas de lotes |
| Recebimento | Rascunho, inclusão de itens e confirmação com entrada no estoque |
| Estoque | Saldo físico por produto/lote calculado pelas movimentações |
| Expedição | Rascunho, validação de saldo/validade e confirmação de saída |
| Rastreabilidade | Histórico, origem, destino e responsável por lote; restrição de quantidades na tela |
| Acessos | Perfis/permissões persistidos, regras por rota, hierarquia de cadastro/status e interface condicional |

Compras, vendas, financeiro, reservas, qualidade, aprovações, auditoria geral e analytics ainda têm entregas pendentes. Códigos de permissões desses módulos não significam funcionalidades disponíveis.

## Executar

Requisitos do projeto: **JDK 25**, Spring Boot **4.1.1** e Maven Wrapper incluído.

No PowerShell, na pasta do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Abra [localhost:8080](http://localhost:8080). Encerre a instância iniciada nesse terminal com `Ctrl+C`. Se a porta estiver ocupada, identifique a instância atual antes de encerrá-la; não reutilize um PID antigo.

O banco local de desenvolvimento é H2 em arquivo, em `data/sistema-rastreabilidade.mv.db`. Flyway aplica migrações e Hibernate valida o esquema. Não apague o banco para resolver falhas de inicialização. Credenciais e hashes não são publicados nesta documentação.

## Testar

```powershell
.\mvnw.cmd clean test
```

Os testes usam H2 em memória no perfil `test`. Relatórios: `target/surefire-reports/`, fora do Git. [Resultados e limites da validação](docs/10-validacao-e-evidencias.md).

## Organização

```text
src/main/java/com/portfolio/rastreabilidade/
  acesso/ config/ usuario/ produto/ lote/
  recebimento/ estoque/ expedicao/ rastreabilidade/ web/
src/main/resources/
  db/migration/ templates/ static/css/ application.properties
src/test/java/       # testes unitários, web e de integração
src/test/resources/  # configuração de testes
docs/                # requisitos, arquitetura, gestão e evolução
```

## Escopo e progresso

**MVP 75% | ERP completo 30%**, estimativas de esforço acordadas, não medições de produtividade, cobertura de testes ou aprovação para produção. Atualizar documentação não altera esses percentuais. [Critérios de conclusão e roadmap](docs/01-estado-e-roadmap.md).

## Leituras principais

- [UML](docs/arquitetura/02-uml.md) e [estrutura computacional](docs/arquitetura/01-estrutura-computacional.md).
- [Sistemas distribuídos](docs/arquitetura/03-sistemas-distribuidos.md).
- [Segurança e permissões](docs/seguranca/01-perfis-permissoes.md).
- [Estrutura organizacional](docs/gestao/01-estrutura-organizacional.md).
- [Serviços e fornecedores](docs/gestao/02-servicos-e-fornecedores.md).
- [Indicadores](docs/gestao/03-indicadores-desempenho.md), [data analytics](docs/dados/01-data-analytics.md) e [big data](docs/dados/02-big-data.md).

A aplicação atual é monolítica e usa H2. Dependências PostgreSQL existem, mas não comprovam homologação nesse banco. Não há ambiente produtivo, BI, data lake ou arquitetura distribuída implantada documentados. Os documentos distinguem implementação de proposta.