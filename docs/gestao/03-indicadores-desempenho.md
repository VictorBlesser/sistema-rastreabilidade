# Indicadores de desempenho

Revisão 16/09/2026. Este catálogo define medições; **não apresenta resultados operacionais inventados**. Estado: D = derivável dos dados atuais, mas sem painel/pipeline; P = fonte ou instrumentação pendente; E = evidência de desenvolvimento disponível.

## Catálogo

| ID / indicador | Fórmula e unidade | Fonte / estado | Periodicidade / responsável proposto |
|---|---|---|---|
| KPI-01 Saldo físico | Soma de ENTRADA menos SAIDA por produto/lote, na unidade do produto | `movimentacao_estoque`, `produto`, `lote` / D | Consulta diária / Estoque |
| KPI-02 Quantidade a vencer | Soma de saldo positivo por produto/lote cuja validade esteja entre data de referência e referência + horizonte | Mesmas tabelas / D; horizonte aprovado pelo negócio | Diária / Estoque e Qualidade |
| KPI-03 Tempo até confirmação | Mediana e p95 de `confirmado_em - criado_em`, em minutos, para documentos confirmados na janela | `recebimento`, `expedicao` / D | Semanal / donos dos processos |
| KPI-04 Documentos confirmados | Contagem distinta de IDs por módulo e data de confirmação | Documentos / D | Diária / Estoque e Logística |
| KPI-05 Acuracidade de inventário | 100 × posições produto/lote sem divergência ÷ posições contadas | Contagem física não implementada / P | Por inventário / Estoque |
| KPI-06 OTIF de fornecedor | 100 × pedidos elegíveis entregues completos no prazo ÷ pedidos elegíveis | Pedido, prazo prometido e recebimentos vinculados ausentes / P | Mensal / Compras |
| KPI-07 Entregas no prazo | 100 × entregas concluídas até prazo ÷ entregas elegíveis | Prazo e entrega não implementados / P | Mensal / Logística |
| KPI-08 Disponibilidade | 100 × (minutos elegíveis - indisponíveis) ÷ minutos elegíveis | Monitor externo e janela de serviço / P | Mensal / TI |
| KPI-09 Latência HTTP | p50/p95/p99 da duração das requisições, em ms, por rota e resultado | Instrumentação e armazenamento de métricas / P | Contínua / TI |
| KPI-10 Taxa de erros técnicos | 100 × respostas 5xx ÷ requisições da janela | Métricas HTTP / P; separar 4xx/403 | Diária / TI |
| KPI-11 Tempo de restauração | Mediana e p95 de `restaurado_em - incidente_inicio`, em minutos | Chamados/incidentes ausentes / P | Mensal / gestor de serviço |
| KPI-12 Cumprimento de SLA | 100 × atendimentos elegíveis dentro do limite ÷ atendimentos elegíveis | Chamados e regras de SLA ausentes / P | Mensal / gestor de serviço |
| KPI-13 Qualidade de cadastro | 100 × registros aprovados em todas as regras selecionadas ÷ registros avaliados | Regras/extração a construir sobre cadastros / D parcial | Semanal / dono do dado |
| KPI-14 Saúde da suíte | Executados, falhas, erros e ignorados por execução identificada | Surefire / E | Por build / Desenvolvimento |
| KPI-15 Cobertura de código | Linhas/ramos cobertos ÷ instrumentados | Relatório de cobertura não configurado / P | Por build / Desenvolvimento |
| KPI-16 Atualidade analítica | Instante de consulta menos último watermark processado com sucesso, em minutos | Pipeline analítico futuro / P | Por carga / responsável de dados |

## Regras de cálculo

- Quantidades de KG, UN, CX e outras unidades não devem ser somadas em um total físico único. Não há conversão de embalagens implementada.
- Saldo em uma data exige filtrar movimentações até o corte antes de agregar; data do documento e instante de registro são conceitos diferentes.
- Excluir rascunhos dos indicadores de movimentos efetivados. Manter contagem distinta de documentos ao unir itens.
- Denominador zero significa **não aplicável**, não 0% ou 100%. Exibir tamanho da amostra e período.
- Usar timezone explícito. Instantes devem preservar fuso; relatórios locais podem adotar America/Sao_Paulo após decisão registrada.
- KPI-03 mede espera até confirmação, não tempo efetivo de trabalho. KPI-06 não pode ser inferido apenas do texto do fornecedor.
- Manutenções excluídas da disponibilidade precisam estar previstas e aprovadas. Medir restauração separadamente da solução definitiva de causa.
- Percentis exigem amostras brutas ou histograma adequado; não tirar média de percentis de grupos.

## Metas e alertas

Metas operacionais e limiares: **a definir** após linha de base representativa. Registrar valor-alvo, janela, proprietário, exclusões e ação quando violado. A suíte deve passar antes da liberação; isso não equivale a cobertura de 100% nem ausência de vulnerabilidades.

Evidência atual: execução limpa de 16/09/2026 com **265 testes, 0 falhas, 0 erros e 0 ignorados**. Os 268 testes de 15/09 são históricos; [diferença documentada](../10-validacao-e-evidencias.md).

## Visualização e acesso

Painéis propostos: operacional para Estoque/Logística; qualidade para validade/conformidade; gestão para serviço/fornecedores; técnico para latência/erros. Aplicar permissões também a agregados e exportações. Custo, preço e margem não devem aparecer em dashboards genéricos. Registrar atualização, unidade, período e regra de cálculo ao lado de cada resultado.
