package com.portfolio.rastreabilidade.acesso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PerfilAcessoIntegrationTest {

    @Autowired
    private PerfilAcessoRepository perfilRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Test
    void diretoriaDevePossuirTodasAsPermissoesDoCatalogo() {
        Set<String> catalogo = permissaoRepository
                .findAll(Sort.by("codigo"))
                .stream()
                .map(Permissao::getCodigo)
                .collect(Collectors.toSet());

        assertFalse(catalogo.isEmpty());
        assertEquals(catalogo, codigos("DIRETORIA"));
    }

    @Test
    void somenteDiretoriaDeveReceberGerenciamentoDoAdministrador() {
        Set<String> autorizados = perfilRepository
                .findAll(Sort.by("codigo"))
                .stream()
                .filter(perfil -> perfil.possuiPermissao(
                        "PERMISSAO_ADMINISTRADOR_GERENCIAR"))
                .map(PerfilAcesso::getCodigo)
                .collect(Collectors.toSet());

        assertEquals(Set.of("DIRETORIA"), autorizados);

        assertTrue(perfil("ADMINISTRADOR")
                .possuiPermissao("PERMISSAO_GERENCIAR"));
    }

    @Test
    void consultaDeveComecarSemPermissoes() {
        assertTrue(codigos("CONSULTA").isEmpty());
    }

    @Test
    void tiDeveReceberSomenteAcessosTecnicosIniciais() {
        assertEquals(
                Set.of(
                        "USUARIO_VISUALIZAR",
                        "AUDITORIA_VISUALIZAR",
                        "CONFIGURACAO_TECNICA_ALTERAR"),
                codigos("TI"));
    }

    @Test
    void estoqueDeveMovimentarSemAdministrarProdutosOuPermissoes() {
        PerfilAcesso estoque = perfil("ESTOQUE");

        assertTrue(estoque.possuiPermissao("LOTE_CADASTRAR"));
        assertTrue(estoque.possuiPermissao("RECEBIMENTO_CONFIRMAR"));
        assertTrue(estoque.possuiPermissao("EXPEDICAO_CONFIRMAR"));
        assertTrue(estoque.possuiPermissao("RASTREABILIDADE_VISUALIZAR"));

        assertFalse(estoque.possuiPermissao("PRODUTO_CADASTRAR"));
        assertFalse(estoque.possuiPermissao("PERMISSAO_GERENCIAR"));
        assertFalse(estoque.possuiPermissao("CUSTO_VISUALIZAR"));
    }

    @Test
    void logisticaDeveExpedirSemReceberMercadorias() {
        PerfilAcesso logistica = perfil("LOGISTICA");

        assertTrue(logistica.possuiPermissao("EXPEDICAO_CRIAR"));
        assertTrue(logistica.possuiPermissao("EXPEDICAO_CONFIRMAR"));
        assertTrue(logistica.possuiPermissao(
                "ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR"));

        assertFalse(logistica.possuiPermissao("RECEBIMENTO_VISUALIZAR"));
        assertFalse(logistica.possuiPermissao("RECEBIMENTO_CONFIRMAR"));
        assertFalse(logistica.possuiPermissao("CUSTO_ALTERAR"));
    }

    @Test
    void vendasDeveConsultarPrecoSemReceberCustoOuMargem() {
        PerfilAcesso vendas = perfil("VENDAS");

        assertTrue(vendas.possuiPermissao("PRECO_VENDA_VISUALIZAR"));
        assertTrue(vendas.possuiPermissao(
                "ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR"));

        assertFalse(vendas.possuiPermissao("CUSTO_VISUALIZAR"));
        assertFalse(vendas.possuiPermissao("MARGEM_VISUALIZAR"));
        assertFalse(vendas.possuiPermissao("PRECO_VENDA_ALTERAR"));
    }

    @Test
    void comprasDeveConsultarCustoSemReceberPrecoDeVenda() {
        PerfilAcesso compras = perfil("COMPRAS");

        assertTrue(compras.possuiPermissao("CUSTO_VISUALIZAR"));
        assertTrue(compras.possuiPermissao(
                "ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR"));

        assertFalse(compras.possuiPermissao("PRECO_VENDA_VISUALIZAR"));
        assertFalse(compras.possuiPermissao("CUSTO_ALTERAR"));
    }

    @Test
    void responsavelTecnicoDeveTerEdicaoTecnicaSemEdicaoGeral() {
        PerfilAcesso tecnico = perfil("RESPONSAVEL_TECNICO");

        assertTrue(tecnico.possuiPermissao("PRODUTO_TECNICO_EDITAR"));
        assertTrue(tecnico.possuiPermissao("LOTE_LIBERAR"));

        assertFalse(tecnico.possuiPermissao("PRODUTO_EDITAR"));
        assertFalse(tecnico.possuiPermissao("PERMISSAO_GERENCIAR"));
    }

    @Test
    void qualidadeDeveBloquearLoteSemReceberLiberacaoTecnica() {
        PerfilAcesso qualidade = perfil("QUALIDADE");

        assertTrue(qualidade.possuiPermissao("LOTE_BLOQUEAR"));
        assertTrue(qualidade.possuiPermissao("RASTREABILIDADE_VISUALIZAR"));

        assertFalse(qualidade.possuiPermissao("LOTE_LIBERAR"));
        assertFalse(qualidade.possuiPermissao("CUSTO_VISUALIZAR"));
        assertFalse(qualidade.possuiPermissao("MARGEM_VISUALIZAR"));
    }

    private PerfilAcesso perfil(String codigo) {
        return perfilRepository.findByCodigo(codigo).orElseThrow();
    }

    private Set<String> codigos(String perfilCodigo) {
        return perfil(perfilCodigo).getPermissoes()
                .stream()
                .map(Permissao::getCodigo)
                .collect(Collectors.toSet());
    }
}
