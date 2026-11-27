package com.projeto.arvorerubronegra;

import com.projeto.modelo.Barco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

/**
 * Demonstração da agenda rubro-negra: agenda 24 atracações em horários crescentes (pior caso para
 * árvores sem balanceamento), rejeita horário repetido, consulta, cancela e confere tudo com um
 * vetor simples (um espaço por minuto do dia). Gera arquivos .dot na pasta {@code saida/}.
 */
public class DemoRubroNegra {

    private static final int TOTAL_ATRACACOES = 24;
    private static final int TOTAL_BARCOS = 8;

    /**
     * Executa a demonstração.
     *
     * @param args não utilizado
     * @throws IOException se não for possível gravar os arquivos de saída
     */
    public static void main(String[] args) throws IOException {
        Path saida = Path.of("saida");
        Files.createDirectories(saida);

        Barco[] barcos = new Barco[TOTAL_BARCOS];
        for (int i = 0; i < TOTAL_BARCOS; i++) {
            barcos[i] = new Barco("Barco " + (i + 1), i % 2 == 0 ? "Regional" : "Voadeira");
        }

        // 1) agendando de 06:00 a 17:30, de meia em meia hora, sempre em ordem crescente
        AgendaRubroNegra agenda = new AgendaRubroNegra();
        for (int i = 0; i < TOTAL_ATRACACOES; i++) {
            int horario = Atracacao.emMinutos(6, 0) + 30 * i;
            agenda.agendar(new Atracacao(horario, barcos[i % TOTAL_BARCOS]));
            if (i + 1 == 10 || i + 1 == TOTAL_ATRACACOES) {
                exportar(agenda, saida, "rn_insercao_" + (i + 1), "Após agendar " + (i + 1) + " atracações");
            }
        }
        resumo("Após agendar", agenda);
        int[] ins = agenda.contagemInsercao();
        System.out.println("Correções na inserção: recolorir=" + ins[0] + ", rotação simples=" + ins[1]
                + ", rotação dupla=" + ins[2]);

        // 2) horário repetido é conflito: rejeitado
        boolean aceitou = agenda.agendar(new Atracacao(Atracacao.emMinutos(8, 0), barcos[5]));
        System.out.println("\nAgendar outro barco às 08:00 (já ocupado) foi aceito? " + aceitou);
        System.out.println("Quem está às 08:00: " + agenda.buscar(Atracacao.emMinutos(8, 0)));

        // 3) consultas
        System.out.println("\nBuscar 09:30: " + agenda.buscar(Atracacao.emMinutos(9, 30)));
        System.out.println("Buscar 09:45 (livre): " + agenda.buscar(Atracacao.emMinutos(9, 45)));
        System.out.println("Próxima depois de 09:45: " + agenda.proximaApos(Atracacao.emMinutos(9, 45)));
        System.out.println("Próxima depois de 09:30: " + agenda.proximaApos(Atracacao.emMinutos(9, 30)));
        System.out.println("Próxima depois de 00:00: " + agenda.proximaApos(0));
        System.out.println("Próxima depois de 17:30 (última): " + agenda.proximaApos(Atracacao.emMinutos(17, 30)));

        // 4) cancelamentos: folhas, nós do meio e a raiz da primeira montagem
        int[] cancelar = {
                Atracacao.emMinutos(6, 0), Atracacao.emMinutos(6, 30), Atracacao.emMinutos(7, 0),
                Atracacao.emMinutos(7, 30), Atracacao.emMinutos(12, 0), Atracacao.emMinutos(14, 0),
                Atracacao.emMinutos(17, 30), Atracacao.emMinutos(11, 30)
        };
        System.out.println();
        for (int horario : cancelar) {
            boolean ok = agenda.cancelar(horario);
            System.out.println("Cancelar " + Atracacao.formatar(horario) + ": " + ok
                    + " | atracações=" + agenda.tamanho() + ", válida=" + agenda.valida());
        }
        System.out.println("Cancelar 03:00 (não existe): " + agenda.cancelar(Atracacao.emMinutos(3, 0)));
        resumo("Após cancelar", agenda);
        exportar(agenda, saida, "rn_cancelamento", "Após cancelar " + cancelar.length + " atracações");

        // 5) tirar um barco da agenda (o que acontece ao trancar ou dar baixa nele)
        Barco trancado = barcos[2];
        List<Atracacao> doBarco = agenda.atracacoesDoBarco(trancado.getInscricao());
        System.out.println("\nAtracações do " + trancado.getNome() + " antes de trancar: " + doBarco);
        int canceladas = agenda.cancelarDoBarco(trancado.getInscricao());
        System.out.println("Canceladas: " + canceladas + " | restantes do barco: "
                + agenda.atracacoesDoBarco(trancado.getInscricao()).size());
        resumo("Sem o barco trancado", agenda);

        // 6) teste com muitas operações aleatórias, comparando com um vetor de 1440 posições
        testeAleatorio();
    }

