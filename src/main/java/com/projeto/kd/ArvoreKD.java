package com.projeto.kd;

import java.util.ArrayList;
import java.util.List;

/**
 * Árvore kD com 2 dimensões: guarda a posição (latitude e longitude) dos barcos ativos.
 * Nos níveis pares, a árvore compara a latitude e nos ímpares compara a longitude.
 * Regra de empate: valor menor vai para a esquerda; igual ou maior vai para a direita.
 * Não tem remoção: quando um barco sai do mapa, a árvore é reconstruída com reconstruir().
 */
public class ArvoreKD {

    private NoKD raiz;

    // usados durante as consultas
    private Posicao melhor;
    private double menorDistancia;
    private int visitas;

    /**
     * Devolve o valor que o nível usa para comparar: latitude nos níveis pares, longitude nos ímpares.
     */
    private double valor(Posicao p, int nivel) {
        if (nivel % 2 == 0) {
            return p.getLat();
        }
        return p.getLon();
    }

    // 

    /**
     * Insere um barco no mapa.
     */
    public void inserir(Posicao nova) {
        raiz = inserir(raiz, nova, 0);
    }

    private NoKD inserir(NoKD atual, Posicao nova, int nivel) {
        if (atual == null) {
            return new NoKD(nova);
        }
        if (valor(nova, nivel) < valor(atual.posicao, nivel)) {
            atual.esq = inserir(atual.esq, nova, nivel + 1);
        } else {
            atual.dir = inserir(atual.dir, nova, nivel + 1);
        }
        return atual;
    }

    // RECONSTRUÇÃO

    /**
     * Refaz a árvore do zero a partir da lista de barcos ativos.
     * Em cada nível ordena a lista pelo eixo e usa o elemento do meio (mediana) como nó,
     * o que deixa a árvore balanceada.
     */
    public void reconstruir(List<Posicao> posicoes) {
        List<Posicao> copia = new ArrayList<>(posicoes);
        raiz = construir(copia, 0);
    }

    private NoKD construir(List<Posicao> lista, int nivel) {
        if (lista.isEmpty()) {
            return null;
        }
        lista.sort((a, b) -> Double.compare(valor(a, nivel), valor(b, nivel)));

        int meio = lista.size() / 2;
        // se tem valores iguais ao do meio, anda para trás para todos os iguais irem para a direita
        while (meio > 0 && valor(lista.get(meio - 1), nivel) == valor(lista.get(meio), nivel)) {
            meio--;
        }

        NoKD no = new NoKD(lista.get(meio));
        List<Posicao> esquerda = new ArrayList<>(lista.subList(0, meio));
        List<Posicao> direita = new ArrayList<>(lista.subList(meio + 1, lista.size()));
        no.esq = construir(esquerda, nivel + 1);
        no.dir = construir(direita, nivel + 1);
        return no;
    }

    // MAIS PRÓXIMO

    /**
     * Acha o barco mais próximo de um ponto.
     * Desce primeiro pelo lado do ponto. Na volta, só olha o outro lado se a reta de corte
     * estiver mais perto do que o melhor barco achado até agora; senão aquele lado é descartado.
     *
     * @return a posição mais próxima, ou null se o mapa estiver vazio
     */
    public Posicao maisProximo(double lat, double lon) {
        melhor = null;
        menorDistancia = Double.MAX_VALUE;
        visitas = 0;
        procurarMaisProximo(raiz, lat, lon, 0);
        return melhor;
    }

    private void procurarMaisProximo(NoKD atual, double lat, double lon, int nivel) {
        if (atual == null) {
            return;
        }
        visitas++;

        // distância ao quadrado (não precisa da raiz para comparar)
        double dLat = atual.posicao.getLat() - lat;
        double dLon = atual.posicao.getLon() - lon;
        double distancia = dLat * dLat + dLon * dLon;
        if (distancia < menorDistancia) {
            menorDistancia = distancia;
            melhor = atual.posicao;
        }

        double alvo;
        if (nivel % 2 == 0) {
            alvo = lat;
        } else {
            alvo = lon;
        }
        double diferenca = alvo - valor(atual.posicao, nivel);

        NoKD mesmoLado;
        NoKD outroLado;
        if (diferenca < 0) {
            mesmoLado = atual.esq;
            outroLado = atual.dir;
        } else {
            mesmoLado = atual.dir;
            outroLado = atual.esq;
        }

        procurarMaisProximo(mesmoLado, lat, lon, nivel + 1);
        if (diferenca * diferenca < menorDistancia) {
            procurarMaisProximo(outroLado, lat, lon, nivel + 1);
        }
    }

    // REGIÃO

