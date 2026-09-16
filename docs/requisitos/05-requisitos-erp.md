# Requisitos consolidados do ERP

Revisão 16/09/2026. IDs abaixo organizam esta documentação; não representam tickets externos. I = implementado no limite descrito; P = parcial; F = futuro.

| ID | Requisito / critério de aceite | Estado | Evidência ou dependência |
|---|---|---|---|
| ERP-PRO-01 | Cadastrar, consultar e inativar produtos sem rota de exclusão | I | `ProdutoServiceTest`, `ProdutoControllerTest` |
| ERP-PRO-02 | Exigir quantidade positiva e inteira quando não fracionável | I | `ProdutoQuantidadeTest` |
| ERP-LOT-01 | Lote vinculado ao produto, datas coerentes e número único por produto | I | `LoteIntegrationTest` |
| ERP-REC-01 | Rascunho sem efeito no saldo; confirmação registra todos os itens ou nenhum | I | `RecebimentoIntegrationTest` |
| ERP-EST-01 | Saldo físico = entradas menos saídas por produto/lote | I | `EstoqueIntegrationTest` |
| ERP-EXP-01 | Recusar saída sem saldo, lote incompatível/inativo ou vencido na data aplicável | I | `ExpedicaoIntegrationTest`, `ExpedicaoLoteIntegrationTest` |
| ERP-EXP-02 | Confirmar sem duplicidade e sem saída indevida sob concorrência | I, no ambiente testado | `ExpedicaoConcorrenciaIntegrationTest`, `ExpedicaoRollbackIntegrationTest` |
| ERP-RAS-01 | Relacionar lote a origem, destino, documento e responsável | I | `RastreabilidadeIntegrationTest` |
| ERP-ACE-01 | Permissões por operação, união de perfis e consulta separada de escrita | P | `UsuarioDetailsServiceIntegrationTest`, testes web de permissão; proteção de serviços ainda desigual |
| ERP-ACE-02 | Diretoria controla Administradores; não conceder acessos superiores aos do operador | P | Política/testes de cadastro/status; edição de permissões futura |
| ERP-ACE-03 | Impedir alteração da própria permissão, inclusive por perfil compartilhado | F | Implementar com edição de vínculos e perfis |
| ERP-ACE-04 | Quantidades, custo, preço e margem com acessos separados | P | Quantidades da rastreabilidade filtradas; teste específico vazio; dados comerciais ausentes |
| ERP-ACE-05 | Usuários ATIVO, BLOQUEADO, INATIVO e AFASTADO | P | Booleano ativo atual; demais estados futuros |
| ERP-ACE-06 | Login individual, recuperação, tentativas/bloqueio, sessões, timeout e 2FA opcional | P | Login/logout atuais; controles ampliados futuros |
| ERP-AUD-01 | Registrar usuário, perfis, instante, ação, módulo, registro, antes/depois, IP/sessão e justificativa | P | Responsável/instante de confirmação atuais; auditoria geral ausente |
| ERP-AUD-02 | Preservar rastreabilidade com cancelamento, estorno, inativação ou versão | P | Inativação e histórico atuais; estorno/cancelamento futuros |
| ERP-APR-01 | Aprovações de compra/venda e segregação entre criador e aprovador | F | Pedidos e motor de aprovação pendentes |
| ERP-COM-01 | Compra: solicitação, cotação, pedido, aprovação, fornecedor, recebimento e conferência | F, salvo recebimento básico | Cadastro estruturado de fornecedor e pedido pendentes |
| ERP-VEN-01 | Venda: orçamento, pedido, aprovação, reserva, faturamento, separação, expedição e entrega | F, salvo expedição básica | Demais etapas pendentes |
| ERP-QUA-01 | Não conformidade, bloqueio/quarentena e liberação técnica | F | Perfis/permissões catalogados, processo ainda ausente |
| ERP-DAD-01 | Indicadores reconciliados, dados autorizados e atualização identificada | F | Definições documentadas; pipeline/painel ausentes |

## Justificativa e histórico exigidos

Cancelamento de pedido, estorno/ajuste de estoque, alteração de lote/validade, bloqueio/liberação, cancelamento financeiro, alteração de custo/preço e mudança manual de quantidade deverão registrar justificativa, autor e estado anterior/novo. Não introduzir essas ações sem trilha correspondente.

## Requisitos não funcionais

| ID | Critério proposto | Situação |
|---|---|---|
| NFR-01 | Consistência transacional, recuperação de falhas e concorrência | Parcialmente testada em H2; repetir no banco de destino |
| NFR-02 | Negação por padrão e menor privilégio em todas as entradas | Parcial: fallback global autenticado e serviços sem proteção uniforme |
| NFR-03 | Desempenho com volume/concorrência representativos | Sem linha de base ou metas aprovadas |
| NFR-04 | Recuperação com RPO/RTO definidos e restauração comprovada | Planejado |
| NFR-05 | Diagnóstico sem divulgar segredos ou dados excessivos | Configuração básica de erros; revisão de logs e processos pendente |
| NFR-06 | Mudanças rastreáveis e migrações reproduzíveis | Git/Flyway existentes; fluxo formal de aceite futuro |

A ausência de exclusão na interface não protege contra toda alteração direta no banco. Definir privilégios de operação e suporte. Esta matriz não declara certificação ou conformidade regulatória; requisitos legais específicos precisam de avaliação própria por responsáveis competentes.
