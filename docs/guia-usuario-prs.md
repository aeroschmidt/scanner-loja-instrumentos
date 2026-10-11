# Guia de PRs para usuários

**Projeto:** Som & Corda — loja de instrumentos  
**Repositório:** `aeroschmidt/scanner-loja-instrumentos`  
**Atualizado:** 10 de outubro de 2026

Este guia é para quem abre, revisa ou acompanha Pull Requests. Ele mostra o que o GitHub Actions verifica hoje, o que procurar quando Code Scanning estiver ativo e como interpretar a demonstração do PR #9. Configurações administrativas ficam no [guia da mantenedora](guia-maintainer-ghas.md).

**Estado deste exercício:** GitHub Actions está ativo. CodeQL / Code Scanning, Dependabot e Secret Scanning estão desligados; por isso, a demonstração não recebeu análise CodeQL.

## 1. Fluxos de Pull Request

### PR comum com GitHub Actions

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

## 2. Exemplo prático: PR #9

**PR:** [Demonstração controlada de SQL Injection](https://github.com/aeroschmidt/scanner-loja-instrumentos/pull/9)
**Branch:** `demo/vulnerabilidade-sql-sem-code-scanning`
**Commit:** `32da6b1` — mensagem assinada e verificada pelo GitHub.

Este PR foi criado para comparar a compilação do Actions com a análise de segurança do CodeQL. Ele adiciona uma rota isolada de demonstração que monta uma consulta SQL concatenando a entrada `name`. A rota comum de busca continua usando consulta parametrizada. O código vulnerável existe apenas para o exercício e não deve ser mesclado nem usado em produção.

### Resultado observado

- O workflow **Build and test with Maven** concluiu com sucesso no PR. Isso confirma que o projeto compilou e que os testes executados por Maven passaram; não confirma que o código é seguro.
- O CodeQL / Code Scanning permaneceu desligado, conforme solicitado. Por isso, o GitHub não executou a análise CodeQL e não mostrou alerta de vulnerabilidade nesse PR.
- A ausência do alerta é esperada porque o analisador estava desligado. Não é evidência de que o código vulnerável tenha passado por uma análise estática.
- Não há reviewer atribuído: a revisão está pendente da mantenedora. Dependabot não é revisor humano; quando habilitado, pode abrir PRs de dependências, que ainda precisam de revisão.
- A mantenedora decide se aprova e mescla. O assistente não aprova nem mescla PRs.

**Evidências no GitHub:** [conversa e descrição do PR #9](https://github.com/aeroschmidt/scanner-loja-instrumentos/pull/9), [checks do PR #9](https://github.com/aeroschmidt/scanner-loja-instrumentos/pull/9/checks) e [execução do Actions #25](https://github.com/aeroschmidt/scanner-loja-instrumentos/actions/runs/38097282398). A Captura 4 é uma execução anterior do workflow, no PR #7; use os links do PR #9 para ver o resultado desta demonstração.

### Antes e depois: trecho que Code Scanning deve examinar

As duas figuras abaixo são visualizações legíveis dos trechos reais de `ProductRepository.java`, comparando `main` com o código introduzido no PR #9. Elas não são capturas da interface do GitHub. O diff original e as linhas verificáveis estão em [Files changed no PR #9](https://github.com/aeroschmidt/scanner-loja-instrumentos/pull/9/files).

**Antes — `main`, linhas 32–36: busca parametrizada.** O `?` ocupa o lugar do valor; `name` é passado separadamente como parâmetro. Isso evita montar SQL com o conteúdo recebido.

![Visualização do código antes: busca parametrizada na main, ProductRepository.java linhas 32 a 36](capturas/05-antes-busca-parametrizada.png)

**Depois — PR #9, linhas 39–42: SQL Injection demonstrativa.** A entrada externa chega pelo parâmetro `name` em `ProductController.java`, linha 38. `ProductRepository.java`, linha 41, concatena esse valor dentro da instrução SQL; a linha 42 envia a consulta montada para `JdbcTemplate.query`. A rota de demonstração é `/api/products/demo/sql-injection` (linhas 37–40 do controller).

![Visualização do código depois: concatenação SQL no PR 9, ProductRepository.java linhas 39 a 42](capturas/06-depois-concatenacao-sql.png)

**O que esperamos observar quando Code Scanning for ligado:** se o CodeQL reconhecer o fluxo da entrada `name` até a consulta SQL, deverá registrar um alerta de SQL Injection associado ao trecho vulnerável, com arquivo, linha e explicação. O PR atual não confirma essa detecção: CodeQL está desligado. O resultado verde existente é somente do Maven; a compilação não detecta essa falha de segurança.

**“A análise encontrou” e “o PR bloqueou” são resultados diferentes.** Um alerta/anotação ajuda a localizar o problema no diff. Para impedir merge, a administração também precisa exigir o check de Code Scanning nas regras de proteção da branch. Com CodeQL desligado, nem alerta nem check do CodeQL são esperados neste PR. Consulte [alertas de Code Scanning](https://docs.github.com/en/code-security/concepts/code-scanning/code-scanning-alerts), [alertas em pull requests](https://docs.github.com/en/code-security/how-tos/manage-security-alerts/manage-code-scanning-alerts/triage-alerts-in-pull-requests) e [checks obrigatórios](https://docs.github.com/en/pull-requests/reference/status-checks).

**Versão segura de referência:** mantenha a consulta parametrizada, como em `searchByName` nas linhas 32–36. Ao encerrar a demonstração, remova a rota e o método inseguros; não copie esse trecho para uma aplicação real.

### Como ler este PR

1. Na conversa, leia **Objetivo**, **Alteração**, **O que observar** e **Revisão** antes de olhar os checks.
2. Em **Checks**, confirme que o job do Maven terminou com sucesso. Um check verde quer dizer que aquele job passou, dentro do que ele executa.
3. Confira quais checks aparecem no PR. Se a mantenedora não habilitou CodeQL, não espere alerta nem check de Code Scanning.
4. Em **Files changed**, localize `ProductRepository.java` nas linhas 39–42 e compare com a busca segura nas linhas 32–36. A visualização lado a lado acima resume essa alteração.
5. Não aprove nem mescle este PR enquanto a rota vulnerável estiver presente. A mantenedora deve decidir o próximo passo do exercício.
