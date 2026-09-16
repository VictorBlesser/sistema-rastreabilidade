# Perfis, permissões e segurança

Base `5153f62`, revisão 16/09/2026. Fontes: [SecurityConfig](../../src/main/java/com/portfolio/rastreabilidade/config/SecurityConfig.java), [política](../../src/main/java/com/portfolio/rastreabilidade/usuario/UsuarioPoliticaAcesso.java), [login](../../src/main/java/com/portfolio/rastreabilidade/usuario/UsuarioDetailsService.java) e [V10](../../src/main/resources/db/migration/V10__criar_cadastrar_permissoes_iniciais.sql).

## Implementação atual

`usuario_perfil` permite múltiplos vínculos; `perfil_permissao` associa permissões aos perfis. O login reúne roles e permissões sem duplicação. Sem vínculos, não usa o perfil legado como alternativa. V11 completa vínculos de usuários que ainda não tinham nenhum, durante a migração; não é fallback de autenticação.

A conta inicia ativa no cadastro; o status ainda é booleano. BLOQUEADO e AFASTADO são requisitos futuros. As migrações contêm perfis predefinidos; não há tela de edição de perfis/vínculos. O formulário ainda escolhe um perfil inicial do enum.

## Rotas operacionais

| Operação | Permissão atual |
|---|---|
| Ler produtos / cadastrar / inativar | `PRODUTO_VISUALIZAR` / `PRODUTO_CADASTRAR` / `PRODUTO_INATIVAR` |
| Ler lotes / cadastrar | `LOTE_VISUALIZAR` / `LOTE_CADASTRAR` |
| Ler / criar / adicionar item / confirmar recebimento | `RECEBIMENTO_VISUALIZAR` / `CRIAR` / `EDITAR` / `CONFIRMAR`, com prefixo `RECEBIMENTO_` |
| Ler / criar / adicionar item / confirmar expedição | Equivalentes com prefixo `EXPEDICAO_` |
| Consultar saldo físico | `ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR` |
| Consultar histórico | `RASTREABILIDADE_VISUALIZAR` |
| Ver quantidades/totais na rastreabilidade | Exige também `ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR` |
| Consultar usuários | `USUARIO_VISUALIZAR` |
| Cadastrar usuário | Role DIRETORIA ou ADMINISTRADOR e permissões `USUARIO_CRIAR` e `USUARIO_VINCULAR_PERFIS` |
| Ativar/inativar usuário | Role DIRETORIA ou ADMINISTRADOR e `USUARIO_ALTERAR_STATUS`, mais política de destino |

O nome de um perfil sozinho não substitui as permissões. As famílias de rotas conhecidas possuem regras de negação para operações não previstas. O fallback global ainda é `anyRequest().authenticated()`: **negação global por padrão ainda não está concluída**.

## Hierarquia aplicada no serviço de usuários

1. Operador autenticado, existente e ativo, carregado com seus acessos do banco.
2. Operador com perfil de gestão e permissões da operação.
3. Administrador pode administrar usuários comuns; somente Diretoria pode administrar destinatários Administrador/Diretoria.
4. Cadastro não pode conceder permissões ausentes no operador.
5. Ninguém pode alterar o próprio status.
6. Interface reutiliza a política para filtrar opções/botões; o serviço valida novamente antes de gravar.

A regra de não alterar a própria **permissão**, inclusive indiretamente por perfil compartilhado, deverá ser implementada quando existir edição de vínculos/permissões. Não confundir a proteção atual do próprio status com essa entrega futura.

## Perfis iniciais: resumo, não autorização adicional

| Perfil | Intenção e limites |
|---|---|
| DIRETORIA | Permissões iniciais completas e autoridade administrativa superior |
| ADMINISTRADOR | Amplo acesso inicial, exceto permissão de gerenciar permissões do Administrador; subordinado à hierarquia |
| RESPONSAVEL_TECNICO | Consultas e catálogo de poderes técnicos; fluxos de bloqueio/liberação ainda pendentes |
| QUALIDADE | Consultas e permissão catalogada de bloqueio; não recebe saldo físico por padrão |
| ESTOQUE | Operações de recebimento/expedição, cadastro de lotes, quantidades e histórico |
| LOGISTICA | Expedição e consultas; quantidade disponível catalogada não libera a tela de saldo físico |
| COMPRAS | Produtos, quantidades física/disponível e custo catalogado |
| VENDAS | Produtos, quantidades física/disponível e preço catalogado |
| FINANCEIRO | Custo/margem catalogados; módulo financeiro pendente |
| TI | Consulta de usuários e permissões técnicas/auditoria catalogadas; sem autoridade comercial automática |
| CONSULTA | Nenhuma permissão operacional inicial |

As descrições detalhadas definidas pelo negócio prevalecem sobre resumos. A V10 é a fonte dos vínculos iniciais efetivos, não uma autorização para funções inexistentes.

## Limites e próximos controles

- Autorizações dos demais domínios concentram-se nas rotas; não existe proteção uniforme de métodos de serviço.
- Sessões mantêm autoridades carregadas no login; revogação/ativação não atualiza automaticamente toda sessão já aberta.
- Logout existe; recuperação segura, tentativas inválidas, bloqueio configurável, sessão inventariada, timeout de negócio e 2FA ainda precisam de implementação/validação.
- Não há auditoria geral de antes/depois, IP, sessão e justificativa. Responsável/instante de confirmação não substituem essa trilha.
- Custos, preços e margens têm códigos de permissões, mas não dados ou telas comerciais implementados.
- A restrição de saldo na rastreabilidade não elimina possíveis inferências por quantidades em documentos, mensagens de saldo insuficiente ou outras rotas. Revisar a política de dados em todos esses pontos.
- Proibição de exclusão deve alcançar serviço, banco operacional e processos de suporte; ausência de botão de exclusão não torna registros imutáveis.

Aceite: matriz de testes por operação, papel, permissão, destino e sessão; negativos de autoelevação, múltiplos perfis, permissão revogada, requisição direta e CSRF. A lacuna atual de testes de quantidades está registrada em [validação](../10-validacao-e-evidencias.md).
