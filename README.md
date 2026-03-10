# spring-batch-multi-layout-processor

Exemplo de projeto Spring Batch demonstrando como processar arquivos de múltiplos layouts usando BeanIO — cada fonte (transportadora) possui seu próprio mapeamento, reader e processor, todos normalizando para uma única entidade de saída.

---

## Visão Geral

No mundo real, integrações logísticas recebem arquivos de diversas transportadoras, cada uma com um formato completamente diferente: Correios envia arquivos delimitados por `|`, Jadlog envia CSV, Total Express envia arquivos posicionais (fixed-length). Este projeto demonstra como unificar esse processamento em um único Job Spring Batch, normalizando todos os registros para a tabela `ocorrencia_entrega`.

```
Correios  (pipe-delimited) ──► CorreiosProcessor   ──┐
Jadlog    (CSV)            ──► JadlogProcessor     ──┼──► OcorrenciaEntregaWriter ──► PostgreSQL
TotalExpress (fixedlength) ──► TotalExpressProcessor ─┘
```

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 25 (`--enable-preview`) |
| Spring Boot | 4.0.3 |
| Spring Batch | 6.0.2 |
| Spring Framework | 7.0.5 |
| BeanIO | 3.2.1 |
| Hibernate ORM | 7.2.4.Final |
| PostgreSQL driver | 42.7.10 |
| Lombok | 1.18.42 |

---

## Estrutura do Projeto

```
src/
└── main/
    ├── java/com/demo/spring/batch/
    │   ├── SpringBatchApplication.java
    │   ├── config/
    │   │   ├── BatchConfig.java                    # Job, Steps e Readers
    │   │   └── BatchProperties.java                # @ConfigurationProperties
    │   ├── controller/
    │   │   └── BatchJobController.java             # POST /batch/iniciar
    │   ├── domain/
    │   │   └── OcorrenciaEntrega.java              # entidade JPA normalizada
    │   ├── listener/
    │   │   └── TransportadoraSkipListener.java
    │   ├── processor/
    │   │   ├── correios/CorreiosOcorrenciaProcessor.java
    │   │   ├── jadlog/JadlogOcorrenciaProcessor.java
    │   │   └── totalexpress/TotalExpressOcorrenciaProcessor.java
    │   ├── reader/
    │   │   ├── TransportadoraItemReader.java        # reader genérico com restart
    │   │   ├── correios/CorreiosOcorrenciaRecord.java
    │   │   ├── jadlog/JadlogOcorrenciaRecord.java
    │   │   └── totalexpress/TotalExpressOcorrenciaRecord.java
    │   ├── repository/
    │   │   └── OcorrenciaEntregaRepository.java
    │   └── writer/
    │       └── OcorrenciaEntregaWriter.java
    └── resources/
        ├── application.yml
        ├── mapping/
        │   └── transportadoras-mapping.xml         # mapeamentos BeanIO
        └── samples/
            ├── correios_ocorrencias_20240115.csv
            ├── jadlog_ocorrencias_20240115.csv
            └── totalexpress_ocorrencias_20240115.txt
```

---

## Pré-requisitos

- Java 25+
- Maven 3.9+
- PostgreSQL rodando em `localhost:5432`

### Criar o banco

```sql
CREATE DATABASE logistica;
```

O schema é criado automaticamente pelo Hibernate (`ddl-auto: create-drop`) e pelo Spring Batch (`initialize-schema: always`) ao subir a aplicação.

---

## Configuração

### `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/logistica
    username: postgres
    password: 123456
    hikari:
      maximum-pool-size: 10

  jpa:
    hibernate:
      ddl-auto: create-drop       # recria tabelas a cada startup
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 500
          order_inserts: true

  batch:
    job:
      enabled: false              # não executa job automaticamente ao subir
    jdbc:
      initialize-schema: always   # cria tabelas do Spring Batch automaticamente

