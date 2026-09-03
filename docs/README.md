# Documentação do Sistema de Rastreabilidade

Esta pasta reúne a documentação funcional e técnica do projeto. Ela deve evoluir junto com o código.

## Módulo atual: Produtos

1. [Requisitos do módulo](requisitos/01-modulo-produtos.md)
2. [Diagramas de classes e objetos](requisitos/02-diagramas-produtos.md)
3. [Guia rápido de sintaxe Java](requisitos/03-guia-java.md)
4. [Registro de andamento](requisitos/04-andamento-produtos.md)

## Estado atual

Atualizado em 3 de setembro de 2026.

- progresso estimado do projeto completo: 20%;
- progresso estimado do módulo Produtos: 100% da primeira versão;
- entidade, tabela, repositório e serviço básicos implementados;
- criação como ativo, inativação, recusa de código duplicado e listagem ordenada cobertas por testes;
- normalização do código implementada e coberta por teste;
- código, nome e tipo são validados como campos obrigatórios;
- `ProdutoForm` implementado com validações e conversão segura para `Produto`;
- `ProdutoController` implementado com rotas de listagem, cadastro e consulta por identificador, cobertas por testes;
- telas de cadastro e listagem implementadas e cobertas por testes de renderização;
- consulta por identificador implementada e testada no serviço, no controlador e na tela;
- regra de inativação implementada e testada no serviço, sem exclusão do registro;
- rota de inativação implementada e testada no controlador;
- botão de inativação implementado e validado por teste integrado com banco H2;
- os 27 testes automatizados passam com o JDK 25.0.4.1;
- edição de produtos fica planejada para uma evolução posterior.

Os percentuais servem apenas para acompanhar o desenvolvimento. Eles não representam aprovação regulatória nem validação para uso em produção.

## Regra de trabalho

Antes de implementar uma funcionalidade:

1. identificar o requisito;
2. entender a regra de negócio;
3. programar uma pequena parte;
4. testar o comportamento;
5. guardar a evidência do teste;
6. revisar e criar um commit.

A documentação deve ser revisada ao concluir uma regra relevante, antes de cada commit e no encerramento de cada dia de desenvolvimento.

> Esta documentação é uma base de desenvolvimento. A relação definitiva com requisitos regulatórios deverá ser revisada e aprovada pelo responsável técnico e pela Garantia da Qualidade antes do uso em produção.
