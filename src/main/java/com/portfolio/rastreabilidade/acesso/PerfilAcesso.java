package com.portfolio.rastreabilidade.acesso;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfil_acesso")
public class PerfilAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private boolean sistema;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "perfil_permissao",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id"))
    private Set<Permissao> permissoes = new LinkedHashSet<>();

    protected PerfilAcesso() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isSistema() {
        return sistema;
    }

    public Set<Permissao> getPermissoes() {
        return Collections.unmodifiableSet(permissoes);
    }

    public boolean possuiPermissao(String codigoPermissao) {
        return permissoes.stream()
                .anyMatch(permissao ->
                        permissao.getCodigo().equals(codigoPermissao));
    }
}