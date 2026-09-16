# Gestão de serviços e fornecedores

Revisão 16/09/2026. **Processo de gestão proposto.** Não há ferramenta de chamados, contratos, fornecedores homologados ou SLAs medidos implementados no ERP.

## Catálogo inicial de serviços

| Serviço | Cliente/dono funcional proposto | Dependências | Evidência de operação |
|---|---|---|---|
| Identidade e acesso | Gestores de área/Administração | Usuários, perfis, sessão e banco | Solicitação/aprovação e testes de acesso |
| Recebimento e estoque | Estoque | Produtos, lotes, documentos e ledger | Confirmação consistente e reconciliação |
| Expedição e histórico | Logística | Saldo, lotes, usuários e documentos | Saída rastreável e recuperação de falhas |
| Disponibilidade e recuperação | Responsável pelo ERP | Infraestrutura, backup, banco e rede | Monitoramento e restauração demonstrada |
| Evolução e suporte | Donos dos processos | Código, testes, requisitos e mudanças | Versão liberada, aceite e registro do atendimento |
| Relatórios analíticos | Donos dos indicadores | Dados autorizados e pipeline futuro | Atualização e conciliação com a origem |

## Incidentes, solicitações e problemas

Solicitação pede acesso/serviço; incidente interrompe ou degrada; problema busca a causa de incidentes recorrentes. Registro mínimo proposto: identificador, solicitante, serviço, impacto, urgência, início, responsável, ações, restauração, causa e evidências sem segredos.

| Prioridade proposta | Exemplo | Tratamento |
|---|---|---|
| P1 | Indisponibilidade total ou suspeita de perda/inconsistência de rastreabilidade | Acionar TI e dono do processo; suspender a operação afetada conforme decisão responsável; preservar evidências |
| P2 | Função crítica indisponível com alternativa controlada | Priorizar diagnóstico e validar alternativa |
| P3 | Falha localizada sem interrupção crítica | Planejar correção e acompanhar |
| P4 | Dúvida ou melhoria | Triar como solicitação/backlog |

Tempos de resposta/resolução dependem da janela de negócio e capacidade de suporte. Não há SLA contratado. Definir relógio útil/corrido, início/fim, pausas e escalonamento antes de medir cumprimento.

## Mudanças e versões

Fluxo proposto: requisito → impacto e riscos → implementação → testes → homologação → aprovação → implantação → verificação. Registrar commit, migrações, versão anterior, evidências e recuperação. Emergências exigem justificativa e revisão posterior. Preservar migrações já aplicadas; alteração de banco exige nova versão.

## Dois tipos de fornecedor

1. **Mercadorias:** cadastro futuro de fornecedor, documentos, habilitação aplicável, produtos fornecidos, pedidos, recebimentos e avaliação.
2. **Tecnologia/serviços:** hospedagem, banco, backup, rede, suporte e eventuais ferramentas de dados.

Hoje `Recebimento.fornecedor` é texto livre. Não há cadastro estruturado de fornecedores, contratos, pedidos de compra, avaliação ou homologação. GitHub é usado para versionamento; não há evidência de contrato/SLA específico no repositório. Dependências Maven não equivalem a fornecedores contratados.

## Qualificação proposta

| Etapa | Registro/evidência |
|---|---|
| Seleção | Necessidade, criticidade, alternativas e responsável |
| Avaliação | Capacidade técnica, requisitos documentais aplicáveis, segurança, continuidade e suporte |
| Aprovação | Aprovador, condições, validade e restrições |
| Contratação | Escopo, responsabilidades, SLA, dados, recuperação, saída e custos |
| Monitoramento | Incidentes, qualidade, prazos, ações e revisão periódica |
| Encerramento | Devolução/exportação de dados, revogação de acessos e transferência do serviço |

Critérios técnicos/sanitários devem ser confirmados por Qualidade/RT conforme o processo; este documento não certifica atendimento legal.

## Continuidade

RPO = perda máxima aceitável de dados no tempo; RTO = tempo máximo aceitável de recuperação. Valores **a definir pelo negócio**, sem promessa numérica nesta revisão. Inventariar dados/configurações, criar cópias consistentes protegidas, manter cópia independente e testar restauração em ambiente isolado. Copiar um arquivo H2 aberto sem método consistente não demonstra recuperação válida.

Roteiro de restauração proposto: identificar versão e backup → restaurar isoladamente → validar esquema/migrações → conferir documentos, vínculos e saldos → registrar tempo/perda observada → aprovar retorno. Não realizar ensaio destrutivo no banco de trabalho.

## Critérios de aceite

Donos nomeados, catálogo aprovado, atendimento rastreável, recuperação demonstrada, fornecedores avaliados e métricas com fontes confiáveis. Painel de SLA, chamados e avaliações permanece planejado.
