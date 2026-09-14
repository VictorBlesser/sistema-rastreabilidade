package com.portfolio.rastreabilidade.expedicao;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public class ExpedicaoForm {

    @NotBlank(message = "O destinatário é obrigatório")
    @Size(max = 150, message = "Informe no máximo 150 caracteres")
    private String destinatario;

    @NotBlank(message = "O documento é obrigatório")
    @Size(max = 100, message = "Informe no máximo 100 caracteres")
    private String documento;

    @NotNull(message = "A data é obrigatória")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataExpedicao = LocalDate.now();

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public LocalDate getDataExpedicao() {
        return dataExpedicao;
    }

    public void setDataExpedicao(LocalDate dataExpedicao) {
        this.dataExpedicao = dataExpedicao;
    }
}