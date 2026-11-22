package com.projeto.arvore234;

import com.projeto.estrutura.ListaDuplamenteEncadeada;
import com.projeto.modelo.Barco;

/**
 * Nó de uma árvore 2-3-4. Guarda de 1 a 3 barcos (chaves) e, se não for folha,
 * uma lista de filhos com uma posição a mais que o número de chaves.
 */
class No234 {

    static final int MAX_CHAVES = 3;

    final ListaDuplamenteEncadeada<Barco> chaves = new ListaDuplamenteEncadeada<>();
    final ListaDuplamenteEncadeada<No234> filhos = new ListaDuplamenteEncadeada<>();

    /**
     * @return {@code true} se o nó não possui filhos
     */
    boolean ehFolha() {
        return filhos.estaVazia();
    }

    /**
     * @return {@code true} se o nó já tem o máximo de 3 chaves
     */
    boolean estaCheio() {
        return chaves.tamanho() == MAX_CHAVES;
    }

    /**
     * Calcula por qual filho a busca deve descer (ou a posição da chave, se existir no nó).
     *
     * @param inscricao inscrição procurada
     * @return quantidade de chaves do nó com inscrição menor que a informada
     */
    int posicao(int inscricao) {
        int i = 0;
        while (i < chaves.tamanho() && chaves.obter(i).getInscricao() < inscricao) {
            i++;
        }
        return i;
    }
}
