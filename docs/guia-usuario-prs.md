# Guia do usuário para Pull Requests e verificações de segurança

Este guia explica como autores e revisores de código acompanham um Pull Request (PR) no GitHub quando há verificações automáticas. Ele ajuda a interpretar resultados do GitHub Actions e do GitHub Advanced Security (GHAS), responder a alertas e decidir o que precisa de revisão humana. O administrador do repositório configura as ferramentas; as instruções de configuração ficam no [guia da mantenedora](guia-maintainer-ghas.md).

Um check verde significa que aquela verificação terminou com sucesso. Isso não prova, por si só, que o código está livre de defeitos ou seguro. Alertas de segurança e bloqueios de merge também são coisas diferentes: o alerta aponta um achado; a regra de proteção da branch define se o merge fica impedido até a resolução.

## 1. Quem faz o quê

- **Autor do PR:** descreve a mudança, acompanha os checks, corrige falhas e responde às observações da revisão.
- **Revisor:** entende o objetivo e o diff, verifica os resultados automáticos e pede ajustes quando necessário.
- **Administrador do repositório:** habilita e configura workflows, ferramentas de segurança e regras de proteção. Consulte o guia da mantenedora para essas tarefas.
- **Bots:** podem executar análises ou propor atualizações. Um resultado automático não substitui a revisão e aprovação previstas pelo processo da equipe.

## 2. Fluxo normal de um Pull Request

1. Crie uma branch para a mudança e mantenha cada PR focado em um objetivo.
2. Abra o PR para a branch de destino indicada pela equipe. Explique o motivo, o que mudou, como foi verificado e quais riscos merecem atenção.
3. Aguarde os checks na área **Checks** ou na seção de status do PR. Enquanto estiverem em execução, espere a conclusão antes de interpretar o resultado.
4. Se um check falhar, abra o detalhe do job, localize a etapa marcada como falha e leia o log a partir da primeira mensagem de erro relevante. Corrija a causa e envie uma nova alteração para o mesmo PR.
5. Revise o diff em **Files changed** e responda aos comentários. A aprovação e o merge seguem as regras da equipe.

### Como ler o estado de um check

- **Em execução:** o workflow ainda está trabalhando. Aguarde o resultado.
- **Sucesso:** as etapas daquele workflow passaram. Confira o que o workflow realmente executou; um build não substitui análise de segurança.
- **Falha:** uma etapa não passou ou encontrou uma condição que encerra o workflow. Abra o log para saber qual.
- **Não aparece:** a verificação pode não estar configurada para esse evento, branch ou linguagem, pode estar desligada ou não ser obrigatória. A ausência não significa que a análise passou.
- **Pendente ou ignorado:** verifique a explicação no GitHub e siga o processo da equipe. Não presuma que isso equivale a uma aprovação.

Checks podem vir de GitHub Actions, Code Scanning ou outros serviços. O nome e o propósito aparecem no próprio check.

## 3. O que cada ferramenta indica para quem envia ou revisa PRs

### GitHub Actions

Actions executa os workflows definidos para eventos como abrir ou atualizar um PR. Um workflow pode compilar o projeto, rodar testes, verificar estilo ou executar outras tarefas. Consulte o resultado e os logs do job que corresponde à falha. Se o workflow está verde, conclua apenas que as tarefas configuradas nele passaram naquela execução.

### Code Scanning

Quando uma análise de código está configurada para o PR, ela pode exibir alertas e anotações com a regra, severidade, arquivo e trecho relacionado. Abra o alerta para entender o caminho do dado, o risco e a correção sugerida; depois confira o contexto completo no diff.

Um alerta não significa automaticamente que o merge será bloqueado. Isso depende das regras de proteção e da configuração do check. Da mesma forma, nenhum alerta pode significar que a análise não encontrou problemas que reconhece, que não foi executada ou que não cobre aquele caso. Considere a linguagem, o tipo de análise e o código efetivamente analisado.

**Ao encontrar um alerta:**

1. Leia a descrição, severidade, arquivo e linha indicados.
2. Entenda se o achado é real no contexto da mudança; não descarte apenas porque o build passou.
3. Corrija a origem do problema e acrescente ou ajuste testes quando fizer sentido.
4. Envie a correção e confira a nova análise. Se considerar o alerta incorreto, siga o processo da equipe para justificar e solicitar triagem; não o oculte sem explicação.

### Dependabot

Dependabot pode abrir PRs de **atualização de segurança** para dependências vulneráveis ou de **atualização de versão** para manter dependências atualizadas. O PR normalmente identifica a dependência e a versão proposta, e pode incluir informações sobre a atualização.

Trate-o como uma alteração de código: confira o diff, o motivo da atualização, a compatibilidade e os checks. Veja notas de versão quando uma atualização puder alterar comportamento. Um PR criado pelo Dependabot não é uma aprovação humana; siga o processo normal de revisão e aprovação.

