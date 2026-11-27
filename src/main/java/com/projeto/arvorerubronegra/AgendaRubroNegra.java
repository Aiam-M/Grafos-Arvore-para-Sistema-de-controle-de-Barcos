package com.projeto.arvorerubronegra;

import java.util.ArrayList;
import java.util.List;

/**
 * Agenda de atracações do rio, guardada em uma árvore rubro-negra ordenada pelo horário
 * (minutos do dia, ex.: 14:30 = 870).
 *
 * <p>Propriedades que o balanceamento mantém:
 * <ol>
 *   <li>todo nó é vermelho ou preto;</li>
 *   <li>a raiz é preta;</li>
 *   <li>um nó vermelho nunca tem filho vermelho;</li>
 *   <li>todo caminho da raiz até um vazio passa pela mesma quantidade de nós pretos.</li>
 * </ol>
 * Por isso o caminho mais longo tem no máximo o dobro do mais curto, e agendar, cancelar e buscar
 * custam O(log n) mesmo quando os horários entram em ordem crescente.
 *
 * <p>Repetidos: dois barcos no mesmo horário são um conflito de atracação, então
 * {@link #agendar(Atracacao)} rejeita o horário repetido e a agenda não muda.
 *
 * <p>Balanceamento:
 * <ul>
 *   <li>Inserção: o nó novo entra vermelho. Se o tio é vermelho, só recolore; se o tio é preto, rotaciona.</li>
 *   <li>Remoção: se o nó que sai era preto, sobra um "duplo preto" que é corrigido olhando o irmão (4 casos).</li>
 * </ul>
 */
public class AgendaRubroNegra {

    // nó sentinela: é o "vazio" de toda a árvore, sempre preto
    private final NoRN vazio = new NoRN();
    private NoRN raiz = vazio;
    private int quantidade;

    // contadores para a apresentação: quantas vezes cada caso do balanceamento aconteceu
    private int insercaoRecolorir;
    private int insercaoRotacaoSimples;
    private int insercaoRotacaoDupla;
    private final int[] remocaoCasos = new int[4];

    public AgendaRubroNegra() {
        vazio.pai = vazio;
        vazio.esq = vazio;
        vazio.dir = vazio;
    }

    // ---------------------------------------------------------------- BUSCA

    /**
     * Busca a atracação marcada em um horário.
     *
     * @param horario minutos do dia
     * @return a atracação, ou {@code null} se o horário está livre
     */
    public Atracacao buscar(int horario) {
        NoRN no = localizar(horario);
        return no == vazio ? null : no.atracacao;
    }

    /**
     * Acha a próxima atracação depois de um horário (estritamente maior que ele).
     * Desce uma vez só: quando o nó serve, guarda como candidato e tenta achar um menor à esquerda.
     *
     * @param horario minutos do dia
     * @return a próxima atracação, ou {@code null} se não há nenhuma depois
     */
    public Atracacao proximaApos(int horario) {
        NoRN atual = raiz;
        Atracacao candidata = null;
        while (atual != vazio) {
            if (atual.horario() > horario) {
                candidata = atual.atracacao;
                atual = atual.esq;
            } else {
                atual = atual.dir;
            }
        }
        return candidata;
    }

    private NoRN localizar(int horario) {
        NoRN atual = raiz;
        while (atual != vazio) {
            if (horario == atual.horario()) {
                return atual;
            }
            atual = horario < atual.horario() ? atual.esq : atual.dir;
        }
        return vazio;
    }

    // ------------------------------------------------------------- INSERÇÃO

    /**
     * Agenda uma atracação.
     *
     * @param nova atracação a agendar
     * @return {@code true} se agendou, {@code false} se o horário já estava ocupado (conflito)
     */
    public boolean agendar(Atracacao nova) {
        int h = nova.getHorario();

        NoRN pai = vazio;
        NoRN atual = raiz;
        while (atual != vazio) {
            pai = atual;
            if (h == atual.horario()) {
                return false;
            }
            atual = h < atual.horario() ? atual.esq : atual.dir;
        }

        NoRN no = new NoRN(nova, vazio);
        no.pai = pai;
        if (pai == vazio) {
            raiz = no;
        } else if (h < pai.horario()) {
            pai.esq = no;
        } else {
            pai.dir = no;
        }
        quantidade++;
        corrigirInsercao(no);
        return true;
    }

