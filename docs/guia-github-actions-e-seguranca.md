# Guia prático: GitHub Actions e segurança do repositório

**Projeto:** Som & Corda — loja de instrumentos  
**Repositório:** `aeroschmidt/scanner-loja-instrumentos`  
**Registro de estado:** 10 de outubro de 2026

Este guia registra o que está ativo, o que ainda está desligado e como cada participante acompanha os testes. O PR #10 continua aberto e contém uma SQL Injection intencional; esta documentação registra o estado observado, não aprova nem integra a alteração.

## 1. Estado atual do projeto

| Recurso | Estado observado | O que significa agora |
|---|---|---|
| GitHub Actions | Ativo | O workflow `Build and test` executa Maven `verify` em `push` para `main` e em PR destinado a `main`. |
| Dependency graph | Off | Ainda não há inventário de dependências apresentado nesta tela do repositório. |
| Dependabot alerts / security updates | Off | Ainda não serão gerados alertas/PRs do Dependabot neste repositório. |
| Dependabot version updates | Não configurado | Não há `.github/dependabot.yml`. |
| CodeQL / Code Scanning | On, Advanced setup | O GitHub criou `.github/workflows/codeql.yml` na `main`; a execução inicial `CodeQL Advanced #1` terminou com sucesso. O PR #10 mantém o teste controlado de SQL Injection. |
| Secret Protection | Off | Settings oferece `Enable`. Em repositório público, padrões de parceiros podem continuar sendo reportados aos provedores, conforme a regra do GitHub. |
| Copilot Autofix | On na tela | Depende de CodeQL habilitado para propor correções de alertas CodeQL; não foi alterado. |

**Captura 1 — Settings > Advanced Security:** Dependency graph desligado.

![Settings: Dependency graph desligado](capturas/01-dependency-graph-off.jpg)

**Captura 2 — estado histórico anterior à ativação:** opções de atualização do Dependabot desligadas e CodeQL aguardando `Set up`. A captura é anterior à configuração atual, que usa Advanced setup.

![Settings: Dependabot desligado e CodeQL sem configuração](capturas/04-dependabot-codeql-config.jpg)

**Captura 3 — Secret Protection:** botão `Enable` disponível, sem habilitação feita.

![Settings: Secret Protection desligado](capturas/02-secret-protection.jpg)

**Captura 4 — Actions:** execução de `Build and test` no PR #7 com resultado `Success`.

![Actions: workflow Maven concluído com sucesso](capturas/03-actions-run-success.jpg)

## 2. Para quem administra o repositório

### A. Entender o GitHub Actions que já existe

1. Abra o repositório e selecione **Actions**. Consulte a execução associada ao PR que estiver avaliando.
2. Confira o evento que iniciou a execução (`pull_request`), o job e os steps.
3. O arquivo `build.yml` prepara Java 21 e executa `./mvnw ... verify` para compilar e testar.
4. Inspecione o log do job para conferir o que foi executado e localizar a primeira mensagem de falha, se houver.

O Actions coordena o workflow e o Maven compila e testa. Uma execução verde confirma apenas os passos configurados nesse workflow; a análise CodeQL é uma verificação separada.

Cada novo push ou atualização de PR pode iniciar outra execução. O GitHub permite execuções simultâneas por padrão. Quando a capacidade de runners hospedados é atingida, os jobs novos aguardam na fila; eles não falham apenas porque várias pessoas enviaram commits. Se a equipe configurar `concurrency` com cancelamento, execuções anteriores da mesma fila podem ser canceladas intencionalmente. Ao investigar um resultado, confira sempre o SHA do commit associado ao check para não confundir a execução de um commit antigo com a mais recente.

### B. Dependabot — testar primeiro

Dependabot cobre dois comportamentos distintos:

- **Dependabot alerts:** avisa que uma dependência usada tem vulnerabilidade conhecida.
- **Security updates:** pode abrir PR para atualizar dependência vulnerável quando há correção compatível.
- **Version updates:** procura novas versões mesmo sem alerta de vulnerabilidade; depende de `.github/dependabot.yml`.

Fluxo de administração, quando a proprietária decidir iniciar este teste:

1. Abra **Settings > Advanced Security**.
2. Ative **Dependency graph** e, separadamente, **Dependabot alerts**. Registre quais alertas aparecem.
3. Em uma etapa posterior, ative **Dependabot security updates** e observe se ele cria um PR de correção. Alertas e atualizações são opções diferentes.
4. Teste **version updates** somente quando decidir adicionar `.github/dependabot.yml`; selecione ecossistema Maven, diretório `/` e agenda apropriada.
5. Compare o PR automático com o PR comum: versão alterada, motivo, checks, logs e resultado de merge. O administrador não deve habilitar as etapas seguintes sem decidir que esta observação terminou.

