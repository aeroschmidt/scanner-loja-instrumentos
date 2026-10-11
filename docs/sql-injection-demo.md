# Detecção e correção de SQL injection

O endpoint `GET /api/products/search?name=...` continua disponível, mas `ProductRepository.searchByName` agora usa uma consulta parametrizada. Assim, a entrada do usuário é tratada como dado e não pode alterar a estrutura da SQL.

O teste `treatsSearchInputAsLiteralText` envia um padrão de SQL injection e confirma que ele não retorna os produtos cadastrados.

