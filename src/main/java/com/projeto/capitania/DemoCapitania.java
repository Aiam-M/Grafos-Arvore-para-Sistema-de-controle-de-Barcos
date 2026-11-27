package com.projeto.capitania;

import com.projeto.arvorerubronegra.Atracacao;
import com.projeto.kd.Posicao;
import com.projeto.modelo.Barco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Demonstração do sistema completo: cadastra barcos (registro 2-3-4 + mapa kD), agenda atracações
 * (agenda rubro-negra), tranca e reativa um barco, dá baixa em outro e, a cada passo, confere se as
 * três árvores continuam coerentes. Termina com um teste aleatório. Gera arquivos .dot em {@code saida/}.
 */
public class DemoCapitania {

    // latitude e longitude de 10 barcos perto de Belém
    private static final double[][] COORDENADAS = {
            {-1.4528, -48.5037}, {-1.4551, -48.5002}, {-1.4610, -48.4890}, {-1.4702, -48.4821},
            {-1.4390, -48.5120}, {-1.4300, -48.5230}, {-1.4100, -48.4950}, {-1.3800, -48.4700},
            {-1.3500, -48.4600}, {-1.2975, -48.4747}
    };

    /**
     * Executa a demonstração.
     *
     * @param args não utilizado
     * @throws IOException se não for possível gravar os arquivos de saída
     */
    public static void main(String[] args) throws IOException {
        Capitania capitania = new Capitania();

        // 1) cadastrando: entra no registro e no mapa
        Barco[] barcos = new Barco[COORDENADAS.length];
        for (int i = 0; i < barcos.length; i++) {
            barcos[i] = new Barco("Barco " + (i + 1), i % 2 == 0 ? "Regional" : "Voadeira");
            capitania.cadastrar(barcos[i], COORDENADAS[i][0], COORDENADAS[i][1]);
        }
        System.out.println("Cadastrados " + barcos.length + " barcos. Consistente: " + capitania.consistente());
        System.out.println("Cadastrar de novo o " + barcos[0].getNome() + ": "
                + capitania.cadastrar(barcos[0], COORDENADAS[0][0], COORDENADAS[0][1]));

        // 2) agendando atracações
        System.out.println();
        agendar(capitania, barcos[0], 8, 0);
        agendar(capitania, barcos[0], 10, 30);
        agendar(capitania, barcos[1], 9, 0);
        agendar(capitania, barcos[2], 9, 30);
        agendar(capitania, barcos[2], 14, 0);
        agendar(capitania, barcos[3], 11, 0);
        agendar(capitania, barcos[4], 15, 30);
        System.out.println("Outro barco às 09:00 (ocupado): "
                + capitania.agendarAtracacao(barcos[5].getInscricao(), Atracacao.emMinutos(9, 0)));
        System.out.println("Barco que não existe (inscrição 99999): "
                + capitania.agendarAtracacao(99999, Atracacao.emMinutos(12, 0)));
        System.out.println("Horário fora do dia (1500): "
                + capitania.agendarAtracacao(barcos[5].getInscricao(), 1500));
        System.out.println("Próxima atracação depois de 09:15: "
                + capitania.proximaAtracacaoApos(Atracacao.emMinutos(9, 15)));

        // 3) consultando o mapa
        Posicao alvo = capitania.posicaoDe(barcos[2].getInscricao());
        System.out.println("\nBarco mais próximo da posição do " + barcos[2].getNome() + ": "
                + capitania.maisProximo(alvo.getLat(), alvo.getLon()));

        // 4) trancando o barco 3 (tem duas atracações)
        System.out.println("\nAtracações do " + barcos[2].getNome() + " antes de trancar: "
                + capitania.atracacoesDoBarco(barcos[2].getInscricao()));
        System.out.println("Trancar " + barcos[2].getNome() + ": " + capitania.trancar(barcos[2].getInscricao()));
        System.out.println("  status: " + capitania.buscarBarco(barcos[2].getInscricao()).getStatus()
                + " (continua no registro)");
        System.out.println("  atracações dele: " + capitania.atracacoesDoBarco(barcos[2].getInscricao()));
        System.out.println("  no mapa, o mais próximo da mesma posição agora é: "
                + capitania.maisProximo(alvo.getLat(), alvo.getLon()));
        System.out.println("  tentar agendar: "
                + capitania.agendarAtracacao(barcos[2].getInscricao(), Atracacao.emMinutos(16, 0)));
        System.out.println("  trancar de novo: " + capitania.trancar(barcos[2].getInscricao()));
        System.out.println("  consistente: " + capitania.consistente());

        // 5) reativando
        System.out.println("\nReativar " + barcos[2].getNome() + ": " + capitania.reativar(barcos[2].getInscricao()));
        System.out.println("  no mapa, o mais próximo da posição dele volta a ser: "
                + capitania.maisProximo(alvo.getLat(), alvo.getLon()));
        System.out.println("  agendar às 16:00: "
                + capitania.agendarAtracacao(barcos[2].getInscricao(), Atracacao.emMinutos(16, 0)));
        System.out.println("  consistente: " + capitania.consistente());

        // 6) dando baixa em um barco com atracações: sai das três
        int inscricaoBaixa = barcos[0].getInscricao();
        System.out.println("\nAtracações do " + barcos[0].getNome() + " antes da baixa: "
                + capitania.atracacoesDoBarco(inscricaoBaixa).size());
        System.out.println("Dar baixa no " + barcos[0].getNome() + ": " + capitania.darBaixa(inscricaoBaixa));
        System.out.println("  no registro: " + capitania.buscarBarco(inscricaoBaixa));
        System.out.println("  na agenda (08:00): " + capitania.buscarAtracacao(Atracacao.emMinutos(8, 0)));
        System.out.println("  no mapa, o mais próximo da posição dele agora é: "
                + capitania.maisProximo(COORDENADAS[0][0], COORDENADAS[0][1]));
        System.out.println("  dar baixa de novo: " + capitania.darBaixa(inscricaoBaixa));
        System.out.println("  consistente: " + capitania.consistente());

        System.out.printf("%nResumo: barcos no registro=%d, ativos no mapa=%d, atracações=%d%n",
                capitania.registro().tamanho(), capitania.mapa().tamanho(), capitania.agenda().tamanho());

        // 7) desenhos das três árvores no estado final
        Path saida = Path.of("saida");
        Files.createDirectories(saida);
        exportar(saida, "cap_registro", capitania.registro().paraDot("Registro de barcos (2-3-4)"));
        exportar(saida, "cap_mapa", capitania.mapa().paraDot("Mapa de barcos ativos (kD)"));
        exportar(saida, "cap_agenda", capitania.agenda().paraDot("Agenda de atracações (rubro-negra)"));

        testeAleatorio();
    }