### C. Code Scanning com CodeQL — Advanced setup

1. Em **Settings > Advanced Security > Code Security > CodeQL analysis**, a mantenedora desativou Default setup e selecionou Advanced setup.
2. O GitHub criou `.github/workflows/codeql.yml` na branch `main` (commit `0087afb`). A lista de Actions passou a exibir **CodeQL Advanced**; a execução inicial `#1` terminou com sucesso em 1 min 44 s: [ver execução](https://github.com/aeroschmidt/scanner-loja-instrumentos/actions/runs/38104871050).
3. O workflow usa `push` para `main`, `pull_request` destinado a `main` e uma agenda semanal (`cron: '20 3 * * 2'`, terça-feira às 03:20 UTC, 00:20 no horário de Brasília).
4. O matrix analisa `actions`, `java-kotlin` e `javascript-typescript`, detectadas para este repositório. O modo `none` para Java cria a base sem executar o Maven e é suportado; `autobuild` ou `manual` podem ser avaliados se for necessário analisar código gerado ou restringir a análise ao que o build compila.
5. O workflow concede `security-events: write` para publicar resultados e permissões de leitura para obter o código e actions. Não há token Sonar nem integração com Sonar neste workflow.
6. Para verificar o resultado, abra **Actions > CodeQL Advanced**, selecione uma execução e confira as análises por linguagem. No PR, consulte **Checks** e **Security**; o check pode terminar verde mesmo quando há alerta. Para bloquear merge, configure uma regra de proteção que exija o check apropriado.

O workflow é configuração versionada: gatilhos, linguagens, permissões, versões de actions e estratégia de build podem ser revistos no diff do PR. A execução inicial na `main` comprova que o workflow avançado executou; a execução seguinte em PR permite observar os resultados da branch desse PR.

### D. Secret Scanning — etapa separada

1. Abra **Settings > Advanced Security > Secret Protection > Enable**.
2. Leia a confirmação apresentada pelo GitHub e confira quais recursos estão incluídos para este repositório e plano.
3. Depois de ativar Secret Protection, examine separadamente **Push protection** e só a ative quando essa for a etapa autorizada.
4. Registre a diferença: Secret Scanning procura padrões de credenciais já presentes; Push protection pode bloquear um push que contenha um padrão reconhecido.
5. Não use credenciais reais em testes. Se for necessário validar detecção, use o procedimento e os valores de teste indicados pela documentação oficial do GitHub.

Regras e disponibilidade de funcionalidades dependem de visibilidade e plano do repositório. Em repositórios públicos, o GitHub informa que envia alertas de padrões de parceiros aos provedores correspondentes; isso não deve ser confundido com a configuração dos alertas privados para a proprietária.

## 3. Para quem cria ou revisa PRs

### PR comum com Actions

1. Crie uma branch e faça commits assinados conforme a configuração do repositório.
2. Abra um PR para `main` e explique a mudança.
3. Em **Checks**, aguarde **Build and test / Build and test with Maven**.
4. Se falhar, abra o job, expanda o step vermelho e use o log para localizar o erro de compilação/teste.
5. Aguarde a revisão e aprovação da proprietária. O assistente não aprova nem faz merge de PRs.

### PR quando Code Scanning estiver habilitado

1. Consulte o check de Code Scanning no PR junto com o build Maven.
2. Abra cada alerta para ver a regra, severidade, arquivo e linha indicados.
3. Corrija no código e envie um novo commit; confirme se a nova análise atualizou ou fechou o alerta.
4. Trate os checks como sinais para revisão. O bloqueio de merge depende da regra de branch configurada pela administração.

### PR automático do Dependabot

1. Confira no diff quais dependências e versões foram alteradas.
2. Leia a descrição do alerta/atualização e examine os checks do PR.
3. Revise compatibilidade e testes como em qualquer PR. Um PR do bot não deve ser aprovado automaticamente neste exercício.
4. Somente a proprietária aprova e faz merge.

### Push protection

Se um push for bloqueado por uma possível credencial, não contorne o bloqueio com um segredo real. Remova o valor do código/histórico conforme o caso, use um armazenamento seguro para credenciais e siga o procedimento da organização.

### Dispensa de alertas de Code Scanning

Não é qualquer pessoa que fez um commit: no padrão atual, quem tem **permissão de escrita no repositório** pode ver e usar **Dismiss alert**, independentemente de o alerta ser High ou Critical. A severidade informa a gravidade; ela não concede nem remove permissões. No alerta High observado no PR #10, o botão abriu um formulário que exige uma razão. As opções mostradas foram `False positive`, `Used in tests`, `Won't fix` e `Mitigated`. Nenhuma foi selecionada e o alerta continua aberto.

Boa prática: primeiro entenda a regra e o caminho indicado, confirme se o achado é real e corrija a causa no código; rode testes e aguarde a nova análise. Não dispense um alerta real só para liberar o PR ou deixar o check verde. Use **Dismiss alert** apenas quando houver uma justificativa válida (por exemplo, falso positivo ou uso deliberado em teste), registre o contexto e siga a política de revisão da equipe. Dispensar fecha o alerta no painel, registra a razão e não corrige o código.

Se a política exigir controle adicional, a mantenedora pode configurar **delegated alert dismissal**: pessoas com acesso de escrita solicitam a dispensa, e proprietários da organização ou security managers podem aprovar ou rejeitar. A opção delegada não foi ativada neste exercício.

![Alerta High aberto no PR #10, com a localização em ProductRepository.java](capturas/pr10-alert-detail-final.jpg)

![Formulário de dispensa com razões disponíveis; nenhuma foi enviada](capturas/pr10-dismiss-reasons.jpg)

## 4. Sequência e registro de aprendizado

1. Aprender o workflow de Actions já ativo.
2. Testar Dependabot isoladamente.
3. Testar Code Scanning/CodeQL isoladamente.
4. Testar Secret Scanning e, se autorizado, Push protection.
5. Registrar estado inicial, configuração alterada e evidências de cada etapa, sem misturar os resultados do build com os alertas de segurança.

Cada ativação é uma decisão separada da proprietária. Registre para cada sessão: data, configuração alterada, estado anterior/novo, URL da execução ou PR, resultado observado e captura da tela.

## 5. Documentação oficial

- [Configurar Dependabot alerts](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/secure-your-dependencies/configure-dependabot-alerts)
- [Configurar atualizações de versão do Dependabot](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/secure-your-dependencies/configure-version-updates)
- [Configurar Code Scanning advanced setup](https://docs.github.com/en/code-security/how-tos/find-and-fix-code-vulnerabilities/configure-code-scanning/configuring-advanced-setup-for-code-scanning)
- [CodeQL para linguagens compiladas e modos de build](https://docs.github.com/en/code-security/how-tos/find-and-fix-code-vulnerabilities/manage-your-configuration/codeql-for-compiled-languages)
- [Tipos de configuração do Code Scanning](https://docs.github.com/en/code-security/concepts/code-scanning/setup-types)
- [Resolver alertas de Code Scanning e dispensá-los](https://docs.github.com/en/code-security/how-tos/manage-security-alerts/manage-code-scanning-alerts/resolve-alerts)
- [Aprovação delegada de dispensas](https://docs.github.com/en/code-security/concepts/security-at-scale/delegated-alert-dismissal)
- [Quem pode dispensar alertas de Code Scanning](https://docs.github.com/en/code-security/how-tos/manage-security-alerts/manage-code-scanning-alerts/triage-alerts-in-pull-requests)
- [Concorrência de workflows e jobs](https://docs.github.com/en/actions/concepts/workflows-and-actions/concurrency)
- [Limites do GitHub Actions](https://docs.github.com/en/actions/reference/limits)
- [Habilitar Secret Scanning](https://docs.github.com/en/code-security/how-tos/secure-your-secrets/detect-secret-leaks/enable-secret-scanning)
- [Habilitar Push protection](https://docs.github.com/en/code-security/how-tos/secure-your-secrets/prevent-future-leaks/enable-push-protection)
- [Quickstart de segurança do GitHub](https://docs.github.com/en/code-security/getting-started/quickstart-for-securing-your-repository)

## 6. Histórico desta trilha

- O workflow `build.yml` executa Maven `verify`; o workflow `codeql.yml` executa CodeQL em um job separado.
- O workflow da branch do PR #10 usa `push` para `main` e `pull_request` destinado a `main`.
- Após a atualização deste guia, a execução [#21 do Actions](https://github.com/aeroschmidt/scanner-loja-instrumentos/actions/runs/38094010130) passou em 27 segundos no evento `pull_request`.
- Code Scanning/CodeQL está ativo via Advanced setup; Dependabot e Secret Protection continuam desligados conforme o escopo.
- Execução inicial do workflow avançado: [CodeQL Advanced #1](https://github.com/aeroschmidt/scanner-loja-instrumentos/actions/runs/38104871050), concluída com sucesso em 10/10/2026.
- A proprietária revisa e aprova PRs. O assistente não aprova nem mescla.
