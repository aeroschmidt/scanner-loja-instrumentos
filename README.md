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

## GitHub Actions e SonarCloud

O workflow em `.github/workflows/build.yml` roda nos pushes para `main` e nos pull requests destinados a `main`. Ele prepara o Java 21 e executa `./mvnw --batch-mode --no-transfer-progress verify`; na configuração normal da `main`, em seguida também chama o scanner do SonarCloud usando o secret `SONAR_TOKEN`. O token é injetado pelo GitHub Actions e não deve ser escrito no YAML ou no código.

Este PR é um teste temporário: seu diff remove apenas a chamada `sonar:sonar` e o uso do secret, mantendo o build e os testes Maven. Se essa alteração de workflow fosse mesclada, o Sonar deixaria de rodar nessa pipeline.

## Próximos testes: GitHub Advanced Security (GHAS)

Vamos estudar e testar separadamente algumas ferramentas do GHAS em pull requests: o Dependabot para alertas e atualizações de dependências, o Code Scanning com CodeQL e o Secret Scanning. A ideia é observar o que cada recurso detecta e como seus resultados aparecem no GitHub. A configuração e a ativação serão tratadas em etapas próprias.

## API

- `GET /api/products` — lista itens
- `GET /api/products/{id}` — consulta um item
- `POST /api/products` — cadastra item
- `PUT /api/products/{id}` — atualiza item
- `DELETE /api/products/{id}` — remove item

Exemplo de corpo JSON: `{"name":"Violão","category":"Cordas","price":799.90,"stock":8}`.
