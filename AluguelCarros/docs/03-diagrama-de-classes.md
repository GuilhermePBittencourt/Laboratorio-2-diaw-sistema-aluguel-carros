# Diagrama de Classes (domínio)

Reflete exatamente as entidades JPA do pacote `model` do back-end.

```mermaid
classDiagram
    direction LR

    class Usuario {
        -Long id
        -String login
        -String senhaHash
        -Perfil perfil
    }
    class Cliente {
        -Long id
        -String rg
        -String cpf
        -String nome
        -String endereco
        -String profissao
        +atualizarDados(...)
    }
    class Rendimento {
        -String entidadeEmpregadora
        -BigDecimal valor
    }
    class Agente {
        -Long id
        -String nome
        -TipoAgente tipo
    }
    class Automovel {
        -Long id
        -String matricula
        -int ano
        -String marca
        -String modelo
        -String placa
        -TipoProprietario proprietario
        +registrarProprietario(TipoProprietario)
    }
    class PedidoAluguel {
        -Long id
        -LocalDate dataInicio
        -LocalDate dataFim
        -StatusPedido status
        -String parecer
        -Instant criadoEm
        -Instant atualizadoEm
        +alterar(Automovel, LocalDate, LocalDate)
        +cancelar()
        +avaliar(Agente, Decisao, String)
        +marcarComoContratado()
    }
    class Contrato {
        -Long id
        -TipoProprietario proprietario
        -BigDecimal valorCredito
        -Instant dataContrato
    }

    class Perfil {
        <<enumeration>>
        CLIENTE
        AGENTE
    }
    class TipoAgente {
        <<enumeration>>
        EMPRESA
        BANCO
    }
    class TipoProprietario {
        <<enumeration>>
        CLIENTE
        EMPRESA
        BANCO
    }
    class StatusPedido {
        <<enumeration>>
        EM_ANALISE
        APROVADO
        REPROVADO
        CANCELADO
        CONTRATADO
    }
    class Decisao {
        <<enumeration>>
        APROVAR
        REPROVAR
    }

    Usuario "1" -- "0..1" Cliente : é
    Usuario "1" -- "0..1" Agente : é
    Cliente "1" *-- "0..3" Rendimento : possui
    Cliente "1" -- "0..*" PedidoAluguel : solicita
    Automovel "1" -- "0..*" PedidoAluguel : é alugado em
    Agente "0..1" -- "0..*" PedidoAluguel : avalia
    PedidoAluguel "1" -- "0..1" Contrato : gera
    Agente "1" -- "0..*" Contrato : executa
    Agente "0..1" -- "0..*" Contrato : concede crédito (banco)

    Usuario ..> Perfil
    Agente ..> TipoAgente
    Automovel ..> TipoProprietario
    Contrato ..> TipoProprietario
    PedidoAluguel ..> StatusPedido
    PedidoAluguel ..> Decisao
```

## Máquina de estados do pedido

```mermaid
stateDiagram-v2
    [*] --> EM_ANALISE : cliente introduz
    EM_ANALISE --> APROVADO : agente aprova
    EM_ANALISE --> REPROVADO : agente reprova (com parecer)
    EM_ANALISE --> CANCELADO : cliente cancela
    APROVADO --> CONTRATADO : agente executa o contrato
    APROVADO --> CANCELADO : cliente cancela
    REPROVADO --> [*]
    CANCELADO --> [*]
    CONTRATADO --> [*]
```

## Decisões de modelagem

- **Usuario** guarda só as credenciais; **Cliente** e **Agente** são os papéis de negócio ligados a ele (1 para 0..1).
- Os **rendimentos** são um objeto de valor (`@Embeddable`) dentro do cliente, limitado a 3.
- Não existe uma classe separada para "empresa" e "banco": ambos são **Agente**, distinguidos por `TipoAgente`.
- O proprietário do automóvel (`TipoProprietario`) muda quando o **Contrato** é executado, pois, segundo o enunciado, depende do tipo de contrato.
- O crédito é opcional: `bancoCredito` e `valorCredito` do contrato são nulos ou preenchidos juntos.
