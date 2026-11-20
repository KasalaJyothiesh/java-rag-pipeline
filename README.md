# Java RAG Pipeline

An end-to-end Retrieval-Augmented Generation (RAG) application built with **Java, Spring Boot, LangChain4j, Google Gemini, PostgreSQL, and pgvector**.

The application loads business documents, splits them into smaller text segments, generates vector embeddings, stores those embeddings in PostgreSQL, and retrieves relevant context to answer user questions.

## Features

- Loads `.txt` documents from the application classpath.
- Splits documents using LangChain4j's recursive document splitter.
- Generates 768-dimensional embeddings with Google's `gemini-embedding-001`.
- Stores text segments, embeddings, and source metadata in PostgreSQL using pgvector.
- Retrieves relevant segments through a vector-based content retriever.
- Uses Google Gemini to generate answers grounded in retrieved context.
- Exposes the RAG service through a Spring Boot controller.

## Tech stack

| Technology | Purpose |
|---|---|
| Java 21 | Configured Java version |
| Spring Boot 4.1.2 | Application framework |
| LangChain4j 1.23.0 | Document ingestion, embeddings, retrieval, and AI service integration |
| Google Gemini | Chat and embedding models |
| PostgreSQL 16 | Relational database |
| pgvector | Vector storage and similarity search |
| Maven | Build and dependency management |

## RAG workflow

```text
INGESTION
Text documents (.txt)
        |
Document loading
        |
Recursive splitting
        |
Text segments / chunks
        |
Gemini embedding model
        |
768-dimensional vectors
        |
PostgreSQL + pgvector

RETRIEVAL AND GENERATION
User question
        |
Query embedding
        |
Vector similarity search
        |
Relevant text segments
        |
Retrieved context + question
        |
Gemini chat model
        |
Final answer
```

## Project structure

```text
first-rag-pipeline/
├── src/
│   ├── main/
│   │   ├── java/com/jyothiesh/first_rag_pipeline/
│   │   │   ├── assistant/
│   │   │   │   └── Assistant.java
│   │   │   ├── configuration/
│   │   │   │   └── RagConfiguration.java
│   │   │   ├── controller/
│   │   │   │   └── RagController.java
│   │   │   ├── service/
│   │   │   │   └── RagService.java
│   │   │   └── FirstRagPipelineApplication.java
│   │   └── resources/
│   │       ├── documents/
│   │       │   ├── jyothiesh_business.txt
│   │       │   └── jyothiesh_business_policies.txt
│   │       └── application.properties
│   └── test/
├── .gitignore
├── pom.xml
└── README.md
```

## Prerequisites

- JDK 21 or later (the project is configured for Java 21).
- Maven, or use the included Maven wrapper.
- Docker Desktop.
- A Google Gemini API key from [Google AI Studio](https://aistudio.google.com/apikey).

## Configuration

The application reads Gemini credentials from environment variables. Do not write API keys into source files.

Relevant `src/main/resources/application.properties` entries:

```properties
spring.application.name=first-rag-pipeline

# Gemini
gemini.api-key=${GEMINI_API_KEY}
gemini.model-name=${MODEL_NAME}

# RAG
rag.store=pgvector

# PostgreSQL / pgvector
rag.pgvector.host=localhost
rag.pgvector.port=5433
rag.pgvector.database=ragdb
rag.pgvector.user=postgres
rag.pgvector.password=postgres
rag.pgvector.table=embeddings

# Gemini embeddings
gemini.embedding.model-name=gemini-embedding-001
gemini.embedding.dimensions=768
```

Set these environment variables in your operating system or IDE run configuration:

| Variable | Value |
|---|---|
| `GEMINI_API_KEY` | Your Google Gemini API key |
| `MODEL_NAME` | `gemini-3.1-flash-lite` |

In IntelliJ IDEA, open **Run → Edit Configurations**, select the application run configuration, and add the variables under **Environment variables**.

Never commit real API keys or credentials to Git.

> The PostgreSQL username and password above are local development defaults. Change them for other environments and keep real credentials out of version control.

## Start PostgreSQL with pgvector

Run:

```bash
docker run -d --name pgvector -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=ragdb pgvector/pgvector:pg16
```

Enable the vector extension:

```bash
docker exec -it pgvector psql -U postgres -d ragdb
```

Then execute:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

The application uses 768-dimensional embeddings and the `embeddings` table.

## Run the application

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/java-rag-pipeline.git
cd java-rag-pipeline
```

Configure the environment variables, start PostgreSQL, and run the application.

**Windows PowerShell**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux**

```bash
./mvnw spring-boot:run
```

The current implementation seeds the knowledge base during application startup using `@PostConstruct` in `RagService`. It loads `.txt` files from `classpath:documents/*.txt`.

## Example questions

The sample documents describe the fictional Jyothiesh Business, its products, and internal policies.

Try questions such as:

- What does Jyothiesh Business develop?
- What is the internal headquarters code?
- How often are employee performance reviews conducted?
- What is the monthly price of JyoBot?
- What approval is required for project expenses above ₹35,000?

Example:

```text
Question: What is the internal headquarters code of Jyothiesh Business?

Answer: The internal headquarters code of Jyothiesh Business is JTB-204.
```

## Current implementation notes

- The sample knowledge base consists of plain-text (`.txt`) documents.
- The embedding model is configured for 768 dimensions.
- The retriever is currently configured for up to 10 results with a minimum score of `0.0` as a permissive retrieval test configuration. Evaluate and tune these settings using representative questions before production use.
- The knowledge base is seeded at application startup. Repeated restarts may insert duplicate records unless ingestion is made idempotent or existing records are cleared appropriately.
- The exact HTTP route and request format are defined in `RagController.java`.

## Possible future improvements

- Add PDF and DOCX document ingestion.
- Add metadata-based filtering.
- Evaluate and tune chunk size, overlap, and retrieval thresholds.
- Add hybrid search and reranking.
- Add ingestion deduplication and document update handling.
- Add automated retrieval and answer-quality evaluation.
- Add authentication and production-ready configuration.

## License

No license has been added yet. All rights reserved unless a license is added to this repository.
