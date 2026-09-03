# Sistema de Rastreabilidade

Sistema interno de rastreabilidade e qualidade, construído em Java com Spring Boot.

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
- [ ] Usuários e permissões
- [ ] Produtos e lotes — telas de cadastro e listagem concluídas
- [ ] Recebimento e estoque
- [ ] Trilha de auditoria

## Regra do projeto

Nenhum módulo regulado entra em produção apenas porque funciona. Cada requisito deverá possuir avaliação de risco, teste e evidência de aprovação.