### Secret Scanning e Push Protection

Secret Scanning procura padrões de credenciais expostas no repositório e pode gerar alertas. Push Protection pode bloquear um envio quando detecta um possível segredo, conforme a configuração aplicada.

**Se o envio for bloqueado:**

1. Não coloque uma credencial real em um PR ou commit de teste.
2. Remova o valor do código e use o mecanismo de armazenamento aprovado pela equipe.
3. Se a credencial for verdadeira ou tiver sido exposta, avise imediatamente o responsável e revogue ou rotacione o segredo. Apagar o texto do commit, sozinho, não invalida uma credencial que já foi exposta.
4. Só use uma opção de bypass se a política da organização permitir e houver justificativa aprovada. O bypass pode registrar um alerta e não deve servir para contornar a correção.

Se o padrão detectado for um falso positivo ou um valor de teste sem validade, siga o procedimento definido pelos administradores para revisar o bloqueio.

## 4. Alerta encontrado versus merge bloqueado

O GitHub pode mostrar um alerta para ajudar a equipe a localizar um problema sem impedir o merge. Para bloquear o merge, o repositório precisa exigir o check relevante ou outra regra aplicável. O usuário deve:

1. verificar se o resultado está concluído e qual ferramenta o produziu;
2. abrir os detalhes do alerta ou do check e entender a ação esperada;
3. corrigir o problema ou encaminhar a triagem conforme a política da equipe;
4. confirmar que a nova execução reflete a correção;
5. aguardar as aprovações exigidas antes do merge.

Não trate “sem check”, “check verde” e “alerta resolvido” como estados equivalentes.

## 5. Exercícios controlados para aprender as ferramentas

Uma equipe pode comparar o resultado de uma mudança segura com o de uma vulnerabilidade intencional para entender o que uma análise detecta. Isso deve ocorrer em uma branch ou repositório de laboratório, com dados fictícios e sob supervisão. Nunca introduza credenciais válidas ou código inseguro em uma branch de produção.

Para cada exercício:

1. registre o estado inicial e quais verificações estão ativas;
2. faça uma única alteração controlada e descreva o comportamento esperado;
3. abra um PR e observe os checks e alertas sem presumir que a ferramenta necessariamente reconhecerá o caso;
4. compare o resultado com o que a análise realmente executou e com o diff;
5. remova o código inseguro, confirme a nova análise e não faça merge enquanto o risco estiver presente.

Uma ferramenta pode não reconhecer um exemplo por limitações de linguagem, configuração, regras selecionadas ou contexto analisado. A finalidade do exercício é aprender o alcance e os limites da verificação, não declarar o código seguro por ausência de alertas.

## 6. Modelo de descrição para PR

Adapte este roteiro ao padrão da equipe:

- **Objetivo:** qual necessidade esta mudança atende?
- **Alterações:** o que foi modificado?
- **Verificação:** quais testes ou checks foram executados?
- **Segurança:** há alertas, dados sensíveis ou riscos que o revisor deve avaliar?
- **Revisão:** há algum ponto específico que precisa de atenção?

Não inclua tokens, senhas, dados pessoais ou segredos nos exemplos, na descrição ou nos comentários do PR.

## 7. Checklist rápido

### Para quem abre o PR

- [ ] O título e a descrição explicam a mudança.
- [ ] O diff contém somente o escopo esperado.
- [ ] Os checks terminaram e as falhas foram investigadas.
- [ ] Alertas de segurança foram tratados ou encaminhados com justificativa.
- [ ] Nenhuma credencial real foi incluída.
- [ ] As revisões e aprovações necessárias foram solicitadas.

### Para quem revisa

- [ ] O diff corresponde ao objetivo informado.
- [ ] Os checks importantes foram executados e seus resultados foram entendidos.
- [ ] Alterações de dependências foram avaliadas quanto a segurança e compatibilidade.
- [ ] Alertas e possíveis segredos foram tratados conforme a política da equipe.
- [ ] A aprovação considera o código e as regras do repositório; status verde isolado não substitui revisão.

## 8. Referências oficiais

- [GitHub Actions: entender workflows](https://docs.github.com/en/actions/about-github-actions/understanding-github-actions)
- [Code Scanning: alertas e resultados em Pull Requests](https://docs.github.com/en/code-security/concepts/code-scanning/code-scanning-alerts)
- [Checks de status e regras de branch](https://docs.github.com/en/pull-requests/reference/status-checks)
- [Revisar PRs do Dependabot](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/manage-your-dependency-security/manage-dependabot-prs)
- [Secret Scanning e prevenção de vazamentos](https://docs.github.com/en/code-security/how-tos/secure-your-secrets/prevent-future-leaks)
