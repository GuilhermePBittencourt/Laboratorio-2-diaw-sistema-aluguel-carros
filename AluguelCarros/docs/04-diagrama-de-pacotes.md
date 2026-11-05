# Diagrama de Pacotes (Visão Lógica) e Arquitetura MVC

## Back-end (Spring Boot)

```mermaid
flowchart TB
    subgraph backend["com.example.aluguelcarros"]
        controller["controller<br/><i>Controller (MVC): recebe HTTP, valida e delega</i>"]
        service["service<br/><i>regras de negócio e transações</i>"]
        repository["repository<br/><i>acesso a dados (Spring Data JPA)</i>"]
        model["model<br/><i>Model (MVC): entidades e enums de domínio</i>"]
        dto["dto<br/><i>contratos JSON da API (View do back-end)</i>"]
        security["security<br/><i>JWT e usuário autenticado</i>"]
        config["config<br/><i>Spring Security, CORS, Clock, dados iniciais</i>"]
        exception["exception<br/><i>erros de negócio e tratamento global</i>"]
        validation["validation<br/><i>@Cpf</i>"]
    end

    controller --> service
    controller --> dto
    controller --> security
    service --> repository
    service --> model
    service --> dto
    service --> exception
    service --> security
    repository --> model
    dto --> model
    dto --> validation
    config --> security
    config --> repository
    security --> model
    exception -.-> controller
```

## Front-end (React + Vite)

```mermaid
flowchart TB
    subgraph frontend["frontend/src"]
        pages["pages<br/><i>telas (uma por rota)</i>"]
        components["components<br/><i>peças reutilizáveis</i>"]
        context["context<br/><i>AuthContext: sessão e perfil</i>"]
        services["services<br/><i>api.js: chamadas HTTP com o token</i>"]
    end

    pages --> components
    pages --> context
    pages --> services
    components --> context
    context --> services
```

## Como o MVC se distribui

| Camada MVC | Onde está |
|---|---|
| **Model** | `model` (entidades), `repository` (persistência) e `service` (regras) no back-end; Supabase (PostgreSQL) como armazenamento |
| **View** | React + Vite (`frontend/`), que renderiza as telas a partir do JSON da API |
| **Controller** | `controller` no back-end (REST), que traduz requisições HTTP em chamadas aos serviços |

## Os dois subsistemas do enunciado

1. **Gestão de pedidos e contratos**: `PedidoController`, `PedidoService`, `Contrato`, regras de status.
2. **Construção dinâmica das páginas web**: o front-end React monta as páginas dinamicamente a partir da API.

O servidor central (Spring Boot) se liga aos computadores locais dos clientes e aos agentes pela Internet
(HTTP/JSON), com CORS restrito às origens configuradas.
