package com.projeto.arvore234;

import com.projeto.modelo.Barco;

/**
 * Árvore 2-3-4 que registra barcos pela inscrição.
 * Cada nó tem de 1 a 3 chaves, todas as folhas ficam no mesmo nível e
 * as operações de inserção e remoção ajustam os nós durante a descida,
 * sem nunca precisar voltar para a raiz.
 */
public class Arvore234 {

    private No234 raiz;

    /**
     * Busca um barco pela inscrição.
     *
     * @param inscricao inscrição do barco
     * @return o barco encontrado ou {@code null} se não estiver registrado
     */
    public Barco buscar(int inscricao) {
        No234 atual = raiz;
        while (atual != null) {
            int i = atual.posicao(inscricao);
            if (i < atual.chaves.tamanho() && atual.chaves.obter(i).getInscricao() == inscricao) {
                return atual.chaves.obter(i);
            }
            atual = atual.ehFolha() ? null : atual.filhos.obter(i);
        }
        return null;
    }

    /**
     * Cadastra um barco. Nós cheios encontrados na descida são divididos antes de entrar neles.
     *
     * @param barco barco a cadastrar
     * @return {@code true} se foi inserido, {@code false} se a inscrição já existe
     */
    public boolean inserir(Barco barco) {
        int inscricao = barco.getInscricao();
        if (buscar(inscricao) != null) {
            return false;
        }
        if (raiz == null) {
            raiz = new No234();
        }
        if (raiz.estaCheio()) {
            No234 novaRaiz = new No234();
            novaRaiz.filhos.adicionar(raiz);
            dividirFilho(novaRaiz, 0);
            raiz = novaRaiz;
        }
        No234 atual = raiz;
        while (!atual.ehFolha()) {
            int i = atual.posicao(inscricao);
            if (atual.filhos.obter(i).estaCheio()) {
                dividirFilho(atual, i);
                // a chave do meio subiu para a posição i: decide em qual metade continuar
                if (inscricao > atual.chaves.obter(i).getInscricao()) {
                    i++;
                }
            }
            atual = atual.filhos.obter(i);
        }
        atual.chaves.inserir(atual.posicao(inscricao), barco);
        return true;
    }

    /**
     * Dá baixa em um barco. Nós com apenas 1 chave são reforçados (empréstimo do irmão
     * ou fusão) antes de a descida continuar por eles.
     *
     * @param inscricao inscrição do barco a remover
     * @return {@code true} se removeu, {@code false} se a inscrição não existe
     */
    public boolean remover(int inscricao) {
        if (buscar(inscricao) == null) {
            return false;
        }
        removerDe(raiz, inscricao);
        if (raiz.chaves.estaVazia()) {
            raiz = raiz.ehFolha() ? null : raiz.filhos.obter(0);
        }
        return true;
    }

    /**
     * @return {@code true} se não há barcos registrados
     */
    public boolean estaVazia() {
        return raiz == null;
    }

    /**
     * @return quantidade de barcos (chaves) na árvore
     */
    public int tamanho() {
        return contarChaves(raiz);
    }

    /**
     * @return quantidade de nós da árvore
     */
    public int contarNos() {
        return contarNos(raiz);
    }

    /**
     * @return altura da árvore (0 para vazia, 1 para só a raiz)
     */
    public int altura() {
        int h = 0;
        for (No234 n = raiz; n != null; n = n.ehFolha() ? null : n.filhos.obter(0)) {
            h++;
        }
        return h;
    }

    /**
     * Verifica as propriedades da 2-3-4: 1 a 3 chaves por nó, chaves ordenadas,
     * filhos = chaves + 1 e todas as folhas no mesmo nível.
     *
     * @return {@code tr++++--ue} se a árvore está válida
     */
    public boolean valida() {
        return raiz == null || validar(raiz, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, altura());
    }

    /**
     * Gera o código DOT (Graphviz) da árvore.
     *
     * @param titulo texto exibido no topo do desenho
     * @return código DOT
     */
    public String paraDot(String titulo) {
        return ExportadorDot.gerar(raiz, titulo);
    }

    No234 raiz() {
        return raiz;
    }

    /**
     * Divide o filho cheio (3 chaves) de {@code pai} na posição {@code i}: a chave do meio
     * sobe para o pai e a maior chave vai para um novo irmão à direita.
     */
    private void dividirFilho(No234 pai, int i) {
        No234 filho = pai.filhos.obter(i);
        No234 irmao = new No234();

        irmao.chaves.adicionar(filho.chaves.remover(2));
        Barco meio = filho.chaves.remover(1);
        if (!filho.ehFolha()) {
            // filhos 2 e 3 passam para o irmão; remove do fim para manter a ordem
            irmao.filhos.inserir(0, filho.filhos.remover(3));
            irmao.filhos.inserir(0, filho.filhos.remover(2));
        }
        pai.chaves.inserir(i, meio);
        pai.filhos.inserir(i + 1, irmao);
    }

