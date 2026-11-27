package com.projeto.arvore234;

import com.projeto.modelo.Barco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstração da árvore 2-3-4: cadastra barcos em ordem crescente de inscrição
 * (pior caso para árvores desbalanceadas), consulta, rejeita repetidos e dá baixa.
 * Gera arquivos .dot na pasta {@code saida/}.
 */
public class Demo234 {

    private static final int TOTAL_BARCOS = 40;

    /**
     * Executa a demonstração.
     *
     * @param args não utilizado
     * @throws IOException se não for possível gravar os arquivos de saída
     */
    public static void main(String[] args) throws IOException {
        Arvore234 arvore = new Arvore234();
        Path saida = Path.of("saida");
        Files.createDirectories(saida);

        for (int i = 1; i <= TOTAL_BARCOS; i++) {
            Barco b = new Barco("Barco " + i, i % 2 == 0 ? "Voadeira" : "Regional");
            arvore.inserir(b);
            if (i == 10 || i == 20 || i == TOTAL_BARCOS) {
                exportar(arvore, saida, "insercao_" + i, "Após cadastrar " + i + " barcos");
            }
        }
        resumo("Após inserções", arvore);

        System.out.println("Busca 17: " + arvore.buscar(17));
        System.out.println("Busca 999: " + arvore.buscar(999));
        System.out.println("Reinserir o barco 17 foi rejeitado? " + !arvore.inserir(arvore.buscar(17)));

        int[] baixas = {1, 2, 3, 4, 5, 6, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        for (int inscricao : baixas) {
            if (!arvore.remover(inscricao)) {
                System.out.println("Falha ao remover " + inscricao);
            }
        }
        System.out.println("Remover inexistente 999: " + arvore.remover(999));
        resumo("Após remoções", arvore);
        exportar(arvore, saida, "remocao", "Após dar baixa em " + baixas.length + " barcos");
    }

    private static void resumo(String titulo, Arvore234 arvore) {
        System.out.printf("%s: barcos=%d, nós=%d, altura=%d, válida=%b%n",
                titulo, arvore.tamanho(), arvore.contarNos(), arvore.altura(), arvore.valida());
    }

    /**
     * Grava o .dot e tenta gerar o .png com o comando {@code dot}, se existir no sistema.
     */
    private static void exportar(Arvore234 arvore, Path pasta, String nome, String titulo) throws IOException {
        Path dot = pasta.resolve(nome + ".dot");
        Files.writeString(dot, arvore.paraDot(titulo));
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
