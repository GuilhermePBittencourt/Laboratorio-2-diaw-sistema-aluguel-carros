# Histórias de Usuário

Atores: **Cliente** (pessoa que aluga) e **Agente** (empresa ou banco que avalia pedidos e executa contratos).

Legenda de status: ✅ implementado no protótipo · 🔜 previsto no modelo, fora do escopo desta entrega.

| ID | História | Status |
|----|----------|--------|
| HU01 | Como **visitante**, quero me cadastrar informando meus dados e uma senha, para poder usar o sistema. | ✅ |
| HU02 | Como **cliente ou agente**, quero entrar com usuário e senha, para acessar apenas o que me pertence. | ✅ |
| HU03 | Como **cliente**, quero consultar e modificar meus dados (RG, CPF, nome, endereço, profissão e rendimentos), para mantê-los atualizados. | ✅ |
| HU04 | Como **cliente**, quero excluir meu cadastro, para sair do sistema. | ✅ |
| HU05 | Como **cliente**, quero introduzir um pedido de aluguel escolhendo um automóvel e o período, para alugá-lo. | ✅ |
| HU06 | Como **cliente**, quero consultar meus pedidos e o status de cada um, para acompanhar a análise. | ✅ |
| HU07 | Como **cliente**, quero modificar um pedido que ainda está em análise, para corrigir datas ou o automóvel. | ✅ |
| HU08 | Como **cliente**, quero cancelar um pedido, para desistir do aluguel. | ✅ |
| HU09 | Como **agente**, quero ver todos os pedidos (com filtro por status), para saber o que precisa de análise. | ✅ |
| HU10 | Como **agente**, quero aprovar ou reprovar um pedido com um parecer financeiro, para decidir se ele segue para contrato. | ✅ |
| HU11 | Como **agente**, quero executar o contrato de um pedido aprovado, definindo em nome de quem o automóvel fica registrado e, se houver, o crédito concedido por um banco. | ✅ |
| HU12 | Como **agente**, quero cadastrar automóveis (matrícula, ano, marca, modelo e placa), para disponibilizá-los para aluguel. | ✅ |
| HU13 | Como **agente**, quero consultar, modificar e excluir clientes, para dar suporte a eles. | ✅ |
| HU14 | Como **agente**, quero modificar um pedido em análise, para ajustá-lo a pedido do cliente. | ✅ |
| HU15 | Como **cliente**, quero ser avisado por e-mail quando meu pedido mudar de status. | 🔜 |

## Critérios de aceitação (principais)

**HU01 – Cadastro**
- CPF válido (dígitos verificadores) e único; RG, nome, endereço e profissão obrigatórios.
- No máximo **3 rendimentos** (entidade empregadora e valor).
- Usuário único; senha com 8 a 72 caracteres, guardada com BCrypt.
- Após o cadastro, o cliente já entra autenticado.

**HU02 – Login**
- Credenciais erradas retornam a mesma mensagem, sem revelar se o usuário existe.
- Todas as rotas, exceto login e cadastro, exigem token JWT.
- Cada perfil só acessa as próprias operações (agente não cria pedido; cliente não avalia).

**HU05 – Introduzir pedido**
- A data de início não pode estar no passado e o término deve ser posterior ao início.
- Um automóvel não pode ter dois pedidos ativos (em análise, aprovados ou contratados) em períodos que se sobrepõem.
- O pedido nasce com status `EM_ANALISE`.

**HU06 – Status do pedido**
- O cliente vê só os próprios pedidos; um pedido de outro cliente aparece como "não encontrado".
- Status possíveis: `EM_ANALISE`, `APROVADO`, `REPROVADO`, `CANCELADO`, `CONTRATADO`.

**HU07/HU08 – Modificar e cancelar**
- Só é possível modificar enquanto `EM_ANALISE`.
- Só é possível cancelar `EM_ANALISE` ou `APROVADO`.

**HU10 – Avaliar**
- Só pedidos `EM_ANALISE` podem ser avaliados; reprovar exige parecer.

**HU11 – Executar contrato**
- Só pedidos `APROVADO` viram contrato.
- Crédito é opcional; se informado, banco e valor são obrigatórios e o crédito só pode vir de um agente do tipo banco.

**HU04/HU13 – Exclusão de cliente**
- Não é possível excluir cliente que tenha pedido ativo; pedidos já encerrados são removidos junto.
