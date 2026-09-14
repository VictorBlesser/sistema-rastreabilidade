package com.portfolio.rastreabilidade.recebimento;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public class RecebimentoForm {

    @NotBlank(message = "O fornecedor é obrigatório")
    @Size(max = 150, message = "Informe no máximo 150 caracteres")
    private String fornecedor;

    @NotBlank(message = "O documento é obrigatório")
    @Size(max = 100, message = "Informe no máximo 100 caracteres")
    private String documento;

    @NotNull(message = "A data é obrigatória")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataRecebimento = LocalDate.now();

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDate dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }
}