    /**
     * Conserta violações da regra "vermelho não tem filho vermelho" subindo a partir do nó novo.
     */
    private void corrigirInsercao(NoRN no) {
        while (no.pai.vermelho) {
            NoRN pai = no.pai;
            NoRN avo = pai.pai;
            boolean paiEhEsquerdo = pai == avo.esq;
            NoRN tio = paiEhEsquerdo ? avo.dir : avo.esq;

            if (tio.vermelho) {
                // tio vermelho: recolore e sobe o problema para o avô
                pai.vermelho = false;
                tio.vermelho = false;
                avo.vermelho = true;
                no = avo;
                insercaoRecolorir++;
            } else {
                // tio preto: rotaciona (em dois passos se o nó está "dobrado" em relação ao avô)
                if (paiEhEsquerdo && no == pai.dir) {
                    no = pai;
                    rotacionarEsquerda(no);
                    insercaoRotacaoDupla++;
                } else if (!paiEhEsquerdo && no == pai.esq) {
                    no = pai;
                    rotacionarDireita(no);
                    insercaoRotacaoDupla++;
                } else {
                    insercaoRotacaoSimples++;
                }
                no.pai.vermelho = false;
                no.pai.pai.vermelho = true;
                if (paiEhEsquerdo) {
                    rotacionarDireita(no.pai.pai);
                } else {
                    rotacionarEsquerda(no.pai.pai);
                }
            }
        }
        raiz.vermelho = false;
    }

    // -------------------------------------------------------------- REMOÇÃO

    /**
     * Cancela a atracação de um horário.
     *
     * @param horario minutos do dia
     * @return {@code true} se cancelou, {@code false} se não havia atracação nesse horário
     */
    public boolean cancelar(int horario) {
        NoRN alvo = localizar(horario);
        if (alvo == vazio) {
            return false;
        }

        NoRN removido = alvo;                    // nó que de fato sai do seu lugar
        boolean corRemovida = removido.vermelho;
        NoRN substituto;                         // quem ocupa o lugar que ficou livre

        if (alvo.esq == vazio) {
            substituto = alvo.dir;
            trocar(alvo, alvo.dir);
        } else if (alvo.dir == vazio) {
            substituto = alvo.esq;
            trocar(alvo, alvo.esq);
        } else {
            // dois filhos: o sucessor (menor da direita) assume o lugar do alvo, com a cor do alvo
            removido = menor(alvo.dir);
            corRemovida = removido.vermelho;
            substituto = removido.dir;
            if (removido.pai == alvo) {
                substituto.pai = removido;
            } else {
                trocar(removido, removido.dir);
                removido.dir = alvo.dir;
                removido.dir.pai = removido;
            }
            trocar(alvo, removido);
            removido.esq = alvo.esq;
            removido.esq.pai = removido;
            removido.vermelho = alvo.vermelho;
        }
        quantidade--;

        if (!corRemovida) {
            // saiu um nó preto: o caminho do substituto perdeu um preto ("duplo preto")
            corrigirRemocao(substituto);
        }
        return true;
    }

