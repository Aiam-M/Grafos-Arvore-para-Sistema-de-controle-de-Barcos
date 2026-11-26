package com.projeto.kd;

/**
 * Nó da árvore kD: guarda uma posição e os dois lados do mapa.
 */
class NoKD {

    final Posicao posicao;
    NoKD esq;   // valores menores no eixo do nível
    NoKD dir;   // valores iguais ou maiores no eixo do nível

    NoKD(Posicao posicao) {
        this.posicao = posicao;
    }
}