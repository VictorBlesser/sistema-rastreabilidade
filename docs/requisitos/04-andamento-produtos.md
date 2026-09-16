# Andamento — Produtos

Revisão 16/09/2026. Registro histórico simplificado; não substitui evidências de homologação.

| Marco | Situação/evidência |
|---|---|
| Até 03/09/2026 | Cadastro, listagem, consulta e inativação da primeira versão; registro histórico de 27 testes na suíte daquela data |
| Evolução posterior até 15/09 | Unidades e fracionamento; integração com lotes, recebimento, estoque e expedição |
| 15/09/2026, commit 5153f62 | Permissões por operação, botões condicionais e testes negativos de acesso |
| 16/09/2026 | Documentação consolidada com arquitetura e escopo ERP; código de negócio não alterado nesta revisão |

O módulo básico está implementado, mas não equivale a um cadastro completo de ERP. Permanecem: edição, dados técnicos ampliados, fornecedores/fabricantes estruturados e requisito temporal REQ-PRO-012.

Evidências atuais: [requisitos](01-modulo-produtos.md), [diagramas](02-diagramas-produtos.md) e [validação consolidada](../10-validacao-e-evidencias.md). Os 27 testes são históricos; não são a contagem atual do projeto.

A próxima prioridade transversal é concluir permissões, auditoria e correções rastreáveis. Inativação já existe e não deve continuar listada como próxima implementação.

Estimativas do projeto: **MVP 75% | ERP completo 30%**. A antiga referência de 20% foi substituída pela separação de escopos; a documentação não mede esforço restante por número de arquivos.