    /**
     * Resolve o duplo preto em {@code no}, olhando o irmão. São 4 casos:
     * <ol>
     *   <li>irmão vermelho: rotaciona para transformar em um dos casos seguintes;</li>
     *   <li>irmão preto com os dois sobrinhos pretos: pinta o irmão de vermelho e sobe o problema;</li>
     *   <li>irmão preto, sobrinho de perto vermelho e o de longe preto: rotaciona o irmão e cai no caso 4;</li>
     *   <li>irmão preto com o sobrinho de longe vermelho: rotaciona o pai e acaba.</li>
     * </ol>
     */
    private void corrigirRemocao(NoRN no) {
        while (no != raiz && !no.vermelho) {
            boolean ehEsquerdo = no == no.pai.esq;
            NoRN irmao = ehEsquerdo ? no.pai.dir : no.pai.esq;

            if (irmao.vermelho) {
                // caso 1
                irmao.vermelho = false;
                no.pai.vermelho = true;
                if (ehEsquerdo) {
                    rotacionarEsquerda(no.pai);
                    irmao = no.pai.dir;
                } else {
                    rotacionarDireita(no.pai);
                    irmao = no.pai.esq;
                }
                remocaoCasos[0]++;
            }

            NoRN sobrinhoPerto = ehEsquerdo ? irmao.esq : irmao.dir;
            NoRN sobrinhoLonge = ehEsquerdo ? irmao.dir : irmao.esq;

            if (!sobrinhoPerto.vermelho && !sobrinhoLonge.vermelho) {
                // caso 2
                irmao.vermelho = true;
                no = no.pai;
                remocaoCasos[1]++;
            } else {
                if (!sobrinhoLonge.vermelho) {
                    // caso 3
                    sobrinhoPerto.vermelho = false;
                    irmao.vermelho = true;
                    if (ehEsquerdo) {
                        rotacionarDireita(irmao);
                        irmao = no.pai.dir;
                    } else {
                        rotacionarEsquerda(irmao);
                        irmao = no.pai.esq;
                    }
                    remocaoCasos[2]++;
                }
                // caso 4
                irmao.vermelho = no.pai.vermelho;
                no.pai.vermelho = false;
                (ehEsquerdo ? irmao.dir : irmao.esq).vermelho = false;
                if (ehEsquerdo) {
                    rotacionarEsquerda(no.pai);
                } else {
                    rotacionarDireita(no.pai);
                }
                remocaoCasos[3]++;
                no = raiz;
            }
        }
        no.vermelho = false;
    }

    /** Coloca a subárvore {@code nova} no lugar da subárvore {@code antiga}, ajustando só o pai. */
    private void trocar(NoRN antiga, NoRN nova) {
        if (antiga.pai == vazio) {
            raiz = nova;
        } else if (antiga == antiga.pai.esq) {
            antiga.pai.esq = nova;
        } else {
            antiga.pai.dir = nova;
        }
        nova.pai = antiga.pai;
    }

    private NoRN menor(NoRN no) {
        while (no.esq != vazio) {
            no = no.esq;
        }
        return no;
    }

    // ------------------------------------------------------------- ROTAÇÕES

    private void rotacionarEsquerda(NoRN x) {
        NoRN y = x.dir;
        x.dir = y.esq;
        if (y.esq != vazio) {
            y.esq.pai = x;
        }
        y.pai = x.pai;
        if (x.pai == vazio) {
            raiz = y;
        } else if (x == x.pai.esq) {
            x.pai.esq = y;
        } else {
            x.pai.dir = y;
        }
        y.esq = x;
        x.pai = y;
    }

    private void rotacionarDireita(NoRN x) {
        NoRN y = x.esq;
        x.esq = y.dir;
        if (y.dir != vazio) {
            y.dir.pai = x;
        }
        y.pai = x.pai;
        if (x.pai == vazio) {
            raiz = y;
        } else if (x == x.pai.dir) {
            x.pai.dir = y;
        } else {
            x.pai.esq = y;
        }
        y.dir = x;
        x.pai = y;
    }

    // ------------------------------------------------- CONSULTAS PARA O SISTEMA

    /**
     * Lista as atracações de um barco, em ordem de horário (usado ao trancar ou dar baixa no barco).
     */
    public List<Atracacao> atracacoesDoBarco(int inscricao) {
        List<Atracacao> achadas = new ArrayList<>();
        coletarDoBarco(raiz, inscricao, achadas);
        return achadas;
    }

    private void coletarDoBarco(NoRN no, int inscricao, List<Atracacao> achadas) {
        if (no == vazio) {
            return;
        }
        coletarDoBarco(no.esq, inscricao, achadas);
        if (no.atracacao.getBarco().getInscricao() == inscricao) {
            achadas.add(no.atracacao);
        }
        coletarDoBarco(no.dir, inscricao, achadas);
    }

    /**
     * Cancela todas as atracações de um barco.
     *
     * @return quantas atracações foram canceladas
     */
    public int cancelarDoBarco(int inscricao) {
        List<Atracacao> doBarco = atracacoesDoBarco(inscricao);
        for (Atracacao a : doBarco) {
            cancelar(a.getHorario());
        }
        return doBarco.size();
    }

