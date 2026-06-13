# Diagramas UML

Dez diagramas PlantUML documentando arquitetura, domínio e fluxos do **github-collaboration-graph**. Cada `.puml` possui exportação `.png` correspondente.

| Arquivo | Assunto |
|---------|---------|
| `arquitetura-camadas` | Camadas da aplicação e fluxo de dados |
| `classes-dominio` | Modelo de domínio (usuários, interações, repositório) |
| `api-grafos` | API própria (`AbstractGraph`, matriz e lista) |
| `diagrama-pacotes` | Organização dos pacotes Java |
| `modelagem-github` | Mapeamento de dados da API GitHub |
| `fluxo-construcao-grafos` | Construção dos quatro grafos |
| `sequencia-exportacao-gexf` | Exportação para Gephi |
| `sequencia-analise-grafo-integrado` | Cálculo de métricas no grafo integrado |
| `sequencia-demo-offline` | Demonstração offline (contingência) |
| `artefatos-saida` | Arquivos gerados em `output/` |

Três diagramas são reutilizados no relatório LaTeX, em [`../relatorio/figuras/`](../relatorio/figuras/): `Arquitetura_Camadas`, `Classes_Dominio` e `Classes_API`.
