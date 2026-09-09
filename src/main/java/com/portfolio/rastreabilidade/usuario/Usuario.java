package com.portfolio.rastreabilidade.usuario;

public class Usuario {

    private Long id;
    private String nome;
    private String login;
    private String senhaHash;
    private PerfilUsuario perfil;
    private boolean ativo;

    protected Usuario() {
    }

    public Usuario(
            String nome,
            String login,
            String senhaHash,
            PerfilUsuario perfil) {

        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = true;
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

public boolean isAtivo() {
    return ativo;
}

public void inativar() {
    this.ativo = false;
}
}