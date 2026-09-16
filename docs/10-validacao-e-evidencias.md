# Validação e evidências

Revisão documental de 16/09/2026. Base funcional `5153f62`. Alterações desta revisão são documentais.

## Execução atual

| Item | Evidência |
|---|---|
| Comando | `mvnw.cmd clean test` |
| Término | 16/09/2026 às 17:23:00, UTC-03:00 |
| Resultado | BUILD SUCCESS |
| Executados | 265 |
| Falhas / erros / ignorados | 0 / 0 / 0 |
| Tempo informado pelo Maven | 1 min 16 s |
| Banco de testes | H2 em memória, perfil test |
| Relatórios | `target/surefire-reports/`, gerados localmente e não versionados |

A execução confirma os cenários presentes, não conformidade regulatória, ausência de vulnerabilidades ou comportamento em PostgreSQL/produção. Não há medição de cobertura de linhas/ramos configurada que sustente percentual de cobertura.

## Divergência entre 268 e 265

Em 15/09/2026, uma execução local anterior terminou com 268 testes aprovados após correções autorizadas. No commit `5153f62`, `RastreabilidadeQuantidadeWebTest.java` contém somente a declaração da classe, sem testes. A execução limpa de 16/09 confirmou 265 testes.

A restrição de quantidades permanece no controller/template, mas os três cenários específicos de não divulgação, quantidade disponível versus física e autorização positiva precisam ser restaurados e executados. Não foram alterados nesta tarefa documental. Não apresentar o resultado histórico como validação do arquivo vazio.

## Cobertura funcional observável

| Área | Classes de evidência |
|---|---|
| Produto | ProdutoTest, ProdutoQuantidadeTest, ProdutoServiceTest, ProdutoControllerTest, ProdutoPermissaoWebTest |
| Lote | LoteIntegrationTest |
| Recebimento | RecebimentoIntegrationTest, RecebimentoWebTest |
| Estoque | EstoqueIntegrationTest |
| Expedição | ExpedicaoIntegrationTest, ExpedicaoWebTest, ExpedicaoLoteIntegrationTest, ExpedicaoRollbackIntegrationTest, ExpedicaoConcorrenciaIntegrationTest |
| Rastreabilidade | RastreabilidadeIntegrationTest; teste específico de quantidades vazio |
| Acesso | PerfilAcessoIntegrationTest, UsuarioAcessoIntegrationTest, UsuarioDetailsServiceIntegrationTest |
| Administração | UsuarioPoliticaAcessoTest, UsuarioServiceTest, UsuarioCadastroAcessoIntegrationTest, UsuarioPermissaoWebTest |
| Interface | BotoesCadastroWebTest, BotoesMovimentacaoWebTest, MenuPermissaoWebTest, SistemaRastreabilidadeApplicationTests |

## Pendências de validação

Reforçar negativos da política em múltiplos perfis/legado, edição futura de vínculos, sessão revogada, proibição de autoelevação, testes da filtragem de perfis/botões administrativos e proteção uniforme de serviços. Validar SQL/migrações, concorrência e recuperação no banco produtivo escolhido. Não há ensaio de carga, restauração ou disponibilidade produtiva documentado.

## Atualização da documentação

Conferidos fontes Java, configuração Maven, migrações, templates e testes. Diagramas descrevem associações/fluxos existentes; arquiteturas analíticas e distribuídas são propostas identificadas. Indicadores distinguem dado derivável de medição efetivamente coletada.

Verificação desta revisão: 18 documentos, 59 links locais resolvidos e 10 blocos Mermaid com cercas de código fechadas. `git diff --check` sem erros de whitespace. Foi conferida a correspondência dos nomes centrais e campos com o código. A validação dos diagramas foi estrutural e por leitura; não foi executado um renderizador Mermaid nesta revisão.

Não usar exemplos de consulta ou diagramas como evidência de implementação de uma funcionalidade futura.
