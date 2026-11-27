# Capitania de Belém — controle de barcos e atracações

Trabalho de Teoria dos Grafos (árvores). Um sistema simples de controle de barcos e atracações nos rios de Belém, em que **cada árvore resolve um problema diferente**:

| Árvore | Problema que resolve | Pacote | Quem fez |
|---|---|---|---|
| **2-3-4** (não binária) | Registro de barcos pela inscrição: cadastrar, dar baixa, consultar | `com.projeto.arvore234` | Aiam |
| **kD** (2 dimensões) | Mapa: barco mais próximo de um ponto e barcos numa região | `com.projeto.kd` | Nathalia |
| **Rubro-negra** | Agenda de atracações por horário: agendar, cancelar, buscar, próxima atracação | `com.projeto.arvorerubronegra` | Arthur Ono (25070052) |

As três são unidas pela classe **`Capitania`** (`com.projeto.capitania`), que mantém registro, mapa e agenda coerentes entre si (veja "Como as três se conectam").

Repositório: https://github.com/Aiam-M/Grafos-Arvore-para-Sistema-de-controle-de-Barcos

Nenhuma biblioteca de árvores é usada: todas as árvores são feitas com nós ligados. A 2-3-4 usa a lista duplamente encadeada própria (`com.projeto.estrutura`). A kD e a rubro-negra usam `java.util.List`/`ArrayList` apenas como apoio (reconstrução e listas de resultado).

## Como executar

Requisitos: **Java 21** (JDK). Maven e Docker são opcionais. O Graphviz (`dot`) também é opcional: sem ele só os arquivos `.dot` são gerados, com ele saem também os `.png`.

Opções do programa: `234`, `kd`, `rn`, `capitania` ou `todas`. Sem argumento roda `todas` (as quatro demos).

### Só com o JDK (sem Maven)

Na pasta que contém o `pom.xml`:

```powershell
# Windows (PowerShell)
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | % FullName)
java "-Dfile.encoding=UTF-8" -cp out com.projeto.Main            # todas as demos
java "-Dfile.encoding=UTF-8" -cp out com.projeto.Main rn         # só a rubro-negra
```

```bash
# Linux / macOS
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp out com.projeto.Main capitania
```

### Com Maven

```bash
mvn package
java -jar target/barcos-belem.jar                # roda todas as demos
java -jar target/barcos-belem.jar 234            # só a 2-3-4
java -jar target/barcos-belem.jar kd             # só a kD
java -jar target/barcos-belem.jar rn             # só a rubro-negra
java -jar target/barcos-belem.jar capitania      # só a Capitania (as três juntas)
```

Também é possível rodar sem gerar o jar: `mvn compile exec:java -Dexec.args="rn"`.

### Com Docker (já inclui o Graphviz)

```bash
docker build -t barcos-belem .
docker run --rm -v "$(pwd)/saida:/app/saida" barcos-belem               # todas as demos
docker run --rm -v "$(pwd)/saida:/app/saida" barcos-belem capitania     # só uma (234, kd, rn ou capitania)
```

### Pelo VS Code ou IntelliJ

Abra a **pasta que contém o `pom.xml`** (abrir só a `src` faz o editor não achar as outras classes) e execute `com.projeto.Main` (ou a `main` de `Demo234`, `DemoKD`, `DemoRubroNegra` ou `DemoCapitania`).

### Saída

As demos imprimem um resumo no terminal e gravam os desenhos da árvore na pasta `saida/`:
`insercao_*`, `remocao` (2-3-4), `kd_*` (kD), `rn_*` (rubro-negra) e `cap_*` (Capitania: `cap_registro`, `cap_mapa`, `cap_agenda`). Para desenhar um `.dot` à mão: `dot -Tpng saida/arquivo.dot -o saida/arquivo.png`.

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
├── Main.java                    # roda as demos: 234 | kd | rn | capitania | todas
├── modelo/                      # Barco (inscrição autoincremento, nome, modelo, status), StatusBarco (ATIVO, EM_MANUTENCAO)
├── estrutura/                   # lista duplamente encadeada
├── arvore234/                   # Arvore234, No234, ExportadorDot, Demo234
├── kd/                          # ArvoreKD, NoKD, Posicao, DemoKD
├── arvorerubronegra/            # AgendaRubroNegra, NoRN, Atracacao, DemoRubroNegra
└── capitania/                   # Capitania (une as três), ResultadoAgendamento, DemoCapitania
```

## Como as três se conectam

A classe `Capitania` guarda o registro (2-3-4), o mapa (kD, só com barcos ativos) e a agenda (rubro-negra), e mantém os três coerentes:

- **Cadastrar barco:** entra no registro (2-3-4) e no mapa (kD).
- **Agendar atracação:** confere se o barco existe no registro e se está ativo, e entra na agenda (rubro-negra). O resultado diz o que aconteceu: `AGENDADA`, `BARCO_INEXISTENTE`, `EM_MANUTENCAO`, `HORARIO_OCUPADO` ou `HORARIO_INVALIDO`.
- **Trancar (manutenção):** o barco continua no registro com status "em manutenção", sai da agenda (`AgendaRubroNegra.cancelarDoBarco`) e a kD é reconstruída sem ele.
- **Reativar:** o barco volta ao mapa e pode agendar de novo.
- **Dar baixa:** sai das três.

`Capitania.verificar()` confere se cada árvore continua válida e se as três estão de acordo entre si (o mapa tem exatamente os barcos ativos, toda atracação é de um barco do registro e nenhum barco em manutenção tem atracação). A `DemoCapitania` mostra esse fluxo com 10 barcos e termina com um teste aleatório contra um modelo simples.
