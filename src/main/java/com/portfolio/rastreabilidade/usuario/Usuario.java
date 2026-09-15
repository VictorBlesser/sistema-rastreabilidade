package com.portfolio.rastreabilidade.usuario;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilUsuario perfil;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_perfil",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private Set<PerfilAcesso> perfis = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean ativo;

    protected Usuario() {
    }

    public Usuario(
            String nome,
            String login,
            String senhaHash,
            PerfilUsuario perfil) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório");
        }

        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("O hash da senha é obrigatório");
        }

        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório");
        }

        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = true;
    }

    void vincularPerfilInicial(PerfilAcesso perfilAcesso) {
        if (id != null || !perfis.isEmpty()) {
            throw new IllegalStateException(
                    "O perfil inicial só pode ser vinculado no cadastro");
        }

        if (perfilAcesso == null || perfilAcesso.getId() == null) {
            throw new IllegalArgumentException(
                    "O perfil de acesso precisa estar cadastrado");
        }

        if (!perfil.name().equals(perfilAcesso.getCodigo())) {
            throw new IllegalArgumentException(
                    "O perfil de acesso não corresponde ao perfil informado");
        }

        perfis.add(perfilAcesso);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public Set<PerfilAcesso> getPerfis() {
        return Collections.unmodifiableSet(perfis);
    }

    public Set<String> getCodigosPermissoes() {
        return perfis.stream()
                .flatMap(perfilAcesso ->
                        perfilAcesso.getPermissoes().stream())
                .map(permissao -> permissao.getCodigo())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }
}