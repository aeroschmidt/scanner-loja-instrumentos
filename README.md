# Som & Corda — loja de instrumentos

Aplicação de demonstração em Java 21 e Maven para aprender o caminho do código até uma análise no SonarQube. Tem uma página web para consultar/manter itens, API REST, banco H2 em memória e testes de integração.

Pasta local: `C:\Applications\git\loja-instrumentos`.

## Abrir no Eclipse

1. No Eclipse, escolha **File > Import > Maven > Existing Maven Projects**.
2. Selecione esta pasta (a que contém `pom.xml`) e finalize a importação.
3. Aguarde o Maven baixar as dependências. Configure um JDK 21 em **Window > Preferences > Java > Installed JREs** se o Eclipse ainda não tiver um.
4. Execute `LojaInstrumentosApplication` como **Java Application**.
5. Abra `http://localhost:8080`.

Também há metadados `.project` e `.classpath` para IDEs Eclipse que usem m2e; a importação Maven é a forma recomendada.

## Executar com Maven

É necessário JDK 21. O Maven Wrapper baixa e usa Maven 3.10.0 sem exigir que o Maven esteja instalado no PATH. Em um terminal nesta pasta:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

O primeiro comando compila o código e executa os testes. O segundo inicia a aplicação; acesse `http://localhost:8080`. Os dados de exemplo voltam ao estado inicial quando a aplicação reinicia porque o H2 é em memória.

## Integrar ao SonarQube local

A chave deste projeto é `loja-instrumentos-musicais` e fica no `pom.xml`. O SonarScanner para Maven lê os fontes e testes do layout Maven, além do bytecode que o build acabou de produzir.

1. Inicie seu SonarQube local e confirme que a tela abre em `http://localhost:9000`.
2. Na primeira análise, crie no SonarQube um projeto com a chave `loja-instrumentos-musicais` (ou use a opção de criar o projeto durante a análise, se a edição oferecer essa opção).
3. Gere um token de análise para esse projeto em **My Account > Security**. Não use o token como valor literal em arquivos versionados.
4. No PowerShell, defina o token apenas para a sessão atual. Cole o valor localmente no prompt seguro:

```powershell
$env:SONAR_TOKEN = Read-Host 'Cole o token de análise do SonarQube'
$env:SONAR_HOST_URL = 'http://localhost:9000'
.\mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar
Remove-Item Env:SONAR_TOKEN
```

Se o SonarQube já criou o projeto durante a análise, a etapa 2 não é necessária. A chave é fixa no `pom.xml`; o endereço pode ser passado em `SONAR_HOST_URL` ou com `-Dsonar.host.url=...`. A autenticação usa `SONAR_TOKEN` pelo ambiente, para evitar pôr o segredo no comando ou no repositório.

O comando faz duas etapas no mesmo Maven build: `clean verify` recompila e executa testes; `sonar:sonar` envia a análise para o servidor. A URL do painel aparece no final da saída do scanner. Em **Project Settings > Quality Gate**, confira o resultado da avaliação de qualidade.

### O que foi configurado

- `sonar.projectKey` e `sonar.projectName` no `pom.xml` identificam o projeto no servidor.
- `sonar-maven-plugin` tem versão fixa para que uma mudança de release não altere a análise silenciosamente.
- O JDK do projeto é 21 e a análise é executada depois do build, para fornecer os `.class` ao analisador Java.
- Nenhum token, senha ou endereço de repositório GitHub está versionado.

## GitHub Actions e segurança do repositório

### Como está funcionando

O workflow em `.github/workflows/build.yml` é executado em pushes para `main` e em pull requests destinados a `main`. Ele usa um runner hospedado pelo GitHub, prepara o Java 21 e executa `./mvnw verify` para compilar e testar o projeto. Nesta etapa de aprendizado, o workflow não chama o SonarQube Cloud e não usa `SONAR_TOKEN`.

O GitHub Actions reage aos eventos e organiza a execução; o Maven compila e roda os testes. O check de build aparece no pull request. A análise do Sonar foi temporariamente removida do workflow para observar primeiro os checks nativos do GitHub sem misturá-los com o Quality Gate. A integração local com SonarQube continua documentada na seção anterior.

O repositório já foi conectado ao SonarQube Cloud e análises anteriores em pull requests mostraram o Quality Gate. Esses resultados são parte do histórico; o workflow desta etapa não envia análises ao Sonar.

O PR #6 foi um teste temporário sem a chamada do Sonar e foi fechado sem merge. A remoção atual está sendo preparada no PR #7. Depois que você revisar e aprovar, o merge poderá atualizar a `main`.

### Estado dos recursos de segurança

Neste momento, CodeQL/Code Scanning, Dependabot e Secret Protection estão desligados no repositório. A configuração observada em **Settings > Advanced Security** também mostra o Dependency graph desligado. Em repositórios públicos, a detecção de padrões de segredos para alertar provedores parceiros pode ocorrer automaticamente mesmo quando o Secret Protection e os alertas para o proprietário estão desligados; consulte a documentação oficial antes de interpretar isso como ausência absoluta de detecção.

Copilot Autofix aparece ligado nas configurações, mas depende de alertas do CodeQL para sugerir correções e não substitui uma análise ativa. Não o alteramos nesta etapa.

O estado observado e o procedimento para ativar cada recurso separadamente estão no [Guia de GitHub Actions e segurança](docs/guia-github-actions-e-seguranca.docx). A inclusão das capturas como imagens no guia fica pendente para a próxima revisão documental.

### Próximos passos de estudo

Vamos aprender uma coisa por vez, observando o que cada check faz antes de ativar a próxima ferramenta:

1. Revisar e aprovar o PR #7 manualmente; nenhum PR deve ser aprovado ou mesclado pelo assistente.
2. Entender o workflow atual: eventos (`push` e `pull_request`), jobs, steps, logs e checks no GitHub Actions.
3. Quando decidido, habilitar e estudar o Dependabot em um PR de teste; depois avaliar separadamente Code Scanning com CodeQL e Secret Scanning.
4. Reintroduzir Sonar somente quando solicitado, para observar a interação com os checks do GitHub.

Este README e o guia documentam os recursos, mas não ativam Dependabot, CodeQL nem Secret Protection. O CircleCI também não faz parte do workflow atual; só será explorado se houver um motivo ou pedido específico.

## API

- `GET /api/products` — lista itens
- `GET /api/products/{id}` — consulta um item
- `POST /api/products` — cadastra item
- `PUT /api/products/{id}` — atualiza item
- `DELETE /api/products/{id}` — remove item

Exemplo de corpo JSON: `{"name":"Violão","category":"Cordas","price":799.90,"stock":8}`.
