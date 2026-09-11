package com.portfolio.rastreabilidade.recebimento;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.portfolio.rastreabilidade.usuario.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "recebimento")
public class Recebimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String fornecedor;

    @Column(nullable = false, length = 100)
    private String documento;

    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusRecebimento status;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "confirmado_em")
    private OffsetDateTime confirmadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmado_por")
    private Usuario confirmadoPor;

    @OneToMany(
            mappedBy = "recebimento",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<RecebimentoItem> itens = new ArrayList<>();

    protected Recebimento() {
    }

    public Recebimento(
            String fornecedor,
            String documento,
            LocalDate dataRecebimento) {

        if (fornecedor == null || fornecedor.isBlank()) {
            throw new IllegalArgumentException("O fornecedor é obrigatório");
        }

        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("O documento é obrigatório");
        }

        if (dataRecebimento == null) {
            throw new IllegalArgumentException("A data é obrigatória");
        }

        String fornecedorNormalizado = fornecedor.strip();
        String documentoNormalizado = documento.strip();

        if (fornecedorNormalizado.length() > 150) {
            throw new IllegalArgumentException(
                    "O fornecedor deve ter no máximo 150 caracteres");
        }

        if (documentoNormalizado.length() > 100) {
            throw new IllegalArgumentException(
                    "O documento deve ter no máximo 100 caracteres");
        }

        this.fornecedor = fornecedorNormalizado;
        this.documento = documentoNormalizado;
        this.dataRecebimento = dataRecebimento;
        this.status = StatusRecebimento.RASCUNHO;
        this.criadoEm = OffsetDateTime.now();
    }

    public void adicionarItem(RecebimentoItem item) {
        exigirRascunho();

        if (item == null) {
            throw new IllegalArgumentException("O item é obrigatório");
        }

        if (item.getRecebimento() != this) {
            throw new IllegalArgumentException(
                    "O item pertence a outro recebimento");
        }

        if (itens.contains(item)) {
            throw new IllegalArgumentException("O item já foi adicionado");
        }

        itens.add(item);
    }

    public void confirmar(Usuario usuario) {
        exigirRascunho();

        if (itens.isEmpty()) {
            throw new IllegalArgumentException(
                    "Adicione pelo menos um item antes de confirmar");
        }

        if (usuario == null || !usuario.isAtivo()) {
            throw new IllegalArgumentException(
                    "Um usuário ativo é obrigatório para confirmar");
        }

        for (RecebimentoItem item : itens) {
            item.validar();
        }

        this.status = StatusRecebimento.CONFIRMADO;
        this.confirmadoEm = OffsetDateTime.now();
        this.confirmadoPor = usuario;
    }

    public void exigirRascunho() {
        if (status != StatusRecebimento.RASCUNHO) {
            throw new IllegalArgumentException(
                    "O recebimento já foi confirmado");
        }
    }

    public Long getId() {
        return id;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    public StatusRecebimento getStatus() {
        return status;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getConfirmadoEm() {
        return confirmadoEm;
    }

    public Usuario getConfirmadoPor() {
        return confirmadoPor;
    }

    public List<RecebimentoItem> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public boolean isRascunho() {
        return status == StatusRecebimento.RASCUNHO;
    }
}