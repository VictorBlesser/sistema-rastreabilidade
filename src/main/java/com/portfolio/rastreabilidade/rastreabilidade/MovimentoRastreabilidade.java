package com.portfolio.rastreabilidade.rastreabilidade;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;

public interface MovimentoRastreabilidade {

    Long getId();

    TipoMovimentacao getTipo();

    BigDecimal getQuantidade();

    OffsetDateTime getRegistradoEm();

    String getResponsavel();

    String getParticipante();

    String getDocumento();

    Long getRecebimentoId();

    Long getExpedicaoId();
}