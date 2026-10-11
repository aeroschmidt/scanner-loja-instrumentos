# Som & Corda — loja de instrumentos

Aplicação de demonstração em Java 21 e Maven para praticar desenvolvimento e verificações de repositório. Inclui uma página web, API REST, banco H2 em memória e testes de integração.

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

## GitHub Actions e segurança do repositório

O workflow em `.github/workflows/build.yml` roda em pushes para `main` e em pull requests destinados a `main`. Em um runner hospedado pelo GitHub, configura o Java 21 e executa `./mvnw verify` para compilar e testar. A análise do CodeQL é configurada separadamente nas opções de segurança do repositório.

O Actions reage aos eventos e organiza jobs e steps; o Maven compila e executa os testes. Em um PR, consulte **Checks** para ver o resultado e abra os logs de um step que falhou.

### Estado das ferramentas de segurança

- **Code Scanning / CodeQL:** habilitado por Default setup no repositório.
- **Dependabot:** permanece desligado até que a mantenedora escolha iniciar esse teste.
- **Secret Protection:** permanece desligado até que a mantenedora escolha iniciar esse teste.
- **GitHub Actions:** executa o build e os testes Maven descritos acima.

Consulte os guias separados para a administração das ferramentas e para o fluxo de quem cria ou revisa PRs:

- [Guia da mantenedora](docs/guia-github-actions-e-seguranca.docx)
- [Fonte editável do guia da mantenedora](docs/guia-github-actions-e-seguranca.md)
- [Guia de quem cria e revisa PRs](docs/guia-usuario-prs.docx)
- [Fonte editável do guia de PRs](docs/guia-usuario-prs.md)

As capturas que sustentam os guias ficam em `docs/capturas/`. Os arquivos Markdown são as fontes editáveis usadas para atualizar os documentos Word.

### Ordem dos testes

As ferramentas serão estudadas uma por vez, com autorização da mantenedora para cada habilitação. Nenhum PR deve ser aprovado ou mesclado pelo assistente; a aprovação é da mantenedora.

1. Entender o workflow: eventos, jobs, steps, logs e checks no GitHub Actions.
2. Testar Dependabot e observar alertas e PRs de atualização.
3. Testar Code Scanning com CodeQL e observar alertas e localização no código.
4. Testar Secret Scanning e, em uma etapa separada, avaliar Push Protection.

O CircleCI não faz parte do workflow atual e só será explorado quando solicitado.

## API

- `GET /api/products` — lista itens
- `GET /api/products/{id}` — consulta um item
- `POST /api/products` — cadastra item
- `PUT /api/products/{id}` — atualiza item
- `DELETE /api/products/{id}` — remove item

Exemplo de corpo JSON: `{"name":"Violão","category":"Cordas","price":799.90,"stock":8}`.
