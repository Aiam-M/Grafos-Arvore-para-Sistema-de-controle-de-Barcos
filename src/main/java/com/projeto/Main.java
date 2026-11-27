package com.projeto;

import com.projeto.arvore234.Demo234;
import com.projeto.arvorerubronegra.DemoRubroNegra;
import com.projeto.capitania.DemoCapitania;
import com.projeto.kd.DemoKD;

import java.io.IOException;

/**
 * Ponto de entrada do projeto: roda as demonstrações das três árvores e do sistema que une as três.
 *
 * <p>Uso: {@code java -jar barcos-belem.jar [234 | kd | rn | capitania | todas]} (sem argumento, roda todas).
 * Os desenhos (.dot e .png) vão para a pasta {@code saida/}.
 *
 * <p>Observação: a inscrição dos barcos é um contador global. A demo da 2-3-4 supõe que seus
 * barcos são os de inscrição 1 a 40, por isso em "todas" ela roda primeiro. As outras demos não dependem do número das inscrições.
 */
public class Main {

    /**
     * Executa a demonstração escolhida.
     *
     * @param args {@code 234}, {@code kd}, {@code rn}, {@code capitania} ou {@code todas}
     * @throws IOException se não for possível gravar os arquivos de saída
     */
    public static void main(String[] args) throws IOException {
        String escolha = args.length > 0 ? args[0].toLowerCase() : "todas";
        switch (escolha) {
            case "234" -> Demo234.main(args);
            case "kd" -> DemoKD.main(args);
            case "rn" -> DemoRubroNegra.main(args);
            case "capitania" -> DemoCapitania.main(args);
            case "todas" -> {
                titulo("Árvore 2-3-4 (registro de barcos)");
                Demo234.main(args);
                titulo("Árvore kD (barco mais próximo)");
                DemoKD.main(args);
                titulo("Árvore rubro-negra (agenda de atracações)");
                DemoRubroNegra.main(args);
                titulo("Capitania (as três árvores juntas)");
                DemoCapitania.main(args);
            }
            default -> System.out.println("Uso: java -jar barcos-belem.jar [234 | kd | rn | capitania | todas]");
        }
    }

    private static void titulo(String texto) {
        System.out.println("\n===== " + texto + " =====");
    }
}
