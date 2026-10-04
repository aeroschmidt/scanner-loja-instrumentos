# SonarQube Cloud no GitHub Actions

Este projeto executa build, testes e análise estática no GitHub Actions usando o SonarQube Cloud.

## Como a configuração funciona

- `.github/workflows/build.yml` inicia a pipeline quando há `push` em `main` ou quando um pull request tem `main` como destino.
- O workflow prepara o Java 21, compila e executa os testes com o Maven Wrapper (`./mvnw verify`).
- Em seguida, o objetivo `org.sonarsource.scanner.maven:sonar-maven-plugin:sonar` envia a análise para o SonarQube Cloud.
- `pom.xml` identifica a organização `aeroschmidt` e o projeto `aeroschmidt_scanner-loja-instrumentos`.
- `fetch-depth: 0` baixa o histórico Git completo, que o scanner usa para atribuir alterações e informações de autoria.
- A variável `SONAR_TOKEN` autentica o scanner. Ela é lida de um GitHub Actions secret e não deve ser gravada no código nem enviada em mensagens.

## Configuração necessária no GitHub

1. No SonarQube Cloud, abra o projeto e crie um token com permissão para executar análise. Copie-o na hora da criação e mantenha-o privado.
2. No repositório GitHub `aeroschmidt/scanner-loja-instrumentos`, abra **Settings > Secrets and variables > Actions > New repository secret**.
3. Crie o secret com o nome `SONAR_TOKEN` e cole o token no campo de valor. Não coloque o valor do token em arquivos do repositório.
4. Depois que essa configuração estiver salva, um push ou pull request para `main` executará a análise.

## Resultado esperado

O log da etapa **Build, test, and analyze with SonarQube Cloud** deve terminar com sucesso. A análise aparecerá na página do projeto SonarQube Cloud. Para ver a análise associada ao PR, o projeto precisa estar vinculado ao repositório GitHub e a GitHub App do SonarQube Cloud precisa ter as permissões de Checks habilitadas.

## Diagnóstico rápido

- `SONAR_TOKEN` ausente: confirme que o secret tem exatamente esse nome e está no repositório certo.
- Projeto não encontrado ou sem permissão: confira `sonar.projectKey`, `sonar.organization` e se o usuário que gerou o token pode executar análise no projeto.
- Build verde, análise ausente: abra os logs da etapa de análise e confirme que ela foi executada no mesmo job após `verify`.
