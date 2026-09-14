package com.portfolio.rastreabilidade.expedicao;

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
@Table(name = "expedicao")
public class Expedicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(nullable = false, length = 100)
    private String documento;

    @Column(name = "data_expedicao", nullable = false)
    private LocalDate dataExpedicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusExpedicao status;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "confirmado_em")
    private OffsetDateTime confirmadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmado_por")
    private Usuario confirmadoPor;

    @OneToMany(
            mappedBy = "expedicao",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ExpedicaoItem> itens = new ArrayList<>();

    protected Expedicao() {
    }

    public Expedicao(
            String destinatario,
            String documento,
            LocalDate dataExpedicao) {

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "O destinatário é obrigatório");
        }

        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException(
                    "O documento é obrigatório");
        }

        if (dataExpedicao == null) {
            throw new IllegalArgumentException(
                    "A data da expedição é obrigatória");
        }

        String destinatarioNormalizado = destinatario.strip();
        String documentoNormalizado = documento.strip();

        if (destinatarioNormalizado.length() > 150) {
            throw new IllegalArgumentException(
                    "O destinatário deve ter no máximo 150 caracteres");
        }

        if (documentoNormalizado.length() > 100) {
            throw new IllegalArgumentException(
                    "O documento deve ter no máximo 100 caracteres");
        }

        this.destinatario = destinatarioNormalizado;
        this.documento = documentoNormalizado;
        this.dataExpedicao = dataExpedicao;
        this.status = StatusExpedicao.RASCUNHO;
        this.criadoEm = OffsetDateTime.now();
    }

    public void adicionarItem(ExpedicaoItem item) {
        exigirRascunho();

        if (item == null) {
            throw new IllegalArgumentException(
                    "O item é obrigatório");
        }

        if (item.getExpedicao() != this) {
            throw new IllegalArgumentException(
                    "O item pertence a outra expedição");
        }

        if (itens.contains(item)) {
            throw new IllegalArgumentException(
                    "O item já foi adicionado");
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

        for (ExpedicaoItem item : itens) {
            item.validar();
        }

        this.status = StatusExpedicao.CONFIRMADO;
        this.confirmadoEm = OffsetDateTime.now();
        this.confirmadoPor = usuario;
    }

    public void exigirRascunho() {
        if (status != StatusExpedicao.RASCUNHO) {
            throw new IllegalArgumentException(
                    "A expedição já foi confirmada");
        }
    }

    public Long getId() {
        return id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getDataExpedicao() {
        return dataExpedicao;
    }

    public StatusExpedicao getStatus() {
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

    public List<ExpedicaoItem> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public boolean isRascunho() {
        return status == StatusExpedicao.RASCUNHO;
    }
}