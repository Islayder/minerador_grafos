# github-collaboration-graph

Ferramenta acadêmica (Teoria de Grafos e Computabilidade) para minerar interações no repositório **giscus/giscus**, construir quatro grafos direcionados ponderados e calcular métricas de rede. A API de grafos (matriz e lista de adjacência) é implementada no projeto, sem bibliotecas externas de grafos.

## Arquitetura

| Pacote | Responsabilidade |
|--------|------------------|
| `app` | Interfaces CLI e desktop |
| `github` | Coleta e cache da API GitHub |
| `model` | Domínio (usuários, interações, repositório) |
| `service` | Construção dos quatro grafos |
| `graph` | API própria de grafos |
| `analysis` | Métricas topológicas |
| `export` | Exportação GEXF |

## Grafos construídos

| Grafo | Conteúdo |
|-------|----------|
| Comentários | Interações em issues e PRs |
| Fechamento de issues | Quem fechou issue de quem |
| Pull requests | Revisão, aprovação e merge |
| Integrado | Rede ponderada consolidada |

## Métricas (grafo integrado)

Grau, densidade, PageRank, centralidade de autovetor, closeness, betweenness (Brandes), coeficiente de aglomeração, assortatividade, detecção de comunidades, bridging ties e contagem de aberturas de PR por autor.

## Estrutura do repositório

```
├── config/          # repositório alvo e parâmetros de mineração
├── docs/            # relatório, diagramas UML e figuras
├── src/             # código-fonte Java
├── mvnw, pom.xml
└── README.md
```

Pastas geradas em execução (não versionadas): `output/`, `cache/`, `target/`.

## Pré-requisitos

- JDK 21
- Token GitHub com acesso de leitura ao repositório alvo

Configure o token copiando `.env.example` para `.env` e preenchendo `GITHUB_TOKEN`.

## Execução

```bat
.\mvnw.cmd test
.\mvnw.cmd compile exec:java
```

| Perfil | Comando |
|--------|---------|
| CLI | `.\mvnw.cmd compile exec:java` |
| Desktop | `.\mvnw.cmd compile exec:java -Pdesktop` |
| API de grafos | `.\mvnw.cmd compile exec:java -Pgraph-api` |

Fluxo principal na CLI: opção **2** (mineração) → opções **4–7** (análise e exportação).

## Saída (`output/`)

Após mineração e exportação: arquivos GEXF dos quatro grafos, relatório textual de métricas (`integrated-analysis.txt`) e demais artefatos descritos no relatório técnico.

## Documentação

| Conteúdo | Local |
|----------|-------|
| Relatório (PDF e LaTeX) | [`docs/relatorio/`](docs/relatorio/) |
| Diagramas UML | [`docs/diagramas/`](docs/diagramas/) |
| Índice de `docs/` | [`docs/README.md`](docs/README.md) |

## Testes

167 testes unitários offline (JUnit 5), sem dependência da API real do GitHub.

```bat
.\mvnw.cmd test
```
