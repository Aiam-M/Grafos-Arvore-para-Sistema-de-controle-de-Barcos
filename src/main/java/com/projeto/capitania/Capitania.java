package com.projeto.capitania;

import com.projeto.arvore234.Arvore234;
import com.projeto.arvorerubronegra.AgendaRubroNegra;
import com.projeto.arvorerubronegra.Atracacao;
import com.projeto.kd.ArvoreKD;
import com.projeto.kd.Posicao;
import com.projeto.modelo.Barco;
import com.projeto.modelo.StatusBarco;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema da Capitania de Belém: une as três árvores, cada uma com seu papel.
 * <ul>
 *   <li><b>2-3-4</b> ({@link Arvore234}): registro de barcos pela inscrição (todos os barcos, ativos ou não);</li>
 *   <li><b>kD</b> ({@link ArvoreKD}): mapa com a posição dos barcos <i>ativos</i>;</li>
 *   <li><b>rubro-negra</b> ({@link AgendaRubroNegra}): agenda de atracações por horário.</li>
 * </ul>
 *
 * <p>Fluxos:
 * <ul>
 *   <li>cadastrar: entra no registro e no mapa;</li>
 *   <li>agendar: confere se o barco existe e está ativo e entra na agenda;</li>
 *   <li>trancar: o barco fica no registro como "em manutenção", sai da agenda e o mapa é reconstruído sem ele;</li>
 *   <li>reativar: o barco volta ao mapa e pode agendar de novo;</li>
 *   <li>dar baixa: sai das três.</li>
 * </ul>
 *
 * <p>Como a kD não tem remoção, o mapa é reconstruído com a lista de barcos ativos sempre que
 * um barco é trancado, reativado ou recebe baixa. Os mesmos objetos {@link Barco} ficam
 * compartilhados entre as três árvores, então mudar o status vale para todas.
 */
public class Capitania {

    private final Arvore234 registro = new Arvore234();
    private final ArvoreKD mapa = new ArvoreKD();
    private final AgendaRubroNegra agenda = new AgendaRubroNegra();

    // posição de todos os barcos registrados (ativos ou não); a kD só recebe os ativos
    private final List<Posicao> posicoes = new ArrayList<>();

    // ------------------------------------------------------------ OPERAÇÕES

