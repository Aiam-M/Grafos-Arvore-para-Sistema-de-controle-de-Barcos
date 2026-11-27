package com.projeto.arvorerubronegra;

/**
 * Nó da árvore rubro-negra: guarda uma atracação, a cor e as ligações com pai e filhos.
 * Os "vazios" da árvore são representados por um único nó sentinela preto (ver AgendaRubroNegra),
 * então os filhos e o pai nunca são null.
 */
class NoRN {

    Atracacao atracacao;
    boolean vermelho;
    NoRN pai;
    NoRN esq;   // horários menores
    NoRN dir;   // horários maiores

    /** Cria o sentinela: preto, sem atracação. */
    NoRN() {
        this.vermelho = false;
    }

    /** Cria um nó novo: sempre entra vermelho, com os vazios apontando para o sentinela. */
    NoRN(Atracacao atracacao, NoRN sentinela) {
        this.atracacao = atracacao;
        this.vermelho = true;
        this.pai = sentinela;
        this.esq = sentinela;
        this.dir = sentinela;
    }

    int horario() {
        return atracacao.getHorario();
    }
}
