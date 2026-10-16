# Lume Reserve API

API REST do projeto demonstrativo Lume Reserve, construída com Spring Boot e PostgreSQL.

## Requisitos

- Java 25 para execução local.
- Docker Desktop para execução conteinerizada.
- PostgreSQL disponível fora do Docker. Em produção, o banco pode ser um projeto
Postgres do Supabase.

## Execução local

Copie o arquivo de exemplo e preencha os valores localmente:

```powershell
Copy-Item .env.example .env
.\mvnw.cmd spring-boot:run
```

O arquivo `.env` é ignorado pelo Git e nunca deve ser commitado.

## Execução com Docker

O container inclui somente a API. O banco de dados permanece externo, conforme a arquitetura do projeto:

```powershell
docker compose build
docker compose up -d
```

A API ficará disponível em `http://localhost:8080`.

Para acompanhar os logs:

```powershell
docker compose logs -f api
```

Para parar:

```powershell
docker compose down
```

## Banco fora do Docker e migrations

O valor de `DB_URL` é carregado pelo `.env` em runtime. Exemplos:

```env
# PostgreSQL em outro servidor
DB_URL=jdbc:postgresql://postgres.example.internal:5432/lume_reserve

# PostgreSQL instalado na máquina host usando Docker Desktop
DB_URL=jdbc:postgresql://host.docker.internal:5432/lume_reserve
```

Não use `localhost` no `DB_URL` quando a API estiver em container: nesse contexto, `localhost` aponta para o próprio container.

O schema é gerenciado pelo Flyway:

- `V1__create_schema.sql` cria as tabelas, relacionamentos, constraints e índices.
- `V2__seed_catalog.sql` insere categorias, métodos de pagamento, fornecedor e produtos de demonstração.
- `V3__seed_additional_catalog.sql` corrige a imagem da IPA e adiciona mais 20 produtos fictícios.
- O Hibernate usa `ddl-auto=validate` por padrão e não altera o banco.

Em um banco vazio, as migrations são executadas automaticamente na inicialização. Para
uma instalação existente que já contenha tabelas criadas manualmente, faça backup e
avalie a migração antes de iniciar com `FLYWAY_ENABLED=true`; o Flyway não deve ser
forçado a assumir um schema que não corresponde às migrations.

## Segurança

- O `.env` nunca é copiado para a imagem.
- O `.env` nunca é enviado ao repositório.
- Segredos são injetados somente em runtime pelo `env_file`.
- A imagem roda com usuário não-root.
- A chave `JWT_SECRET_KEY` é obrigatória e deve ter pelo menos 32 bytes.
- O Flyway é ativado por padrão e valida o schema pelo histórico de migrations.
- O token privado do Mercado Pago deve ficar somente no backend.
- O `.dockerignore` exclui `.env`, histórico Git, IDE e artefatos locais.

Antes de iniciar:

```env
JWT_SECRET_KEY=gere-uma-chave-aleatoria-com-pelo-menos-32-caracteres
MERCADOPAGO_ACCESS_TOKEN=seu-token-privado
DB_USERNAME=usuario
DB_PASSWORD=senha
```

## Validação

```powershell
.\mvnw.cmd test
docker compose config
docker compose build
```

`docker compose config` valida a estrutura do Compose, mas precisa encontrar um `.env` local para interpolar o `env_file`. Nunca substitua o `.env` real por valores de produção em comandos ou logs.

## Decisões e trade-offs

### Multi-stage build

A primeira etapa usa Maven para compilar. A imagem final contém somente o JRE e o JAR, reduzindo o tamanho e a superfície de ataque.

### Banco externo

O Compose não cria PostgreSQL nem volume de dados. Isso mantém os dados fora do ciclo de vida da aplicação e evita que um `docker compose down` altere o banco.

### Configuração em runtime

As configurações sensíveis são lidas pelo Spring a partir de variáveis de ambiente. O custo é exigir configuração antes do primeiro start, mas evita secrets dentro da imagem e do repositório.

### Healthcheck

O serviço expõe o actuator, mas a imagem final usa apenas Java para um healthcheck mínimo. O endpoint HTTP pode ser conectado a um utilitário de healthcheck dedicado em uma evolução futura sem adicionar ferramentas ao runtime.
