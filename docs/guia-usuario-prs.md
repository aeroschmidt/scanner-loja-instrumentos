# Guia de PRs: Actions e Code Scanning

**Público:** pessoas que criam, revisam ou atualizam pull requests  
**Objetivo:** interpretar os checks de compilação e de segurança antes da revisão.

## O que acontece quando um PR é aberto

O workflow de build compila o projeto e executa seus testes. O workflow CodeQL faz análise estática do código e publica os resultados no pull request e em **Security > Code scanning**. Em Advanced setup, o workflow é definido em YAML e pode ser ajustado para os eventos, linguagens e modos de build necessários.

O material do GitHub abaixo compara a configuração gerenciada pelo GitHub (Default setup) com Advanced setup, que cria um workflow editável. É uma explicação geral do produto, não uma captura do código do PR.

![Material do GitHub Docs comparando Default setup e Advanced setup](capturas/05-github-docs-default-advanced.png)

O agendamento diário executa uma nova análise da branch padrão. Ele não cria commits, não sincroniza branches e não mescla pull requests.

## Como interpretar os resultados

| Resultado | O que informa | Próximo passo |
|---|---|---|
| Build/teste aprovado | Compilação e testes configurados terminaram com sucesso. | Confira também a análise de segurança e a revisão. |
| Job de análise CodeQL aprovado | O scanner foi executado para aquela linguagem. | Abra o resultado agregado de Code Scanning; um job concluído pode ter encontrado alertas. |
| Alerta em Code Scanning | Uma regra identificou um fluxo ou padrão que merece investigação. | Leia a regra, severidade, arquivo, linha, origem e destino do fluxo. |
| Check agregado vermelho | Há um resultado que falhou a condição do check, como um novo alerta. | Corrija e envie um commit atualizado; confirme que a nova análise refletiu a correção. |

Um check vermelho não bloqueia merge por si só. A administração precisa tornar esse check obrigatório em regras de proteção da branch. Mesmo quando o GitHub permite mesclar, a pessoa autora deve corrigir o problema ou seguir o processo de exceção da organização.

## O que fazer quando o alerta aponta uma vulnerabilidade

1. Abra o alerta a partir do PR ou de **Security > Code scanning**.
2. Leia a regra e a explicação do fluxo. Confirme qual dado é controlado por entrada externa e onde ele chega a uma operação sensível.
3. Corrija a causa no código e acrescente ou ajuste testes quando fizer sentido.
4. Envie um commit para a mesma branch do PR. Os checks de build e CodeQL serão executados novamente.
5. Confirme que o alerta foi atualizado ou fechado e peça revisão.

Não descarte um alerta apenas para deixar o check verde. Use dismiss somente quando a análise confirmar que é falso positivo ou que existe uma justificativa aceita pela política da organização.

### Exemplo didático de resultado

No PR usado para o exercício, os jobs que executaram a análise CodeQL terminaram com sucesso, mas o resultado agregado encontrou uma vulnerabilidade High de SQL Injection e apontou o trecho de código. Isso mostra a diferença entre “o scanner conseguiu rodar” e “o scanner não encontrou problemas”. O alerta pode deixar o check de Code Scanning vermelho mesmo quando o build e os jobs de análise terminaram.

Evidências para consulta: [execução do workflow](https://github.com/aeroschmidt/scanner-loja-instrumentos/actions/runs/38105323924) e [resultado de Code Scanning no pull request](https://github.com/aeroschmidt/scanner-loja-instrumentos/pull/10/checks?check_run_id=114369610380). O PR continua sujeito à revisão da mantenedora; o check só bloqueia merge se for obrigatório nas regras da branch.

## Revisão e merge

A pessoa autora responde aos comentários e checks. A mantenedora revisa e aprova o pull request conforme as regras do repositório. Não presuma que um check verde equivale a aprovação, nem que um check vermelho sempre impede merge: são controles separados.

## Referências

- [Tipos de configuração do Code Scanning](https://docs.github.com/en/code-security/concepts/code-scanning/setup-types)
- [Configuração avançada do Code Scanning](https://docs.github.com/en/code-security/how-tos/find-and-fix-code-vulnerabilities/configure-code-scanning/configuring-advanced-setup-for-code-scanning)
- [Triagem de alertas em pull requests](https://docs.github.com/en/code-security/how-tos/manage-security-alerts/manage-code-scanning-alerts/triage-alerts-in-pull-requests)
