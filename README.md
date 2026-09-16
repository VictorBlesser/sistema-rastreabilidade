# Sistema de Rastreabilidade (A ideia é transformar em um sistema de ERP completo com vários módulos)

Sistema interno de rastreabilidade e qualidade, construído em Java com Spring Boot.

## Requisito de desenvolvimento

- JDK 25, conforme configurado no `pom.xml`.

## Executar no VS Code

1. Abra esta pasta no VS Code.
2. Abra o terminal integrado.
3. Execute `./mvnw.cmd spring-boot:run`.
4. Acesse `http://localhost:8080` no navegador.
5. Para encerrar, volte ao terminal e pressione `Ctrl+C`.

## Etapa atual

- [x] Projeto Spring Boot criado
- [x] Banco local configurado
- [x] Histórico de alterações do banco com Flyway
- [x] Página inicial
- [x] Estrutura inicial de segurança
- [x] Especificação do primeiro módulo
- [ ] Usuários e permissões - fiz algumas alterações no planejamento dessa fase portanto vou demorar bem mais que o esperado inicialmente.(estou nessa fase atualmente)
- [x] Produtos — cadastro, listagem, consulta e inativação da primeira versão concluídos
- [ ] Lotes e números de série
- [ ] Recebimento e estoque
- [ ] Trilha de auditoria

## Regra do projeto

Nenhum módulo regulado entra em produção apenas porque funciona. Cada requisito deverá possuir avaliação de risco, teste e evidência de aprovação.
Tudo que está sendo publicado no github não é necessáriamente como o sistema é, pois vou colocar em funcionamento e isso demanda alguns requisitos de segurança. (como estou iniciando pode haver varios pontos falhos nesse quêsito portanto estou estudando e me aprofundando para para corrigir esses erros.)
