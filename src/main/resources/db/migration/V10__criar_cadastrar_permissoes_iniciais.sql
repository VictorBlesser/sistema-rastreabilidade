INSERT INTO permissao (codigo, descricao)
VALUES
    ('PRODUTO_VISUALIZAR', 'Consultar produtos'),
    ('PRODUTO_CADASTRAR', 'Cadastrar produtos'),
    ('PRODUTO_EDITAR', 'Editar o cadastro geral de produtos'),
    ('PRODUTO_INATIVAR', 'Inativar produtos'),
    ('PRODUTO_TECNICO_CADASTRAR', 'Cadastrar informações técnicas de produtos'),
    ('PRODUTO_TECNICO_EDITAR', 'Editar informações técnicas de produtos'),

    ('LOTE_VISUALIZAR', 'Consultar lotes e validades'),
    ('LOTE_CADASTRAR', 'Cadastrar lotes'),
    ('LOTE_EDITAR', 'Editar lotes conforme as regras da operação'),
    ('LOTE_BLOQUEAR', 'Bloquear lotes com justificativa'),
    ('LOTE_LIBERAR', 'Liberar lotes conforme as regras técnicas'),

    ('ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR', 'Consultar quantidade física'),
    ('ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR', 'Consultar quantidade disponível'),
    ('ESTOQUE_QUANTIDADE_RESERVADA_VISUALIZAR', 'Consultar quantidade reservada'),

    ('RECEBIMENTO_VISUALIZAR', 'Consultar recebimentos'),
    ('RECEBIMENTO_CRIAR', 'Criar rascunhos de recebimento'),
    ('RECEBIMENTO_EDITAR', 'Alterar itens de recebimentos em rascunho'),
    ('RECEBIMENTO_CONFIRMAR', 'Confirmar entradas de estoque'),
    ('RECEBIMENTO_CANCELAR', 'Cancelar recebimentos conforme as regras da operação'),

    ('EXPEDICAO_VISUALIZAR', 'Consultar expedições'),
    ('EXPEDICAO_CRIAR', 'Criar rascunhos de expedição'),
    ('EXPEDICAO_EDITAR', 'Alterar itens de expedições em rascunho'),
    ('EXPEDICAO_CONFIRMAR', 'Confirmar saídas de estoque'),
    ('EXPEDICAO_CANCELAR', 'Cancelar expedições conforme as regras da operação'),

    ('RASTREABILIDADE_VISUALIZAR', 'Consultar origem e destino dos lotes'),

    ('CUSTO_VISUALIZAR', 'Consultar custos'),
    ('CUSTO_ALTERAR', 'Alterar custos com justificativa'),
    ('PRECO_VENDA_VISUALIZAR', 'Consultar preços de venda'),
    ('PRECO_VENDA_ALTERAR', 'Alterar preços de venda com justificativa'),
    ('MARGEM_VISUALIZAR', 'Consultar margens'),

    ('USUARIO_VISUALIZAR', 'Consultar usuários'),
    ('USUARIO_CRIAR', 'Criar usuários'),
    ('USUARIO_EDITAR', 'Editar dados cadastrais de usuários'),
    ('USUARIO_ALTERAR_STATUS', 'Alterar o status de usuários'),
    ('USUARIO_VINCULAR_PERFIS', 'Gerenciar vínculos entre usuários e perfis'),

    ('PERFIL_VISUALIZAR', 'Consultar perfis e suas permissões'),
    ('PERFIL_CRIAR', 'Criar perfis personalizados'),
    ('PERFIL_EDITAR', 'Editar dados de perfis personalizados'),
    ('PERMISSAO_GERENCIAR', 'Gerenciar permissões dentro dos limites administrativos'),
    ('PERMISSAO_ADMINISTRADOR_GERENCIAR', 'Alterar permissões do Administrador pela Diretoria'),

    ('AUDITORIA_VISUALIZAR', 'Consultar registros de auditoria'),
    ('CONFIGURACAO_OPERACIONAL_ALTERAR', 'Alterar configurações operacionais'),
    ('CONFIGURACAO_TECNICA_ALTERAR', 'Alterar configurações técnicas');

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, a.id
FROM perfil_acesso p
CROSS JOIN permissao a
WHERE p.codigo = 'DIRETORIA';

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, a.id
FROM perfil_acesso p
CROSS JOIN permissao a
WHERE p.codigo = 'ADMINISTRADOR'
  AND a.codigo <> 'PERMISSAO_ADMINISTRADOR_GERENCIAR';

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, a.id
FROM perfil_acesso p
JOIN (
    VALUES
        ('RESPONSAVEL_TECNICO', 'PRODUTO_VISUALIZAR'),
        ('RESPONSAVEL_TECNICO', 'PRODUTO_TECNICO_CADASTRAR'),
        ('RESPONSAVEL_TECNICO', 'PRODUTO_TECNICO_EDITAR'),
        ('RESPONSAVEL_TECNICO', 'LOTE_VISUALIZAR'),
        ('RESPONSAVEL_TECNICO', 'LOTE_BLOQUEAR'),
        ('RESPONSAVEL_TECNICO', 'LOTE_LIBERAR'),
        ('RESPONSAVEL_TECNICO', 'ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR'),
        ('RESPONSAVEL_TECNICO', 'RECEBIMENTO_VISUALIZAR'),
        ('RESPONSAVEL_TECNICO', 'EXPEDICAO_VISUALIZAR'),
        ('RESPONSAVEL_TECNICO', 'RASTREABILIDADE_VISUALIZAR'),

        ('QUALIDADE', 'PRODUTO_VISUALIZAR'),
        ('QUALIDADE', 'LOTE_VISUALIZAR'),
        ('QUALIDADE', 'LOTE_BLOQUEAR'),
        ('QUALIDADE', 'RECEBIMENTO_VISUALIZAR'),
        ('QUALIDADE', 'EXPEDICAO_VISUALIZAR'),
        ('QUALIDADE', 'RASTREABILIDADE_VISUALIZAR'),

        ('ESTOQUE', 'PRODUTO_VISUALIZAR'),
        ('ESTOQUE', 'LOTE_VISUALIZAR'),
        ('ESTOQUE', 'LOTE_CADASTRAR'),
        ('ESTOQUE', 'LOTE_EDITAR'),
        ('ESTOQUE', 'ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR'),
        ('ESTOQUE', 'ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR'),
        ('ESTOQUE', 'ESTOQUE_QUANTIDADE_RESERVADA_VISUALIZAR'),
        ('ESTOQUE', 'RECEBIMENTO_VISUALIZAR'),
        ('ESTOQUE', 'RECEBIMENTO_CRIAR'),
        ('ESTOQUE', 'RECEBIMENTO_EDITAR'),
        ('ESTOQUE', 'RECEBIMENTO_CONFIRMAR'),
        ('ESTOQUE', 'EXPEDICAO_VISUALIZAR'),
        ('ESTOQUE', 'EXPEDICAO_CRIAR'),
        ('ESTOQUE', 'EXPEDICAO_EDITAR'),
        ('ESTOQUE', 'EXPEDICAO_CONFIRMAR'),
        ('ESTOQUE', 'RASTREABILIDADE_VISUALIZAR'),

        ('LOGISTICA', 'PRODUTO_VISUALIZAR'),
        ('LOGISTICA', 'LOTE_VISUALIZAR'),
        ('LOGISTICA', 'ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR'),
        ('LOGISTICA', 'EXPEDICAO_VISUALIZAR'),
        ('LOGISTICA', 'EXPEDICAO_CRIAR'),
        ('LOGISTICA', 'EXPEDICAO_EDITAR'),
        ('LOGISTICA', 'EXPEDICAO_CONFIRMAR'),

        ('COMPRAS', 'PRODUTO_VISUALIZAR'),
        ('COMPRAS', 'ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR'),
        ('COMPRAS', 'ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR'),
        ('COMPRAS', 'CUSTO_VISUALIZAR'),

        ('VENDAS', 'PRODUTO_VISUALIZAR'),
        ('VENDAS', 'ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR'),
        ('VENDAS', 'ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR'),
        ('VENDAS', 'PRECO_VENDA_VISUALIZAR'),

        ('FINANCEIRO', 'CUSTO_VISUALIZAR'),
        ('FINANCEIRO', 'MARGEM_VISUALIZAR'),

        ('TI', 'USUARIO_VISUALIZAR'),
        ('TI', 'AUDITORIA_VISUALIZAR'),
        ('TI', 'CONFIGURACAO_TECNICA_ALTERAR')
) AS vinculo(perfil_codigo, permissao_codigo)
    ON vinculo.perfil_codigo = p.codigo
JOIN permissao a
    ON a.codigo = vinculo.permissao_codigo;