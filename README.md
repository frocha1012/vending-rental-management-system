# vending-rental-management-system

University project (Projeto II) — Vending Machine Rental Management System.

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Persistence | Spring Data JPA + PostgreSQL |
| Web UI (FrontOffice) | Thymeleaf + Bootstrap 5 |
| Desktop UI (BackOffice) | JavaFX |
| Build | Maven |

## Architecture

```
src/main/java/pt/ipvc/vending/
├── config/          # DataSeeder, DatabaseMigration
├── domain/
│   ├── entity/      # Cliente, VendingMachine, Contrato, Proposta, Instalacao, PedidoRescisaoContrato
│   └── enums/       # EstadoCliente, EstadoContrato, EstadoProposta, EstadoInstalacao, ...
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
├── web/
│   ├── controller/  # Web controllers (portal, login, CRUD)
│   └── interceptor/ # PortalInterceptor (session auth)
└── javafx/          # BackOffice desktop app
```

## Running

### Prerequisites
- Java 21
- Maven
- PostgreSQL running locally (`vending_rental` database, user `postgres`, password `pwd`)

### Web FrontOffice (client portal)
```bash
mvn spring-boot:run
```
Access at `http://localhost:8080`

Default client credentials (seeded):
- Username: `escola` / Password: `1234`
- Username: `cafe` / Password: `1234`

### Desktop BackOffice (JavaFX)
```bash
mvn javafx:run
```

## BackOffice Roles

| Role | Access |
|---|---|
| Administrador | Full access to all modules |
| Gestor | Proposals, Contracts, Installations, Rescission Requests |
| Rececionista | Client CRUD + read-only view of Proposals & Contracts |
| Técnico | Installation workflow (mark as Concluded or Postponed) |

## Key Workflows

### Proposal negotiation
1. Client submits proposal via portal
2. Manager reviews and sets price in BackOffice
3. Manager sends to client
4. Client accepts, rejects, or counter-proposes
5. On acceptance, Contract + Installation created automatically

### Contract rescission
1. Client requests rescission from portal
2. Manager approves or rejects in BackOffice
3. On approval, Contract → TERMINADO, VendingMachine → DISPONIVEL

### Installation workflow (Técnico)
1. Técnico sees all installations
2. For AGENDADA: mark as CONCLUIDA or ADIADA
3. ADIADA requires new date + delay reason
