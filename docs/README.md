# Documentação funcional, técnica e de gestão

Revisão: **16/09/2026**. Base: `5153f62`. Esta revisão substitui a visão limitada ao módulo Produtos de 03/09/2026.

## Classificação

- **Implementado:** identificado no código, classe ou migração.
- **Parcial:** parte do processo existe, mas faltam controles ou fluxos.
- **Planejado/proposto:** especificação ou decisão futura; não é funcionalidade disponível.
- **Evidência histórica:** execução anterior que não comprova a revisão atual.

## Mapa documental

| Documento | Conteúdo |
|---|---|
| [Estado e roadmap](01-estado-e-roadmap.md) | MVP/ERP, pendências e critérios de conclusão |
| [Requisitos ERP](requisitos/05-requisitos-erp.md) | Regras por módulo e relação com testes |
| [Produtos](requisitos/01-modulo-produtos.md) | Dados e regras do cadastro |
| [Diagramas de Produtos](requisitos/02-diagramas-produtos.md) | Classes e comportamento |
| [Guia Java](requisitos/03-guia-java.md) | Sintaxe e armadilhas encontradas |
| [Andamento de Produtos](requisitos/04-andamento-produtos.md) | Histórico e situação atual |
| [Estrutura computacional](arquitetura/01-estrutura-computacional.md) | Camadas, execução, persistência e ambientes |
| [UML](arquitetura/02-uml.md) | Classes, associações, sequência e estados |
| [Sistemas distribuídos](arquitetura/03-sistemas-distribuidos.md) | Concorrência e evolução |
| [Segurança](seguranca/01-perfis-permissoes.md) | Autorizações, hierarquia, sessões e lacunas |
| [Estrutura organizacional](gestao/01-estrutura-organizacional.md) | Responsabilidades e RACI proposto |
| [Serviços e fornecedores](gestao/02-servicos-e-fornecedores.md) | Catálogo, incidentes, mudanças e continuidade |
| [Indicadores](gestao/03-indicadores-desempenho.md) | Fórmulas, fontes, responsáveis e periodicidade |
| [Data analytics](dados/01-data-analytics.md) | Modelo analítico, qualidade, linhagem e acesso |
| [Big data](dados/02-big-data.md) | Critérios de adoção e arquitetura futura condicional |
| [Validação](10-validacao-e-evidencias.md) | Execuções, resultados e lacunas |

## Fontes internas

[pom.xml](../pom.xml), [configuração local](../src/main/resources/application.properties), [configuração de testes](../src/test/resources/application-test.properties), [código Java](../src/main/java/com/portfolio/rastreabilidade/), [migrações](../src/main/resources/db/migration/), [templates](../src/main/resources/templates/) e [testes](../src/test/java/com/portfolio/rastreabilidade/).

Propostas organizacionais, metas, modelo analítico e estratégia distribuída dependem de decisões do negócio. Não foram inventados contratos, fornecedores homologados, medições operacionais ou aprovações regulatórias.

## Manutenção

Ao concluir uma entrega, atualizar requisito, diagrama, autorização, evidência e pendências. Registrar versão e data dos testes. Nunca incluir senhas, hashes ou dados reais nos exemplos. O aceite produtivo exige validação própria, além dos testes de desenvolvimento.