    private static void agendar(Capitania capitania, Barco barco, int hora, int minuto) {
        ResultadoAgendamento r = capitania.agendarAtracacao(barco.getInscricao(), Atracacao.emMinutos(hora, minuto));
        System.out.println("Agendar " + barco.getNome() + " às " + Atracacao.formatar(Atracacao.emMinutos(hora, minuto))
                + ": " + r);
    }

    /**
     * Mistura cadastros, agendamentos, cancelamentos, trancamentos, reativações e baixas aleatórios.
     * Cada resultado é comparado com o esperado por um modelo simples (vetores), e depois de cada
     * operação confere se as três árvores continuam válidas e coerentes entre si.
     */
    private static void testeAleatorio() {
        final int maxBarcos = 60;
        Random sorteio = new Random(7);
        Capitania capitania = new Capitania();

        Barco[] criados = new Barco[maxBarcos];
        boolean[] existe = new boolean[maxBarcos];
        boolean[] manutencao = new boolean[maxBarcos];
        int[] ocupadoPor = new int[Atracacao.MINUTOS_POR_DIA];   // índice do barco + 1, ou 0 se livre
        int total = 0;
        int erros = 0;
        String primeiroErro = null;

        for (int passo = 0; passo < 2000; passo++) {
            int operacao = sorteio.nextInt(8);
            if (operacao <= 1 && total >= maxBarcos) {
                operacao += 2;
            }
            if (operacao == 7 && sorteio.nextInt(6) != 0) {
                operacao = 2;   // baixa é rara; senão os barcos acabam e o teste perde força
            }
            if (operacao > 1 && total == 0) {
                continue;
            }
            boolean ok = true;

            if (operacao <= 1) {
                Barco novo = new Barco("Teste " + total, "Regional");
                criados[total] = novo;
                existe[total] = true;
                ok = capitania.cadastrar(novo, -1.6 + sorteio.nextDouble() * 0.5, -48.6 + sorteio.nextDouble() * 0.2);
                total++;
            } else {
                int i = sorteio.nextInt(total);
                int inscricao = criados[i].getInscricao();
                int horario = sorteio.nextInt(Atracacao.MINUTOS_POR_DIA);

                if (operacao <= 3) {
                    ResultadoAgendamento esperado = !existe[i] ? ResultadoAgendamento.BARCO_INEXISTENTE
                            : manutencao[i] ? ResultadoAgendamento.EM_MANUTENCAO
                            : ocupadoPor[horario] != 0 ? ResultadoAgendamento.HORARIO_OCUPADO
                            : ResultadoAgendamento.AGENDADA;
                    ResultadoAgendamento obtido = capitania.agendarAtracacao(inscricao, horario);
                    ok = obtido == esperado;
                    if (obtido == ResultadoAgendamento.AGENDADA) {
                        ocupadoPor[horario] = i + 1;
                    }
                } else if (operacao == 4) {
                    ok = capitania.cancelarAtracacao(horario) == (ocupadoPor[horario] != 0);
                    ocupadoPor[horario] = 0;
                } else if (operacao == 5) {
                    boolean esperado = existe[i] && !manutencao[i];
                    ok = capitania.trancar(inscricao) == esperado;
                    if (esperado) {
                        manutencao[i] = true;
                        liberar(ocupadoPor, i + 1);
                    }
                } else if (operacao == 6) {
                    boolean esperado = existe[i] && manutencao[i];
                    ok = capitania.reativar(inscricao) == esperado;
                    if (esperado) {
                        manutencao[i] = false;
                    }
                } else {
                    boolean esperado = existe[i];
                    ok = capitania.darBaixa(inscricao) == esperado;
                    if (esperado) {
                        existe[i] = false;
                        manutencao[i] = false;
                        liberar(ocupadoPor, i + 1);
                    }
                }
            }

            String problema = capitania.verificar();
            if (!ok || problema != null) {
                erros++;
                if (primeiroErro == null) {
                    primeiroErro = "passo " + passo + ", operação " + operacao
                            + (problema != null ? ": " + problema : ": resultado diferente do esperado");
                }
            }
        }

        int vivos = 0;
        int ativos = 0;
        for (int i = 0; i < total; i++) {
            if (existe[i]) vivos++;
            if (existe[i] && !manutencao[i]) ativos++;
        }
        int agendadas = 0;
        for (int v : ocupadoPor) {
            if (v != 0) agendadas++;
        }
        if (vivos != capitania.registro().tamanho() || ativos != capitania.mapa().tamanho()
                || agendadas != capitania.agenda().tamanho()) {
            erros++;
            primeiroErro = "contagens finais diferentes do modelo";
        }

        System.out.println("\n2000 operações aleatórias: erros = " + erros
                + (primeiroErro != null ? " (primeiro: " + primeiroErro + ")" : ""));
        System.out.println("Estado final: barcos=" + vivos + ", ativos=" + ativos + ", atracações=" + agendadas);
    }

    private static void liberar(int[] ocupadoPor, int dono) {
        for (int m = 0; m < ocupadoPor.length; m++) {
            if (ocupadoPor[m] == dono) {
                ocupadoPor[m] = 0;
            }
        }
    }

    /**
     * Grava o .dot e tenta gerar o .png com o comando {@code dot}, se existir no sistema.
     */
    private static void exportar(Path pasta, String nome, String codigoDot) throws IOException {
        Path dot = pasta.resolve(nome + ".dot");
        Files.writeString(dot, codigoDot);
        try {
            Process p = new ProcessBuilder("dot", "-Tpng", dot.toString(), "-o",
                    pasta.resolve(nome + ".png").toString()).inheritIO().start();
            p.waitFor();
        } catch (IOException e) {
            System.out.println("Graphviz não encontrado; apenas " + dot + " foi gerado.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
