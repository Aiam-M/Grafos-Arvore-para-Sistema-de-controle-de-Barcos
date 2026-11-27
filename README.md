# Capitania de Belém — controle de barcos e atracações

Trabalho de Teoria dos Grafos (árvores). Um sistema simples de controle de barcos e atracações nos rios de Belém, em que **cada árvore resolve um problema diferente**:

| Árvore | Problema que resolve | Pacote | Quem fez |
|---|---|---|---|
| **2-3-4** (não binária) | Registro de barcos pela inscrição: cadastrar, dar baixa, consultar | `com.projeto.arvore234` | Aiam |
| **kD** (2 dimensões) | Mapa: barco mais próximo de um ponto e barcos numa região | `com.projeto.kd` | Nathalia |
| **Rubro-negra** | Agenda de atracações por horário: agendar, cancelar, buscar, próxima atracação | `com.projeto.arvorerubronegra` | Arthur Ono (25070052) |

Nenhuma biblioteca de árvores é usada: todas as árvores são feitas com nós ligados. A 2-3-4 usa a lista duplamente encadeada própria (`com.projeto.estrutura`). A kD e a rubro-negra usam `java.util.List`/`ArrayList` apenas como apoio (reconstrução e listas de resultado).

## Como executar

Requisitos: Java 21 e Maven 3.9+ (ou Docker). O Graphviz (`dot`) é opcional: sem ele só os arquivos `.dot` são gerados, com ele saem também os `.png`.

### Com Maven

```bash
mvn package
java -jar target/barcos-belem.jar          # roda as três demos
java -jar target/barcos-belem.jar 234      # só a 2-3-4
java -jar target/barcos-belem.jar kd       # só a kD
java -jar target/barcos-belem.jar rn       # só a rubro-negra
```

Também é possível rodar sem gerar o jar: `mvn compile exec:java -Dexec.args="rn"`.

### Com Docker (já inclui o Graphviz)

```bash
docker build -t barcos-belem .
docker run --rm -v "$(pwd)/saida:/app/saida" barcos-belem        # as três demos
docker run --rm -v "$(pwd)/saida:/app/saida" barcos-belem rn     # só uma (234, kd ou rn)
```

### Pelo IntelliJ

Execute `com.projeto.Main` (ou a `main` de `Demo234`, `DemoKD` ou `DemoRubroNegra`).

### Saída

As demos imprimem um resumo no terminal e gravam os desenhos da árvore na pasta `saida/`:
`insercao_*`, `remocao` (2-3-4), `kd_*` (kD) e `rn_*` (rubro-negra). Para desenhar um `.dot` à mão: `dot -Tpng saida/arquivo.dot -o saida/arquivo.png`.

## Regras de cada árvore

| Árvore | Elementos repetidos | Balanceamento |
|---|---|---|
| 2-3-4 | Inscrição repetida é **rejeitada** | Inserção: divide (split) nós cheios já na descida. Remoção: empresta do irmão ou funde. |
| kD | Pontos iguais são **aceitos** (vão para a direita; menor vai para a esquerda). Não tem remoção: ao trancar um barco, a árvore é reconstruída pela mediana. | Só na reconstrução (mediana) |
| Rubro-negra | Horário repetido é **rejeitado** (conflito de atracação) | Inserção: nó novo entra vermelho; tio vermelho recolore, tio preto rotaciona. Remoção: corrige o "duplo preto" olhando o irmão (4 casos). |

Os dados das demos entram em ordem crescente (pior caso para árvores sem balanceamento) e as demos usam pelo menos 21 elementos.

## Estrutura

```
src/main/java/com/projeto/
├── Main.java                    # roda as demos
├── modelo/Barco.java            # inscrição (autoincremento), nome, modelo
├── estrutura/                   # lista duplamente encadeada
├── arvore234/                   # Arvore234, No234, ExportadorDot, Demo234
├── kd/                          # ArvoreKD, NoKD, Posicao, DemoKD
└── arvorerubronegra/            # AgendaRubroNegra, NoRN, Atracacao, DemoRubroNegra
```

## Como as três se conectam (planejado)

- **Cadastrar barco:** entra no registro (2-3-4) e no mapa (kD).
- **Agendar atracação:** confere se o barco existe no registro e entra na agenda (rubro-negra).
- **Trancar (manutenção):** o barco continua no registro com status "em manutenção", sai da agenda (`AgendaRubroNegra.cancelarDoBarco`) e a kD é reconstruída sem ele.
- **Reativar:** o barco volta ao mapa e pode agendar de novo.
- **Dar baixa:** sai das três.

Cada árvore já funciona e é demonstrada separadamente; a classe que une as três ainda não foi escrita.