    // ------------------------------------------------------------ UTILITÁRIOS

    public boolean estaVazia() {
        return raiz == vazio;
    }

    /**
     * @return quantidade de atracações agendadas
     */
    public int tamanho() {
        return quantidade;
    }

    /**
     * @return altura da árvore (0 para vazia, 1 para só a raiz)
     */
    public int altura() {
        return altura(raiz);
    }

    private int altura(NoRN no) {
        if (no == vazio) {
            return 0;
        }
        return 1 + Math.max(altura(no.esq), altura(no.dir));
    }

    /**
     * @return quantidade de nós pretos em qualquer caminho da raiz até um vazio (-1 se a árvore é inválida)
     */
    public int alturaNegra() {
        int h = conferir(raiz, -1, Atracacao.MINUTOS_POR_DIA);
        return h < 0 ? -1 : h - 1;   // o -1 desconta o vazio, que a conferência conta como preto
    }

    /**
     * Quantas vezes cada correção aconteceu desde a criação da agenda:
     * {recolorir, rotação simples, rotação dupla}.
     */
    public int[] contagemInsercao() {
        return new int[]{insercaoRecolorir, insercaoRotacaoSimples, insercaoRotacaoDupla};
    }

    /**
     * Quantas vezes cada caso do duplo preto aconteceu: posições 0 a 3 são os casos 1 a 4.
     */
    public int[] contagemRemocao() {
        return remocaoCasos.clone();
    }

    /**
     * Confere as 4 propriedades rubro-negras e a ordem dos horários.
     *
     * @return {@code true} se a árvore está válida
     */
    public boolean valida() {
        if (raiz == vazio) {
            return true;
        }
        if (raiz.vermelho) {
            return false;
        }
        return conferir(raiz, -1, Atracacao.MINUTOS_POR_DIA) >= 0;
    }

    /**
     * Confere o nó e devolve sua altura negra, ou -1 se achar algum erro.
     * Os limites (min, max) são exclusivos e garantem a ordem de busca.
     */
    private int conferir(NoRN no, int min, int max) {
        if (no == vazio) {
            return 1;   // o vazio é preto
        }
        int h = no.horario();
        if (h <= min || h >= max) {
            return -1;
        }
        if (no.vermelho && (no.esq.vermelho || no.dir.vermelho)) {
            return -1;
        }
        if (no.esq != vazio && no.esq.pai != no || no.dir != vazio && no.dir.pai != no) {
            return -1;
        }
        int esq = conferir(no.esq, min, h);
        int dir = conferir(no.dir, h, max);
        if (esq < 0 || dir < 0 || esq != dir) {
            return -1;
        }
        return esq + (no.vermelho ? 0 : 1);
    }

    // --------------------------------------------------------------- DESENHO

    /**
     * Gera o código DOT da árvore para desenhar com o Graphviz (nós vermelhos e pretos de verdade).
     */
    public String paraDot(String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph AgendaRubroNegra {\n");
        sb.append("  label=\"").append(titulo).append("\"; labelloc=t; fontsize=18;\n");
        sb.append("  node [shape=circle, style=filled, fontcolor=white, fontname=Helvetica];\n");
        if (raiz != vazio) {
            escreverNo(sb, raiz, new int[]{0});
        }
        sb.append("}\n");
        return sb.toString();
    }

    private int escreverNo(StringBuilder sb, NoRN no, int[] contador) {
        int id = contador[0]++;
        sb.append("  n").append(id).append(" [label=\"")
                .append(Atracacao.formatar(no.horario()))
                .append("\\n").append(no.atracacao.getBarco().getNome())
                .append("\", fillcolor=").append(no.vermelho ? "red" : "black")
                .append("];\n");
        if (no.esq != vazio) {
            int filho = escreverNo(sb, no.esq, contador);
            sb.append("  n").append(id).append(" -> n").append(filho).append(";\n");
        }
        if (no.dir != vazio) {
            int filho = escreverNo(sb, no.dir, contador);
            sb.append("  n").append(id).append(" -> n").append(filho).append(";\n");
        }
        return id;
    }
}