batch:
  logistica:
    chunk-size: 500
    skip-limit: 100
    arquivo:
      correios:      src/main/resources/samples/correios_ocorrencias_20240115.csv
      jadlog:        src/main/resources/samples/jadlog_ocorrencias_20240115.csv
      total-express: src/main/resources/samples/totalexpress_ocorrencias_20240115.txt
```

Os caminhos dos arquivos podem ser sobrescritos via variáveis de ambiente:

```bash
BATCH_ARQUIVO_CORREIOS=/data/correios.csv
BATCH_ARQUIVO_JADLOG=/data/jadlog.csv
BATCH_ARQUIVO_TOTAL_EXPRESS=/data/totalexpress.txt
```

---

## Executando

### Build

```bash
mvn clean package -DskipTests
```

### Rodando a aplicação

```bash
mvn spring-boot:run
```

### Disparando o Job via HTTP

```bash
curl -X POST http://localhost:8080/batch/iniciar
```

Resposta:
```json
{
  "steps": {
    "correiosStep": {
      "processados": 1000,
      "status": "COMPLETED",
      "lidos": 1000,
      "erros": [],
      "ignorados": 0
    },
    "jadlogStep": {
      "processados": 1000,
      "status": "COMPLETED",
      "lidos": 1000,
      "erros": [],
      "ignorados": 0
    },
    "totalExpressStep": {
      "processados": 1000,
      "status": "COMPLETED",
      "lidos": 1000,
      "erros": [],
      "ignorados": 0
    }
  },
  "status": "COMPLETED",
  "inicio": "2026-03-10T01:55:44.5782585",
  "fim": "2026-03-10T01:55:45.4159426",
  "executionId": 1
}
```

### Log esperado de uma execução bem-sucedida

```
Executing step: [correiosStep]
[correiosStream] Leitura concluída. Total: 1000
Step: [correiosStep] executed in 267ms

Executing step: [jadlogStep]
[jadlogStream] Leitura concluída. Total: 1000
Step: [jadlogStep] executed in 42ms

Executing step: [totalExpressStep]
[totalExpressStream] Leitura concluída. Total: 1000
Step: [totalExpressStep] executed in 38ms