    /**
     * Cadastra um barco no registro (2-3-4) e, se estiver ativo, no mapa (kD).
     *
     * @param barco barco a cadastrar
     * @param lat latitude da posição atual, entre -90 e 90
     * @param lon longitude da posição atual, entre -180 e 180
     * @return {@code true} se cadastrou, {@code false} se a inscrição já estava registrada
     * @throws IllegalArgumentException se o barco for nulo ou a posição for inválida
     */
    public boolean cadastrar(Barco barco, double lat, double lon) {
        if (barco == null) {
            throw new IllegalArgumentException("Barco nulo");
        }
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Posição inválida: (" + lat + ", " + lon + ")");
        }
        if (!registro.inserir(barco)) {
            return false;
        }
        Posicao posicao = new Posicao(barco, lat, lon);
        posicoes.add(posicao);
        if (barco.estaAtivo()) {
            mapa.inserir(posicao);
        }
        return true;
    }

    /**
     * Agenda a atracação de um barco em um horário.
     *
     * @param inscricao inscrição do barco
     * @param horario minutos do dia (14:30 = 870)
     * @return o resultado do agendamento
     */
    public ResultadoAgendamento agendarAtracacao(int inscricao, int horario) {
        Barco barco = registro.buscar(inscricao);
        if (barco == null) {
            return ResultadoAgendamento.BARCO_INEXISTENTE;
        }
        if (!barco.estaAtivo()) {
            return ResultadoAgendamento.EM_MANUTENCAO;
        }
        if (horario < 0 || horario >= Atracacao.MINUTOS_POR_DIA) {
            return ResultadoAgendamento.HORARIO_INVALIDO;
        }
        return agenda.agendar(new Atracacao(horario, barco))
                ? ResultadoAgendamento.AGENDADA
                : ResultadoAgendamento.HORARIO_OCUPADO;
    }

    /**
     * Cancela a atracação de um horário.
     *
     * @return {@code true} se havia atracação nesse horário
     */
    public boolean cancelarAtracacao(int horario) {
        return agenda.cancelar(horario);
    }

    /**
     * Tranca um barco para manutenção: muda o status, cancela as atracações dele e
     * reconstrói o mapa sem ele. Ele continua no registro.
     *
     * @return {@code true} se trancou, {@code false} se o barco não existe ou já estava em manutenção
     */
    public boolean trancar(int inscricao) {
        Barco barco = registro.buscar(inscricao);
        if (barco == null || !barco.estaAtivo()) {
            return false;
        }
        barco.setStatus(StatusBarco.EM_MANUTENCAO);
        agenda.cancelarDoBarco(inscricao);
        reconstruirMapa();
        return true;
    }

    /**
     * Reativa um barco em manutenção: ele volta ao mapa e pode agendar atracações de novo.
     *
     * @return {@code true} se reativou, {@code false} se o barco não existe ou já estava ativo
     */
    public boolean reativar(int inscricao) {
        Barco barco = registro.buscar(inscricao);
        if (barco == null || barco.estaAtivo()) {
            return false;
        }
        barco.setStatus(StatusBarco.ATIVO);
        reconstruirMapa();
        return true;
    }

    /**
     * Dá baixa em um barco: sai do registro, da agenda e do mapa.
     *
     * @return {@code true} se removeu, {@code false} se a inscrição não existe
     */
    public boolean darBaixa(int inscricao) {
        Barco barco = registro.buscar(inscricao);
        if (barco == null) {
            return false;
        }
        agenda.cancelarDoBarco(inscricao);
        registro.remover(inscricao);
        posicoes.removeIf(p -> p.getBarco() == barco);
        if (barco.estaAtivo()) {
            reconstruirMapa();   // só os ativos estão no mapa
        }
        return true;
    }

    /**
     * Refaz o mapa (kD) pela mediana com os barcos ativos. É chamado automaticamente ao trancar,
     * reativar e dar baixa; também pode ser usado para rebalancear depois de muitos cadastros.
     */
    public void reconstruirMapa() {
        mapa.reconstruir(posicoesAtivas());
    }

    private List<Posicao> posicoesAtivas() {
        List<Posicao> ativas = new ArrayList<>();
        for (Posicao p : posicoes) {
            if (p.getBarco().estaAtivo()) {
                ativas.add(p);
            }
        }
        return ativas;
    }

    // ------------------------------------------------------------- CONSULTAS

    /**
     * @return o barco registrado com essa inscrição, ou {@code null}
     */
    public Barco buscarBarco(int inscricao) {
        return registro.buscar(inscricao);
    }

    /**
     * @return a posição de um barco registrado (ativo ou não), ou {@code null}
     */
    public Posicao posicaoDe(int inscricao) {
        for (Posicao p : posicoes) {
            if (p.getBarco().getInscricao() == inscricao) {
                return p;
            }
        }
        return null;
    }

    /**
     * @return o barco ativo mais próximo de um ponto, ou {@code null} se não há barcos ativos
     */
    public Posicao maisProximo(double lat, double lon) {
        return mapa.maisProximo(lat, lon);
    }

    /**
     * @return os barcos ativos dentro do retângulo (as bordas contam)
     */
    public List<Posicao> barcosNaRegiao(double latMin, double latMax, double lonMin, double lonMax) {
        return mapa.naRegiao(latMin, latMax, lonMin, lonMax);
    }

    /**
     * @return a atracação marcada nesse horário, ou {@code null} se está livre
     */
    public Atracacao buscarAtracacao(int horario) {
        return agenda.buscar(horario);
    }

    /**
     * @return a próxima atracação depois do horário (estritamente maior), ou {@code null}
     */
    public Atracacao proximaAtracacaoApos(int horario) {
        return agenda.proximaApos(horario);
    }

    /**
     * @return as atracações de um barco, em ordem de horário
     */
    public List<Atracacao> atracacoesDoBarco(int inscricao) {
        return agenda.atracacoesDoBarco(inscricao);
    }

    /** @return o registro de barcos (2-3-4), por exemplo para desenhar */
    public Arvore234 registro() {
        return registro;
    }

    /** @return o mapa de barcos ativos (kD), por exemplo para desenhar */
    public ArvoreKD mapa() {
        return mapa;
    }

    /** @return a agenda de atracações (rubro-negra), por exemplo para desenhar */
    public AgendaRubroNegra agenda() {
        return agenda;
    }

    // ----------------------------------------------------------- CONFERÊNCIA

    /**
     * Confere se as três árvores estão válidas e coerentes entre si.
     *
     * @return {@code true} se está tudo consistente
     */
    public boolean consistente() {
        return verificar() == null;
    }

    /**
     * Confere as três árvores e a coerência entre elas:
     * cada barco registrado é o mesmo objeto da sua posição; o mapa tem exatamente os ativos;
     * toda atracação é de um barco registrado e ativo.
     *
     * @return {@code null} se está tudo certo, ou o texto do primeiro problema encontrado
     */
    public String verificar() {
        if (!registro.valida()) return "2-3-4 inválida";
        if (!mapa.valida()) return "kD inválida";
        if (!agenda.valida()) return "rubro-negra inválida";
        if (registro.tamanho() != posicoes.size()) return "registro e lista de posições com tamanhos diferentes";

        List<Posicao> ativas = posicoesAtivas();
        if (mapa.tamanho() != ativas.size()) return "mapa não tem exatamente os barcos ativos";
        List<Posicao> noMapa = mapa.naRegiao(-90, 90, -180, 180);
        for (Posicao p : ativas) {
            if (!noMapa.contains(p)) return "barco ativo fora do mapa: " + p;
        }

        int atracacoes = 0;
        for (Posicao p : posicoes) {
            Barco barco = p.getBarco();
            if (registro.buscar(barco.getInscricao()) != barco) return "barco fora do registro: " + barco;
            int doBarco = agenda.atracacoesDoBarco(barco.getInscricao()).size();
            if (!barco.estaAtivo() && doBarco > 0) return "barco em manutenção com atracação: " + barco;
            atracacoes += doBarco;
        }
        if (atracacoes != agenda.tamanho()) return "atracação de barco que não está no registro";
        return null;
    }
}
