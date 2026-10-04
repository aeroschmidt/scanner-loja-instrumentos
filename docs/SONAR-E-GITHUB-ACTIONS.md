# Guia de aprendizagem: Maven, SonarQube e a futura etapa de Actions

Este documento descreve a configuração que acompanha a aplicação e o fluxo local. Nenhum repositório GitHub foi criado, alterado ou conectado nesta etapa e não há workflow de Actions no projeto.

## Peças do fluxo

| Peça | Papel neste projeto |
|---|---|
| Eclipse | IDE para abrir e editar o projeto Maven. Não executa o Sonar por conta própria. |
| Maven | Lê `pom.xml`, resolve dependências, compila, executa testes e empacota a aplicação. |
| SonarScanner for Maven | Plugin `sonar-maven-plugin` chamado pelo goal `sonar:sonar`; coleta fontes, testes e bytecode e envia a análise ao servidor. |
| SonarQube Server | Serviço local que recebe e processa a análise, aplica o Quality Profile/Gate e mostra os resultados na interface. |
| GitHub Actions (etapa futura) | Runner de CI que fará checkout, preparará JDK/Maven e chamará o mesmo build e scanner após eventos do repositório. |

## Configuração no código

No `pom.xml` estão Java 21, dependências Spring Boot e a versão fixa do plugin Sonar Maven. `sonar.projectKey=loja-instrumentos-musicais` identifica de forma estável o projeto no servidor; `sonar.projectName` é o rótulo legível. Esses valores não são credenciais.

O Scanner for Maven é apropriado aqui porque o Maven conhece as pastas de código e teste, as dependências e os diretórios de bytecode. A análise deve vir depois de `verify`; assim o Sonar recebe classes compiladas e os relatórios de teste do build. Use `mvnw.cmd clean verify ...:sonar` para manter a versão do Maven reproduzível.

## Primeira conexão local

1. Inicie SonarQube em `http://localhost:9000` e entre com sua conta.
2. Crie o projeto no servidor usando a chave `loja-instrumentos-musicais`.
3. Em **My Account > Security**, crie um token de análise com acesso ao projeto. Um token do tipo Project Analysis é preferível quando disponível.
4. Em PowerShell, leia o token sem incluí-lo no histórico do repositório e defina o servidor:

   ```powershell
   $env:SONAR_TOKEN = Read-Host 'Cole o token de análise do SonarQube'
   $env:SONAR_HOST_URL = 'http://localhost:9000'
   .\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar
   Remove-Item Env:SONAR_TOKEN
   ```

   O campo de token do prompt do PowerShell aparece como texto. Para maior sigilo, pode-se usar o gerenciador de credenciais do sistema ou uma variável protegida no terminal. Não salve o token em `pom.xml`, scripts, logs, prints ou commits.

5. Abra o dashboard do projeto indicado na saída do Maven e examine Issues, Measures e Quality Gate.

O servidor SonarQube pode criar projeto automaticamente em alguns modos; se isso estiver habilitado, a etapa de criação manual pode ser omitida. A análise requer permissão **Execute Analysis** no projeto. Não confunda o token do Sonar com uma senha do GitHub.

## O que cada etapa do comando faz

```text
.\mvnw.cmd clean verify sonar-maven-plugin:<versão>:sonar
│   │     │      └─ envia metadados e resultados de análise ao SonarQube
│   │     └──────── compila e executa testes até a fase verify
│   └────────────── remove resultados antigos de target/
└────────────────── executa o Maven do projeto
```

O plugin lê a chave e o nome do `pom.xml`, usa `SONAR_HOST_URL` para o servidor e `SONAR_TOKEN` para autenticação. O token deve ser transmitido por um mecanismo secreto e nunca como propriedade em texto aberto na linha de comando.

## O passo de GitHub Actions que faremos juntos depois

Quando conectar o repositório, o workflow será um arquivo YAML em `.github/workflows/`. Em termos didáticos, ele terá gatilhos (por exemplo, push e pull request), um runner, passos para obter o código, configurar JDK/Maven, rodar `mvnw.cmd clean verify` e chamar o scanner. O segredo ficará em **GitHub Settings > Secrets and variables > Actions** e será injetado no processo do Maven como `SONAR_TOKEN`. O histórico dos jobs mostrará a sequência e o resultado de cada passo.

**Limite do servidor local:** `http://localhost:9000` é interpretado no computador do runner. Um runner hospedado pelo GitHub está fora da rede local e não consegue acessar esse endereço. Na próxima conversa, antes do workflow, precisamos decidir entre tornar um SonarQube acessível pela rede ao runner ou usar um runner self-hosted no computador/rede que já alcança o Sonar local. Abrir a porta do servidor à internet muda a exposição da instalação; não será feito nesta etapa.

## Verificação desta etapa

Ao terminar a preparação, registre aqui os resultados de `mvnw.cmd clean verify` e da primeira análise, incluindo o status do Quality Gate. O projeto ainda não foi publicado no GitHub.
