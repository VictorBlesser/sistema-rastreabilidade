# Estrutura organizacional e responsabilidades

Revisão 16/09/2026. **Modelo proposto para organizar o ERP**, não organograma formal já aprovado nem afirmação sobre cargos ocupados. Pessoas e substitutos devem ser nomeados pela organização.

## Áreas e decisões


| Área | Responsabilidade de negócio | Limite |
|---|---|---|
| Diretoria | Aprovar políticas, riscos, acessos de administradores e prioridades | Não substituir decisões técnicas fora de sua atribuição |
| Administração do ERP | Executar gestão autorizada de usuários e configurações | Não elevar próprio acesso nem administrar Diretoria |
| Responsável Técnico | Decisões técnicas/sanitárias aplicáveis ao processo | Atribuições devem ser formalmente definidas |
| Qualidade | Conformidade, documentação, desvios e evidências | Encaminhar decisões técnicas ao responsável competente |
| Compras | Solicitação, cotação, pedido e relacionamento com fornecedor | Aprovação independente quando exigida |
| Vendas | Orçamento, pedido e negociação autorizada | Sem acesso automático a custo ou margem |
| Financeiro | Crédito, liberação e controles financeiros | Sem movimentação física automática |
| Estoque | Recebimento, lotes, contagem e movimentação física | Sem decisão técnica de liberação por simples posse do material |
| Logística | Separação/expedição, transporte e entrega | Acessos separados de recebimento/compras |
| TI | Infraestrutura, suporte, disponibilidade e recuperação | Sem autoridade comercial/financeira/regulatória automática |
| Consulta | Leitura dos módulos explicitamente concedidos | Sem operações de escrita |

Os perfis de acesso existentes representam apenas parte dessa organização. Usuários com múltiplos perfis podem acumular permissões; isso não garante segregação de funções.

## RACI proposto

R = executa; A = responde pela decisão final; C = consultado; I = informado. Um único A por atividade. Donos devem ser confirmados antes da adoção.

| Atividade | R | A | C | I |
|---|---|---|---|---|
| Criar usuário comum | Administrador | Gestor da área solicitante | TI | Usuário |
| Conceder acesso de Administrador | Operador autorizado da Diretoria | Diretoria | Qualidade/TI | Administrador |
| Aprovar política de permissões | Administração do ERP | Diretoria | Gestores/Qualidade/RT | Usuários |
| Confirmar recebimento | Estoque | Gestor de Estoque | Compras/Qualidade | Financeiro |
| Decidir liberação técnica aplicável | Responsável Técnico | Responsável Técnico | Qualidade | Estoque/Logística |
| Aprovar compra | Aprovador designado | Diretoria ou delegado formal | Compras/Financeiro | Estoque |
| Liberar financeiramente venda | Financeiro | Gestor Financeiro | Vendas | Estoque |
| Operar backup/restauração | TI | Responsável pelo serviço | Donos dos dados/Qualidade | Diretoria |
| Homologar regra do sistema | Equipe do projeto | Dono do processo | Qualidade/TI | Usuários afetados |

RACI é governança organizacional; não está implementado como motor de aprovação. A atribuição A não concede automaticamente uma role no software.

## Segregação proposta

Compra criada por Compras e aprovada por pessoa autorizada distinta quando exigido. Venda passa por aprovação comercial/financeira antes da reserva/separação. Estoque registra não conformidade; Qualidade avalia; RT decide liberação quando aplicável. Implementar comparação de identidade, não apenas de nome de perfil.

Exceção: documentar motivo, escopo, prazo, aprovador independente e revisão posterior. O ERP ainda não possui esse fluxo. Não usar compartilhamento de login para contornar falta de pessoas.

## Ciclo de acesso

Admissão: solicitação do gestor → aprovação → menor acesso necessário → teste de acesso. Mudança de função: revisar conjunto acumulado e remover acessos antigos. Afastamento/desligamento: bloquear/inativar e encerrar sessões; hoje só a inativação básica está implementada. Revisão periódica proposta: conferir usuários, perfis acumulados, exceções, inativos e acessos administrativos, com evidência de decisão.

Manter registro de responsáveis por módulo, substitutos, contato de escalonamento e janela de atendimento em ambiente controlado; dados pessoais reais não pertencem aos exemplos públicos do repositório.
