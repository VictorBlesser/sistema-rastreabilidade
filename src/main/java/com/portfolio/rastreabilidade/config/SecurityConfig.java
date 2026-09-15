package com.portfolio.rastreabilidade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/actuator/health")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/produtos/novo")
                        .hasAuthority("PRODUTO_CADASTRAR")
                        .requestMatchers(HttpMethod.POST, "/produtos")
                        .hasAuthority("PRODUTO_CADASTRAR")
                        .requestMatchers(HttpMethod.POST, "/produtos/*/inativar")
                        .hasAuthority("PRODUTO_INATIVAR")
                        .requestMatchers(HttpMethod.GET, "/produtos", "/produtos/*")
                        .hasAuthority("PRODUTO_VISUALIZAR")
                        .requestMatchers("/produtos", "/produtos/**")
                        .denyAll()

                        .requestMatchers(HttpMethod.GET, "/lotes/novo")
                        .hasAuthority("LOTE_CADASTRAR")
                        .requestMatchers(HttpMethod.POST, "/lotes")
                        .hasAuthority("LOTE_CADASTRAR")
                        .requestMatchers(HttpMethod.GET, "/lotes", "/lotes/*")
                        .hasAuthority("LOTE_VISUALIZAR")
                        .requestMatchers("/lotes", "/lotes/**")
                        .denyAll()

                        .requestMatchers(HttpMethod.GET, "/recebimentos/novo")
                        .hasAuthority("RECEBIMENTO_CRIAR")
                        .requestMatchers(HttpMethod.POST, "/recebimentos")
                        .hasAuthority("RECEBIMENTO_CRIAR")
                        .requestMatchers(HttpMethod.POST, "/recebimentos/*/itens")
                        .hasAuthority("RECEBIMENTO_EDITAR")
                        .requestMatchers(HttpMethod.POST, "/recebimentos/*/confirmar")
                        .hasAuthority("RECEBIMENTO_CONFIRMAR")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/recebimentos",
                                "/recebimentos/*")
                        .hasAuthority("RECEBIMENTO_VISUALIZAR")
                        .requestMatchers("/recebimentos", "/recebimentos/**")
                        .denyAll()

                        .requestMatchers(HttpMethod.GET, "/expedicoes/novo")
                        .hasAuthority("EXPEDICAO_CRIAR")
                        .requestMatchers(HttpMethod.POST, "/expedicoes")
                        .hasAuthority("EXPEDICAO_CRIAR")
                        .requestMatchers(HttpMethod.POST, "/expedicoes/*/itens")
                        .hasAuthority("EXPEDICAO_EDITAR")
                        .requestMatchers(HttpMethod.POST, "/expedicoes/*/confirmar")
                        .hasAuthority("EXPEDICAO_CONFIRMAR")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/expedicoes",
                                "/expedicoes/*")
                        .hasAuthority("EXPEDICAO_VISUALIZAR")
                        .requestMatchers("/expedicoes", "/expedicoes/**")
                        .denyAll()

                        .requestMatchers("/estoque", "/estoque/**")
                        .hasAuthority("ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR")
                        .requestMatchers("/rastreabilidade", "/rastreabilidade/**")
                        .hasAuthority("RASTREABILIDADE_VISUALIZAR")

                        .requestMatchers(HttpMethod.GET, "/usuarios/novo")
                        .access(AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.hasAnyRole(
                                        "DIRETORIA", "ADMINISTRADOR"),
                                AuthorityAuthorizationManager.hasAuthority(
                                        "USUARIO_CRIAR"),
                                AuthorityAuthorizationManager.hasAuthority(
                                        "USUARIO_VINCULAR_PERFIS")))

                        .requestMatchers(HttpMethod.POST, "/usuarios")
                        .access(AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.hasAnyRole(
                                        "DIRETORIA", "ADMINISTRADOR"),
                                AuthorityAuthorizationManager.hasAuthority(
                                        "USUARIO_CRIAR"),
                                AuthorityAuthorizationManager.hasAuthority(
                                        "USUARIO_VINCULAR_PERFIS")))

                        .requestMatchers(
                                HttpMethod.POST,
                                "/usuarios/*/ativar",
                                "/usuarios/*/inativar")
                        .access(AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.hasAnyRole(
                                        "DIRETORIA", "ADMINISTRADOR"),
                                AuthorityAuthorizationManager.hasAuthority(
                                        "USUARIO_ALTERAR_STATUS")))

                        .requestMatchers(HttpMethod.GET, "/usuarios", "/usuarios/*")
                        .hasAuthority("USUARIO_VISUALIZAR")
                        .requestMatchers("/usuarios", "/usuarios/**")
                        .denyAll()

                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}