    /**
     * Mistura agendamentos, cancelamentos e consultas aleatórios. A cada passo confere o resultado
     * com um vetor onde a posição m guarda o barco do minuto m, e confere as propriedades da árvore.
     */
    private static void testeAleatorio() {
        Random sorteio = new Random(1);
        AgendaRubroNegra agenda = new AgendaRubroNegra();
        Barco barco = new Barco("Barco do teste", "Regional");
        Barco[] ocupado = new Barco[Atracacao.MINUTOS_POR_DIA];
        int erros = 0;

        for (int passo = 0; passo < 20000; passo++) {
            int horario = sorteio.nextInt(Atracacao.MINUTOS_POR_DIA);
            int operacao = sorteio.nextInt(3);

            if (operacao == 0) {
                boolean esperado = ocupado[horario] == null;
                if (agenda.agendar(new Atracacao(horario, barco)) != esperado) erros++;
                ocupado[horario] = barco;
            } else if (operacao == 1) {
                boolean esperado = ocupado[horario] != null;
                if (agenda.cancelar(horario) != esperado) erros++;
                ocupado[horario] = null;
            } else {
                int proximo = -1;
                for (int m = horario + 1; m < Atracacao.MINUTOS_POR_DIA && proximo < 0; m++) {
                    if (ocupado[m] != null) proximo = m;
                }
                Atracacao obtida = agenda.proximaApos(horario);
                if ((obtida == null ? -1 : obtida.getHorario()) != proximo) erros++;
                if ((agenda.buscar(horario) != null) != (ocupado[horario] != null)) erros++;
            }
            if (!agenda.valida()) erros++;
        }

        int ocupados = 0;
        for (Barco b : ocupado) {
            if (b != null) ocupados++;
        }
        if (ocupados != agenda.tamanho()) erros++;

        int[] rem = agenda.contagemRemocao();
        System.out.println("\n20000 operações aleatórias: erros = " + erros + " | atracações no fim=" + ocupados);
        System.out.println("Casos do duplo preto na remoção: caso 1=" + rem[0] + ", caso 2=" + rem[1]
                + ", caso 3=" + rem[2] + ", caso 4=" + rem[3]);
    }

    private static void resumo(String titulo, AgendaRubroNegra agenda) {
        System.out.printf("%s: atracações=%d, altura=%d, altura negra=%d, válida=%b%n",
                titulo, agenda.tamanho(), agenda.altura(), agenda.alturaNegra(), agenda.valida());
    }

    /**
     * Grava o .dot e tenta gerar o .png com o comando {@code dot}, se existir no sistema.
     */
    private static void exportar(AgendaRubroNegra agenda, Path pasta, String nome, String titulo) throws IOException {
        Path dot = pasta.resolve(nome + ".dot");
        Files.writeString(dot, agenda.paraDot(titulo));
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
