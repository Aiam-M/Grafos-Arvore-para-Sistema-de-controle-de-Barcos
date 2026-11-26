package com.projeto.kd;

import com.projeto.modelo.Barco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demonstração da kD: cadastra barcos no mapa, procura o mais próximo, lista uma região,
 * "tranca" um barco (reconstruindo a árvore) e confere tudo com a força bruta.
 */
public class DemoKD {

    // latitude e longitude de 21 barcos perto de Belém
    private static final double[][] COORDENADAS = {
            {-1.4528, -48.5037}, {-1.4551, -48.5002}, {-1.4610, -48.4890}, {-1.4702, -48.4821},
            {-1.4390, -48.5120}, {-1.4300, -48.5230}, {-1.4100, -48.4950}, {-1.3800, -48.4700},
            {-1.3500, -48.4600}, {-1.2975, -48.4747}, {-1.3010, -48.4390}, {-1.3200, -48.4300},
            {-1.5100, -48.4600}, {-1.5250, -48.4450}, {-1.4900, -48.4400}, {-1.4700, -48.5300},
            {-1.4900, -48.5500}, {-1.5400, -48.5100}, {-1.1500, -48.4800}, {-1.2200, -48.5000},
            {-1.4528, -48.5037}   // mesmo ponto do primeiro barco (testa o empate)
    };

    public static void main(String[] args) throws IOException {
        List<Posicao> todos = new ArrayList<>();
        for (int i = 0; i < COORDENADAS.length; i++) {
            Barco b = new Barco("Barco " + (i + 1), i % 2 == 0 ? "Regional" : "Voadeira");
            todos.add(new Posicao(b, COORDENADAS[i][0], COORDENADAS[i][1]));
        }

        // 1) montando por inserção, na ordem em que os barcos foram cadastrados
        ArvoreKD porInsercao = new ArvoreKD();
        for (Posicao p : todos) {
            porInsercao.inserir(p);
        }
        resumo("Por inserção", porInsercao);

        // 2) montando por reconstrução (mediana)
        ArvoreKD kd = new ArvoreKD();
        kd.reconstruir(todos);
        resumo("Por reconstrução", kd);

        // 3) barco mais próximo de um ponto
        System.out.println();
        double latAlvo = -1.4560, lonAlvo = -48.4950;
        Posicao achado = kd.maisProximo(latAlvo, lonAlvo);
        System.out.println("Mais próximo de (" + latAlvo + ", " + lonAlvo + "): " + achado
                + " | nós visitados: " + kd.ultimasVisitas() + " de " + kd.tamanho());
        System.out.println("Força bruta confirma: " + forcaBruta(todos, latAlvo, lonAlvo));

        // 4) barcos em uma região
        List<Posicao> regiao = kd.naRegiao(-1.50, -1.40, -48.55, -48.48);
        System.out.println("\nBarcos na região (-1.50..-1.40, -48.55..-48.48): " + regiao.size()
                + " | nós visitados: " + kd.ultimasVisitas());
        for (Posicao p : regiao) {
            System.out.println("  " + p);
        }

        // 5) teste com muitos pontos aleatórios, comparando com a força bruta
        Random sorteio = new Random(1);
        int erros = 0;
        for (int i = 0; i < 1000; i++) {
            double lat = -1.60 + sorteio.nextDouble() * 0.50;
            double lon = -48.60 + sorteio.nextDouble() * 0.20;
            Posicao esperado = forcaBruta(todos, lat, lon);
            Posicao obtido = kd.maisProximo(lat, lon);
            if (distancia(esperado, lat, lon) != distancia(obtido, lat, lon)) {
                erros++;
            }
        }
        System.out.println("\n1000 consultas aleatórias: erros = " + erros);

        // 6) trancar um barco: reconstrói sem ele
        Posicao trancado = todos.get(4);
        System.out.println("\nMais próximo do " + trancado.getBarco().getNome() + " antes de trancar: "
                + kd.maisProximo(trancado.getLat(), trancado.getLon()));
        List<Posicao> ativos = new ArrayList<>(todos);
        ativos.remove(trancado);
        kd.reconstruir(ativos);
        System.out.println("Depois de trancar: " + kd.maisProximo(trancado.getLat(), trancado.getLon()));
        resumo("Sem o barco trancado", kd);

        // 7) desenhos (precisa do Graphviz instalado para gerar o png)
        Path saida = Path.of("saida");
        Files.createDirectories(saida);
        exportar(porInsercao, saida, "kd_insercao", "kD montada por inserção");
        exportar(kd, saida, "kd_reconstruida", "kD reconstruída (mediana)");
    }

    private static void resumo(String titulo, ArvoreKD arvore) {
        System.out.println(titulo + ": barcos=" + arvore.tamanho() + ", altura=" + arvore.altura()
                + ", válida=" + arvore.valida());
    }

    // procura o mais próximo olhando todos os barcos, um por um
    private static Posicao forcaBruta(List<Posicao> lista, double lat, double lon) {
        Posicao melhor = null;
        for (Posicao p : lista) {
            if (melhor == null || distancia(p, lat, lon) < distancia(melhor, lat, lon)) {
                melhor = p;
            }
        }
        return melhor;
    }

    private static double distancia(Posicao p, double lat, double lon) {
        double dLat = p.getLat() - lat;
        double dLon = p.getLon() - lon;
        return dLat * dLat + dLon * dLon;
    }

    private static void exportar(ArvoreKD arvore, Path pasta, String nome, String titulo) throws IOException {
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