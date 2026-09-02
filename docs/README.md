# Documentação do Sistema JA

Esta pasta reúne a documentação funcional e técnica do projeto. Ela deve evoluir junto com o código.

## Módulo atual: Produtos

1. [Requisitos do módulo](requisitos/01-modulo-produtos.md)
2. [Diagramas de classes e objetos](requisitos/02-diagramas-produtos.md)
3. [Guia rápido de sintaxe Java](requisitos/03-guia-java.md)
4. [Registro de andamento](requisitos/04-andamento-produtos.md)

## Estado atual

Atualizado em 2 de setembro de 2026.

- progresso estimado do projeto completo: 13%;
- progresso estimado do módulo Produtos: 82%;
- entidade, tabela, repositório e serviço básicos implementados;
- criação como ativo, inativação, recusa de código duplicado e listagem ordenada cobertas por testes;
- normalização do código implementada e coberta por teste;
- código, nome e tipo são validados como campos obrigatórios;
- `ProdutoForm` implementado com validações para a futura tela;
- `ProdutoController` é a próxima classe planejada;
- telas de cadastro, consulta e edição ainda não foram criadas.

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
