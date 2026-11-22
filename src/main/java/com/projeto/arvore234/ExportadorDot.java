package com.projeto.arvore234;

/**
 * Converte a árvore 2-3-4 para o formato DOT, que pode ser desenhado com o Graphviz
 * (ex.: {@code dot -Tpng arvore.dot -o arvore.png}).
 */
final class ExportadorDot {

    private ExportadorDot() {
    }

    /**
     * Gera o código DOT de uma árvore a partir de sua raiz.
     *
     * @param raiz raiz da árvore (pode ser {@code null})
     * @param titulo texto exibido no topo do desenho
     * @return código DOT completo
     */
    static String gerar(No234 raiz, String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Arvore234 {\n");
        sb.append("  label=\"").append(titulo).append("\"; labelloc=t; fontsize=18;\n");
        sb.append("  node [shape=record, style=filled, fillcolor=lightblue, fontname=Helvetica];\n");
        if (raiz != null) {
            escreverNo(sb, raiz, new int[]{0});
        }
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Escreve o nó e, recursivamente, seus filhos.
     *
     * @param contador contador compartilhado que dá um identificador único a cada nó
     * @return identificador do nó escrito
     */
    private static int escreverNo(StringBuilder sb, No234 no, int[] contador) {
        int id = contador[0]++;
        sb.append("  n").append(id).append(" [label=\"");
        for (int i = 0; i < no.chaves.tamanho(); i++) {
            if (i > 0) sb.append("|");
            sb.append(no.chaves.obter(i).getInscricao());
        }
        sb.append("\"];\n");
        for (int i = 0; i < no.filhos.tamanho(); i++) {
            int idFilho = escreverNo(sb, no.filhos.obter(i), contador);
            sb.append("  n").append(id).append(" -> n").append(idFilho).append(";\n");
        }
        return id;
    }
}
