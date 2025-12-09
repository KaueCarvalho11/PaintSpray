# Paint-Spray - Sistema de Gestão de Oficina

O **Paint-Spray** é um sistema desktop desenvolvido em JavaFX focado na gestão eficiente de oficinas de pintura de motos. O objetivo principal é substituir controles manuais por uma interface visual intuitiva baseada em quadros.

## 📋 Funcionalidades Principais

- **Interface Moderna**: Design limpo e profissional utilizando a biblioteca AtlantaFX (Nord Theme).
- **Dashboard Kanban**: Visualização do fluxo de trabalho (Pendente → Em Andamento → Finalizado).
- **Gestão de Entidades**: CRUD completo de Clientes, Veículos e Ordens de Serviço.
- **Persistência Local**: Banco de dados SQLite embarcado.

## Requisitos

- Java 21 ou superior
- Maven 3.6+

## 🚀 Como Executar

### 1. Pré-requisitos

- Java 21 ou superior
- Maven 3.6+
- SQLite3 (para popular banco manualmente, opcional)

### 2. Executar a aplicação

```bash
mvn javafx:run
```

### 3. Credenciais de Acesso
Como o projeto ainda está em teste, temos apenas credenciais de teste. Clique em Primeiro Acesso, o usuário padrão vai ser criado.

**Usuário padrão:**
- Email: `admin`
- Senha: `admin`

## 📦 Gerar JAR Executável

```bash
mvn clean package
java -jar target/paintspray-1.0-SNAPSHOT.jar
```

## 🏗️ Arquitetura do Sistema

O projeto segue uma arquitetura em camadas baseada no padrão **MVC (Model-View-Controller)** com separação clara de responsabilidades:

### 1. Separação de Responsabilidades
- **View (`/fxml`, `/css`)**: Camada de apresentação responsável pela interface com o usuário.
- **Controller (`/controller`)**: Gerencia a interação entre a View e a lógica de negócios.
- **Service (`/service`)**: Contém as regras de negócio e validações do sistema.
- **Repository (`/repository`)**: Camada de acesso a dados (DAO), responsável pelas queries SQL no SQLite.
- **Model (`/model`)**: Representação dos objetos de domínio (Cliente, Veículo, Serviço).


```
src/main/java/com/paintspray/
├── config/                  # Configuração de Banco de Dados
│   └── DatabaseConnection.java
├── controller/              # Controladores JavaFX (Interação com UI)
│   ├── LoginController.java
│   ├── MainController.java
│   ├── NovaOrdemController.java
│   ├── ServicoController.java
│   ├── ClienteController.java
│   └── SessionManager.java
├── enums/                   # Constantes e Tipos
│   ├── StatusServico.java
│   ├── TipoServico.java
│   └── FormaPagamento.java
├── model/                   # Entidades do Domínio
│   ├── Cliente.java
│   ├── Servico.java
│   ├── Usuario.java
│   └── Veiculo.java
├── repository/              # Acesso a Dados (DAO/SQL)
│   ├── BaseRepository.java
│   ├── ClienteRepository.java
│   ├── ServicoRepository.java
│   ├── UsuarioRepository.java
│   └── VeiculoRepository.java
├── service/                 # Regras de Negócio
│   ├── ClienteService.java
│   ├── ServicoService.java
│   ├── UsuarioService.java
│   └── VeiculoService.java
├── util/                    # Utilitários
│   ├── SceneNavigator.java  # Gerenciador de trocas de tela
│   └── ValidationUtils.java # Validadores de campos
├── MainApplication.java     # Classe Principal (JavaFX)
└── Program.java             # Launcher Alternativo
```

## Banco de Dados

O sistema utiliza SQLite com as seguintes tabelas:

- **usuarios**: Dados do proprietário da oficina
- **clientes**: Cadastro de clientes
- **veiculos**: Veículos dos clientes (motos)
- **servicos**: Ordens de serviço (pintura)

## Status de Serviço (Pipeline)

1. **Pendente** → Serviço cadastrado, aguardando início
2. **Em Andamento** → Serviço sendo executado
3. **Aguardando Pagamento** → Serviço concluído, pendente pagamento
4. **Finalizado** → Serviço pago e concluído

## Tecnologias

- **JavaFX 21**: Framework de interface gráfica
- **SQLite**: Banco de dados embarcado

## 📅 Backlog do Produto

Este backlog foi priorizado com foco nas métricas de sucesso do projeto: reduzir a carga mental do proprietário e garantir o rastreamento de 100% dos veículos.

| Épico | História de Usuário | Prioridade | Status |
| :--- | :--- | :---: | :---: |
| **Autenticação** | **[RF-Seg]** Como proprietário, quero realizar login no sistema para proteger os dados da oficina. | Alta | ✅ Feito |
| **Gestão de Clientes** | **[UC-01]** Como usuário, quero cadastrar, listar e editar dados de clientes (CRUD Completo). | Alta | ✅ Feito |
| **Gestão de Veículos** | **[UC-01/RN-1]** Como usuário, quero cadastrar veículos vinculando-os obrigatoriamente a um cliente dono. | Alta | ✅ Feito |
| **Ordem de Serviço** | **[RF-3]** Como usuário, quero criar uma OS vinculando Veículo e Serviço em menos de 2 minutos. | Alta | ✅ Feito |
| **Fluxo (Kanban)** | **[RF-4]** Como usuário, quero mover a OS entre os status: Pendente, Em Andamento, Aguardando Pagamento e Finalizado. | Alta |  ✅ Feito |
| **Visão Geral** | **[ON-4]** Como dono, quero visualizar quantos carros estão em cada etapa do processo para gerenciar meu tempo. | Alta | ✅ Feito |
| **Persistência** | **[RNF-3]** O sistema deve salvar todos os dados localmente (SQLite) para garantir funcionamento sem internet. | Alta | ✅ Feito |


Desenvolvido para a disciplina de Engenharia de Software - UFERSA 2025.2

## 👥 Componentes
| Integrante | Papel | Resumo das Atividades |
| :--- | :--- | :--- |
| **Kauê Valentim** | Product Owner / Dev | Infraestrutura, diagramas técnicos e migração de dados. |
| **Antonio Erick** | Backend Developer | Modelagem de dados (DER), persistência e refatoração do Core. |
| **Jean Richardson** | Analista de Requisitos / Dev | Definição de requisitos (RF/RNF) e lógica de regras de negócio. |
| **Francisco Adrian** | Frontend / UI Designer | Design de telas, experiência do usuário e construção do Dashboard. |

## 📝 Licença

Este projeto é parte de um trabalho acadêmico.
