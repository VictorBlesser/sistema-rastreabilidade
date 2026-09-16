# Estado do projeto e roadmap

Base `5153f62`, conferida em 16/09/2026. Prioridades aprovadas pelo proprietário do projeto; responsáveis funcionais ainda devem ser formalmente designados.

## Escopos

| Escopo | Estimativa | Critério de conclusão |
|---|---:|---|
| ERP MVP | 75% | Núcleo operacional utilizável, acessos essenciais, auditoria e correções rastreáveis, backup/restauração e aceite |
| ERP completo | 30% | MVP mais compras, vendas, financeiro, qualidade, reservas, aprovações, segregação e segurança ampliada |

Estimativas subjetivas de esforço mantidas de 15/09, não calculadas por classes ou testes. Não há prazo confiável sem dividir o backlog em entregas e medir a capacidade real.

## Inventário

| Área | Situação | Existe | Falta |
|---|---|---|---|
| Produtos | Básico implementado | Cadastro, consulta, unidade, fracionamento, inativação | Edição, dados técnicos ampliados e auditoria geral |
| Lotes | Parcial | Cadastro, datas, consulta e validações | Séries, bloqueio, quarentena e liberação |
| Recebimento | Básico implementado | Rascunho, itens e confirmação atômica | Compra vinculada, conferência, cancelamento e estorno |
| Estoque | Parcial | Entradas/saídas e saldo físico | Reservas, disponível, inventário e ajuste justificado |
| Expedição | Básico implementado | Rascunho, saldo, validade e confirmação | Separação, transporte, entrega e devolução |
| Rastreabilidade | Parcial | Histórico por lote e origem/destino | Recall, séries e trilha geral de alterações |
| Usuários/perfis | Parcial | Vínculos, união de permissões, política de cadastro/status | Edição de vínculos, perfis customizados, auditoria e sessões |
| Compras/vendas/financeiro | Planejado | Perfis e permissões iniciais | Entidades, serviços, telas e aprovações |
| Qualidade/RT | Planejado operacionalmente | Perfis e permissões catalogadas | Não conformidades, documentos e decisões técnicas |
| Analytics/big data | Planejado | Dados transacionais de origem | Extração, camada analítica, painéis e operação |

## Ordem proposta

1. **Concluir acessos:** edição segura de vínculos, impedir autoelevação direta/indireta, auditoria, efeito nas sessões e testes. Recuperar a cobertura de quantidades ausente no commit atual.
2. **Fechar rastreabilidade operacional:** cancelamento/estorno com justificativa, prevenção de duplicidade, rollback e histórico preservado.
3. **Homologar MVP:** banco de destino, segredos, HTTPS, restauração demonstrada, suporte e aceite por processo.
4. **Ciclo comercial:** fornecedores/clientes, compras/vendas, aprovações, reservas e financeiro.
5. **Qualidade/logística:** quarentena, bloqueio/liberação, devoluções, transporte e entrega.
6. **Dados/escala:** instrumentar indicadores e avaliar BI ou distribuição mediante evidências.

## Evidências de conclusão

| Entrega | Evidência |
|---|---|
| Permissões | Matriz operação/perfil, testes positivos/negativos e bloqueio da autoelevação por perfil compartilhado |
| Movimentação | Consistência após falha, concorrência sem saída indevida e correção preservando origem |
| Produção | Restauração testada, responsabilidades, acessos e roteiro de atendimento |
| Aprovações | Criador/aprovador registrados e conflitos de função definidos e testados |
| Analytics | Conciliação com origem, atualização, qualidade e autorização de dados |

## Decisões em aberto

- Procedimento controlado para o primeiro acesso de Diretoria; não elevar automaticamente o administrador inicial.
- Conflitos de função obrigatórios e exceções aprovadas.
- Visibilidade de quantidade documental versus saldo físico em todos os módulos.
- Volumes, concorrência, janela de serviço, retenção, RPO/RTO e infraestrutura produtiva.
- Critérios de aceite do MVP: o percentual não autoriza liberação.