    /**
     * Lista os barcos que estão dentro de um retângulo do mapa (as bordas contam).
     */
    public List<Posicao> naRegiao(double latMin, double latMax, double lonMin, double lonMax) {
        List<Posicao> achados = new ArrayList<>();
        visitas = 0;
        procurarNaRegiao(raiz, latMin, latMax, lonMin, lonMax, 0, achados);
        return achados;
    }

    private void procurarNaRegiao(NoKD atual, double latMin, double latMax,
                                  double lonMin, double lonMax, int nivel, List<Posicao> achados) {
        if (atual == null) {
            return;
        }
        visitas++;

        Posicao p = atual.posicao;
        boolean dentro = p.getLat() >= latMin && p.getLat() <= latMax
                && p.getLon() >= lonMin && p.getLon() <= lonMax;
        if (dentro) {
            achados.add(p);
        }

        double minimo;
        double maximo;
        if (nivel % 2 == 0) {
            minimo = latMin;
            maximo = latMax;
        } else {
            minimo = lonMin;
            maximo = lonMax;
        }
        double v = valor(p, nivel);

        if (minimo < v) {      // pode ter barcos do lado esquerdo (valores menores)
            procurarNaRegiao(atual.esq, latMin, latMax, lonMin, lonMax, nivel + 1, achados);
        }
        if (maximo >= v) {     // pode ter barcos do lado direito (valores iguais ou maiores)
            procurarNaRegiao(atual.dir, latMin, latMax, lonMin, lonMax, nivel + 1, achados);
        }
    }

    // UTILITÁRIOS

    /**
     * @return quantos nós a última consulta visitou (mostra a poda funcionando)
     */
    public int ultimasVisitas() {
        return visitas;
    }

    public int tamanho() {
        return tamanho(raiz);
    }

    private int tamanho(NoKD no) {
        if (no == null) {
            return 0;
        }
        return 1 + tamanho(no.esq) + tamanho(no.dir);
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(NoKD no) {
        if (no == null) {
            return 0;
        }
        return 1 + Math.max(altura(no.esq), altura(no.dir));
    }

    /**
     * Confere a regra da árvore: em todo nó, tudo à esquerda é menor e tudo à direita
     * é igual ou maior no eixo daquele nível.
     */
    public boolean valida() {
        return valida(raiz, 0);
    }

    private boolean valida(NoKD no, int nivel) {
        if (no == null) {
            return true;
        }
        double v = valor(no.posicao, nivel);
        return todosMenores(no.esq, nivel, v)
                && todosMaiores(no.dir, nivel, v)
                && valida(no.esq, nivel + 1)
                && valida(no.dir, nivel + 1);
    }

    // confere o eixo do nível do ancestral (nivelAncestral) em toda a subárvore
    private boolean todosMenores(NoKD no, int nivelAncestral, double limite) {
        if (no == null) {
            return true;
        }
        if (valor(no.posicao, nivelAncestral) >= limite) {
            return false;
        }
        return todosMenores(no.esq, nivelAncestral, limite) && todosMenores(no.dir, nivelAncestral, limite);
    }

    private boolean todosMaiores(NoKD no, int nivelAncestral, double limite) {
        if (no == null) {
            return true;
        }
        if (valor(no.posicao, nivelAncestral) < limite) {
            return false;
        }
        return todosMaiores(no.esq, nivelAncestral, limite) && todosMaiores(no.dir, nivelAncestral, limite);
    }

    // DESENHO

    /**
     * Gera o código DOT da árvore para desenhar com o Graphviz.
     */
    public String paraDot(String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ArvoreKD {\n");
        sb.append("  label=\"").append(titulo).append("\"; labelloc=t; fontsize=18;\n");
        sb.append("  node [shape=box, style=filled, fillcolor=lightyellow, fontname=Helvetica];\n");
        if (raiz != null) {
            escreverNo(sb, raiz, 0, new int[]{0});
        }
        sb.append("}\n");
        return sb.toString();
    }

    private int escreverNo(StringBuilder sb, NoKD no, int nivel, int[] contador) {
        int id = contador[0]++;
        String eixo = (nivel % 2 == 0) ? "lat" : "lon";
        sb.append("  n").append(id).append(" [label=\"")
                .append(no.posicao.getBarco().getNome())
                .append("\\n(").append(no.posicao.getLat()).append(", ").append(no.posicao.getLon()).append(")")
                .append("\\ncorta em ").append(eixo).append("\"];\n");
        if (no.esq != null) {
            int filho = escreverNo(sb, no.esq, nivel + 1, contador);
            sb.append("  n").append(id).append(" -> n").append(filho).append(" [label=\"<\"];\n");
        }
        if (no.dir != null) {
            int filho = escreverNo(sb, no.dir, nivel + 1, contador);
            sb.append("  n").append(id).append(" -> n").append(filho).append(" [label=\">=\"];\n");
        }
        return id;
    }
}