Job: [processarOcorrenciasJob] completed with status: [COMPLETED]
```

---

## Formatos de Arquivo

### Correios — delimitado por `|`

```
codigo_rastreio|tipo_ocorrencia|descricao_ocorrencia|data_ocorrencia|hora_ocorrencia|cpf_destinatario|nome_destinatario|cep_destino|cidade_destino|uf_destino|peso_gramas|valor_declarado
AA100000000BR|BDE|OBJETO ENTREGUE AO DESTINATARIO|15/01/2024|14:32|123.456.789-00|JOAO DA SILVA|01310-100|SAO PAULO|SP|450|0.00
```

| Campo | Formato |
|---|---|
| `data_ocorrencia` | `dd/MM/yyyy` |
| `cpf_destinatario` | com máscara `000.000.000-00` |
| `cep_destino` | com hífen `00000-000` |

**Códigos de ocorrência:**

| Código | Status normalizado |
|---|---|
| `BDE` | `ENTREGUE` |
| `BDI`, `OEC` | `SAIU_PARA_ENTREGA` |
| `IDC`, `RDE` | `DEVOLVIDO` |
| `EXT` | `EXTRAVIO` |

---

### Jadlog — CSV separado por `,`

```
NR_CONHECIMENTO,DT_OCORRENCIA,HR_OCORRENCIA,CD_OCORRENCIA,DS_OCORRENCIA,NR_CPF_CNPJ,NM_DESTINATARIO,NR_CEP,NM_CIDADE,SG_UF,VL_FRETE,VL_MERCADORIA,NR_TENTATIVA
JL9000000000,2024-01-15,14:32:00,D,ENTREGUE,12345678900,JOAO DA SILVA,01310100,SAO PAULO,SP,18.50,0.00,1
```

| Campo | Formato |
|---|---|
| `DT_OCORRENCIA` | `yyyy-MM-dd` |
| `HR_OCORRENCIA` | `HH:mm:ss` |
| `NR_CEP` | sem hífen, 8 dígitos |
| `NR_CPF_CNPJ` | apenas dígitos, sem máscara |

**Códigos de ocorrência:**

| Código | Status normalizado |
|---|---|
| `D` | `ENTREGUE` |
| `S` | `SAIU_PARA_ENTREGA` |
| `T` | `EM_TRANSITO` |
| `V` | `TENTATIVA_FALHA` |
| `R` | `DEVOLVIDO` |
| `E` | `EXTRAVIO` |

---

### Total Express — posicional (fixed-length)

Cada linha possui **exatamente 110 caracteres**, sem separador. O layout é:

| Col | Tamanho | Campo | Observação |
|---|---|---|---|
| 001–015 | 15 | `numeroEncomenda` | ex: `TE100000000    ` |
| 016–029 | 14 | `documentoDestinatario` | CPF (11) padded com espaços à dir, ou CNPJ (14) |
| 030–051 | 22 | `nomeDestinatario` | padded com espaços |
| 052–059 | 8 | `cepDestino` | sem hífen |
| 060–079 | 20 | `cidadeDestino` | padded com espaços |
| 080–081 | 2 | `ufDestino` | sigla |
| 082–089 | 8 | `dataOcorrencia` | `yyyyMMdd` |
| 090–093 | 4 | `horaOcorrencia` | `HHmm` |
| 094–096 | 3 | `codigoStatus` | ex: `ENT` |
| 097–100 | 4 | `pesoGramas` | right-aligned com espaço |
| 101–109 | 9 | `valorMercadoria` | em centavos zero-padded; dividido por 100 no processor |
| 110 | 1 | `numeroDeTentativa` | `1` ou `2` |

**Códigos de status:**

| Código | Status normalizado |
|---|---|
| `ENT` | `ENTREGUE` |
| `SAI` | `SAIU_PARA_ENTREGA` |
| `TRS` | `EM_TRANSITO` |
| `AUS`, `REC` | `TENTATIVA_FALHA` |
| `DEV` | `DEVOLVIDO` |
| `EXT` | `EXTRAVIO` |

> **Atenção:** arquivos posicionais não suportam campos de tamanho variável. O `documentoDestinatario` deve sempre ocupar 14 chars — CPF é paddado com espaços à direita.

---

## Arquitetura

### Por que um Step por transportadora?

Cada transportadora tem seu próprio Step independente, o que garante:

- **Checkpoint granular**: falha na Jadlog não reprocessa os Correios já concluídos.
- **Restart cirúrgico**: em caso de reprocessamento, apenas o Step que falhou é reexecutado a partir do último checkpoint.
- **Monitoramento claro**: cada Step possui métricas independentes de leitura, escrita e skip em `BATCH_STEP_EXECUTION`.

### `TransportadoraItemReader<T>` — reader genérico com restart

Um único reader genérico serve para todos os formatos. O `streamName` passado no construtor determina qual mapeamento BeanIO usar.

```java
// reutilização do mesmo reader para formatos completamente diferentes
TransportadoraItemReader correios     = new TransportadoraItemReader<>(resource, "correiosStream");
TransportadoraItemReader jadlog       = new TransportadoraItemReader<>(resource, "jadlogStream");
TransportadoraItemReader totalExpress = new TransportadoraItemReader<>(resource, "totalExpressStream");
```

A posição atual é persistida no `ExecutionContext` a cada chunk via `update()`. Em caso de restart, o reader pula automaticamente os registros já processados.

### `OcorrenciaEntregaWriter` — writer compartilhado

Todos os processors produzem `OcorrenciaEntrega`, então um único writer serve para todos os Steps. O `saveAll()` combinado com `hibernate.jdbc.batch_size=500` e `order_inserts=true` garante inserção em batch eficiente no PostgreSQL.

### `TransportadoraSkipListener` — tolerância a falhas

Configurado com `skipLimit=100`, o job tolera até 100 registros inválidos por Step antes de falhar. Cada skip é logado com contexto:

```
[SKIP][READ]    erro de parsing ou mapeamento BeanIO
[SKIP][PROCESS] erro de transformação no Processor
[SKIP][WRITE]   erro de persistência no Writer
```

---

## Entidade de Saída

Todos os formatos são normalizados para a tabela `ocorrencia_entrega`:

```sql
CREATE TABLE ocorrencia_entrega (
    id                         BIGINT PRIMARY KEY,
    codigo_rastreio            VARCHAR(30)   NOT NULL,
    transportadora             VARCHAR(30)   NOT NULL,  -- CORREIOS | JADLOG | TOTAL_EXPRESS
    status                     VARCHAR(30)   NOT NULL,  -- ver enum StatusEntrega abaixo
    codigo_ocorrencia_original VARCHAR(10),
    descricao_ocorrencia       VARCHAR(200),
    data_hora_ocorrencia       TIMESTAMP     NOT NULL,
    cpf_cnpj_destinatario      VARCHAR(14),
    nome_destinatario          VARCHAR(100),
    cep_destino                VARCHAR(9),
    cidade_destino             VARCHAR(60),
    uf_destino                 VARCHAR(2),
    valor_mercadoria           NUMERIC(12,2),
    nr_tentativa_entrega       INT,
    processado_em              TIMESTAMP     NOT NULL   -- preenchido via @PrePersist
);
```

**Enum `StatusEntrega`:**

```
ENTREGUE | SAIU_PARA_ENTREGA | EM_ROTA | EM_TRANSITO | TENTATIVA_FALHA | DEVOLVIDO | EXTRAVIO | DESCONHECIDO
```

**Índices criados automaticamente:**

```sql
CREATE INDEX idx_rastreio       ON ocorrencia_entrega (codigo_rastreio);
CREATE INDEX idx_transportadora ON ocorrencia_entrega (transportadora);
CREATE INDEX idx_status         ON ocorrencia_entrega (status);
```

---

## Adicionando uma Nova Transportadora

1. Criar o Record em `reader/{transportadora}/NomeDaTransportadoraRecord.java` com `@Data @NoArgsConstructor @AllArgsConstructor`
2. Adicionar o `<stream>` no `transportadoras-mapping.xml`
3. Criar o Processor em `processor/{transportadora}/NomeDaTransportadoraProcessor.java`
4. Adicionar o valor no enum `OcorrenciaEntrega.Transportadora`
5. Adicionar o path em `BatchProperties.Arquivo` e no `application.yml`
6. Declarar o `@Bean` do reader e do step em `BatchConfig` e encadear com `.next()`

---

## Decisões Técnicas

| Decisão | Motivo |
|---|---|
| BeanIO em vez de `FlatFileItemReader` nativo | Suporte a múltiplos layouts no mesmo XML, incluindo fixed-length com `ignoreUnidentifiedRecords` |
| Classes `@Data @NoArgsConstructor` em vez de Java Records | BeanIO exige construtor padrão — Records não possuem e causam `InvalidBeanClassException` |
| `@ConfigurationProperties` sem `@Component` | Evita registro duplo de bean no Spring Boot 4; registrar via `@EnableConfigurationProperties` na classe principal |
| `chunk().transactionManager()` em vez de `chunk(n, tm)` | API do Spring Batch 6 — a sobrecarga com dois parâmetros foi depreciada |
| `JobOperator` em vez de `JobLauncher` direto | `JobOperator` estende `JobLauncher` no Spring Batch 6; `start(Job, JobParameters)` é o método atual |
| Imports `org.springframework.batch.infrastructure.item.*` | Pacotes de `ItemStreamReader`, `ItemWriter`, `Chunk` e `ExecutionContext` foram movidos no Spring Batch 6 |
| `allocationSize = 500` na sequence | Alinhado ao `chunk-size`, evita roundtrips ao banco para geração de IDs |