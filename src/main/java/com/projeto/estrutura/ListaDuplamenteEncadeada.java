package com.projeto.estrutura;

/**
 * Lista duplamente encadeada genérica, usada pelos nós da árvore 2-3-4
 * para guardar chaves e filhos.
 *
 * @param <T> tipo dos elementos armazenados
 */
public class ListaDuplamenteEncadeada<T> {

    private static class Celula<T> {
        T valor;
        Celula<T> anterior;
        Celula<T> proxima;

        Celula(T valor) {
            this.valor = valor;
        }
    }

    private Celula<T> primeira;
    private Celula<T> ultima;
    private int tamanho;

    /**
     * @return quantidade de elementos na lista
     */
    public int tamanho() {
        return tamanho;
    }

    /**
     * @return {@code true} se a lista não possui elementos
     */
    public boolean estaVazia() {
        return tamanho == 0;
    }

    /**
     * Adiciona um elemento ao final da lista.
     *
     * @param valor elemento a adicionar
     */
    public void adicionar(T valor) {
        inserir(tamanho, valor);
    }

    /**
     * Insere um elemento em uma posição, deslocando os seguintes.
     *
     * @param indice posição de destino, entre 0 e {@link #tamanho()}
     * @param valor elemento a inserir
     * @throws IndexOutOfBoundsException se o índice for inválido
     */
    public void inserir(int indice, T valor) {
        if (indice < 0 || indice > tamanho) {
            throw new IndexOutOfBoundsException("Índice: " + indice + ", tamanho: " + tamanho);
        }
        Celula<T> nova = new Celula<>(valor);
        if (indice == tamanho) {
            nova.anterior = ultima;
            if (ultima != null) ultima.proxima = nova; else primeira = nova;
            ultima = nova;
        } else {
            Celula<T> atual = celula(indice);
            nova.proxima = atual;
            nova.anterior = atual.anterior;
            if (atual.anterior != null) atual.anterior.proxima = nova; else primeira = nova;
            atual.anterior = nova;
        }
        tamanho++;
    }

    /**
     * @param indice posição desejada
     * @return elemento na posição informada
     * @throws IndexOutOfBoundsException se o índice for inválido
     */
    public T obter(int indice) {
        return celula(indice).valor;
    }

    /**
     * Substitui o elemento de uma posição.
     *
     * @param indice posição a substituir
     * @param valor novo elemento
     * @return elemento que estava na posição
     * @throws IndexOutOfBoundsException se o índice for inválido
     */
    public T definir(int indice, T valor) {
        Celula<T> c = celula(indice);
        T antigo = c.valor;
        c.valor = valor;
        return antigo;
    }

    /**
     * Remove o elemento de uma posição.
     *
     * @param indice posição a remover
     * @return elemento removido
     * @throws IndexOutOfBoundsException se o índice for inválido
     */
    public T remover(int indice) {
        Celula<T> c = celula(indice);
        if (c.anterior != null) c.anterior.proxima = c.proxima; else primeira = c.proxima;
        if (c.proxima != null) c.proxima.anterior = c.anterior; else ultima = c.anterior;
        tamanho--;
        return c.valor;
    }

    /**
     * @return último elemento da lista
     * @throws IndexOutOfBoundsException se a lista estiver vazia
     */
    public T ultimo() {
        return obter(tamanho - 1);
    }

    /**
     * Localiza a célula de um índice percorrendo a partir da extremidade mais próxima.
     */
    private Celula<T> celula(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice: " + indice + ", tamanho: " + tamanho);
        }
        Celula<T> atual;
        if (indice < tamanho / 2) {
            atual = primeira;
            for (int i = 0; i < indice; i++) atual = atual.proxima;
        } else {
            atual = ultima;
            for (int i = tamanho - 1; i > indice; i--) atual = atual.anterior;
        }
        return atual;
    }
}