    /**
     * Remove a inscrição a partir de {@code no}, que sempre tem 2+ chaves (ou é a raiz).
     */
    private void removerDe(No234 no, int inscricao) {
        int i = no.posicao(inscricao);
        boolean achou = i < no.chaves.tamanho() && no.chaves.obter(i).getInscricao() == inscricao;

        if (achou && no.ehFolha()) {
            no.chaves.remover(i);
        } else if (achou) {
            No234 esquerdo = no.filhos.obter(i);
            No234 direito = no.filhos.obter(i + 1);
            if (esquerdo.chaves.tamanho() >= 2) {
                Barco antecessor = maior(esquerdo);
                no.chaves.definir(i, antecessor);
                removerDe(esquerdo, antecessor.getInscricao());
            } else if (direito.chaves.tamanho() >= 2) {
                Barco sucessor = menor(direito);
                no.chaves.definir(i, sucessor);
                removerDe(direito, sucessor.getInscricao());
            } else {
                fundir(no, i);
                removerDe(esquerdo, inscricao);
            }
        } else {
            i = garantirChaves(no, i);
            removerDe(no.filhos.obter(i), inscricao);
        }
    }

    /**
     * Garante que o filho {@code i} de {@code pai} tenha 2+ chaves antes de descer por ele.
     *
     * @return índice do filho por onde a descida deve continuar (muda se houver fusão à esquerda)
     */
    private int garantirChaves(No234 pai, int i) {
        No234 filho = pai.filhos.obter(i);
        if (filho.chaves.tamanho() >= 2) {
            return i;
        }
        No234 irmaoEsq = i > 0 ? pai.filhos.obter(i - 1) : null;
        No234 irmaoDir = i < pai.filhos.tamanho() - 1 ? pai.filhos.obter(i + 1) : null;

        if (irmaoEsq != null && irmaoEsq.chaves.tamanho() >= 2) {
            // rotação à direita: chave do pai desce, última chave do irmão sobe
            filho.chaves.inserir(0, pai.chaves.obter(i - 1));
            pai.chaves.definir(i - 1, irmaoEsq.chaves.remover(irmaoEsq.chaves.tamanho() - 1));
            if (!irmaoEsq.ehFolha()) {
                filho.filhos.inserir(0, irmaoEsq.filhos.remover(irmaoEsq.filhos.tamanho() - 1));
            }
            return i;
        }
        if (irmaoDir != null && irmaoDir.chaves.tamanho() >= 2) {
            // rotação à esquerda: chave do pai desce, primeira chave do irmão sobe
            filho.chaves.adicionar(pai.chaves.obter(i));
            pai.chaves.definir(i, irmaoDir.chaves.remover(0));
            if (!irmaoDir.ehFolha()) {
                filho.filhos.adicionar(irmaoDir.filhos.remover(0));
            }
            return i;
        }
        if (irmaoEsq != null) {
            fundir(pai, i - 1);
            return i - 1;
        }
        fundir(pai, i);
        return i;
    }

    /**
     * Funde os filhos {@code i} e {@code i+1} de {@code pai} com a chave {@code i} do pai,
     * resultando em um único nó na posição {@code i} (ambos tinham 1 chave, logo ficam com 3).
     */
    private void fundir(No234 pai, int i) {
        No234 esquerdo = pai.filhos.obter(i);
        No234 direito = pai.filhos.remover(i + 1);

        esquerdo.chaves.adicionar(pai.chaves.remover(i));
        while (!direito.chaves.estaVazia()) {
            esquerdo.chaves.adicionar(direito.chaves.remover(0));
        }
        while (!direito.filhos.estaVazia()) {
            esquerdo.filhos.adicionar(direito.filhos.remover(0));
        }
    }

    private Barco maior(No234 no) {
        while (!no.ehFolha()) {
            no = no.filhos.ultimo();
        }
        return no.chaves.ultimo();
    }

    private Barco menor(No234 no) {
        while (!no.ehFolha()) {
            no = no.filhos.obter(0);
        }
        return no.chaves.obter(0);
    }

    private int contarChaves(No234 no) {
        if (no == null) return 0;
        int total = no.chaves.tamanho();
        for (int i = 0; i < no.filhos.tamanho(); i++) total += contarChaves(no.filhos.obter(i));
        return total;
    }

    private int contarNos(No234 no) {
        if (no == null) return 0;
        int total = 1;
        for (int i = 0; i < no.filhos.tamanho(); i++) total += contarNos(no.filhos.obter(i));
        return total;
    }

    /**
     * Valida recursivamente o nó, com limites (min, max) exclusivos para suas chaves.
     */
    private boolean validar(No234 no, int min, int max, int nivel, int alturaTotal) {
        int n = no.chaves.tamanho();
        if (n < 1 || n > No234.MAX_CHAVES) return false;
        int anterior = min;
        for (int i = 0; i < n; i++) {
            int k = no.chaves.obter(i).getInscricao();
            if (k <= anterior || k >= max) return false;
            anterior = k;
        }
        if (no.ehFolha()) return nivel == alturaTotal;
        if (no.filhos.tamanho() != n + 1) return false;
        for (int i = 0; i <= n; i++) {
            int inf = i == 0 ? min : no.chaves.obter(i - 1).getInscricao();
            int sup = i == n ? max : no.chaves.obter(i).getInscricao();
            if (!validar(no.filhos.obter(i), inf, sup, nivel + 1, alturaTotal)) return false;
        }
        return true;
    }
}
