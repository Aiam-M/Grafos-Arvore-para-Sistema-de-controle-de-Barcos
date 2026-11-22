package com.projeto.modelo;

/**
 * Representa um barco registrado no sistema de controle de atracações dos rios de Belém.
 * A inscrição é gerada automaticamente (autoincremento) a cada nova instância.
 */
public class Barco implements Comparable<Barco> {

    private static int proximaInscricao = 1;

    private final int inscricao;
    private final String nome;
    private final String modelo;

    /**
     * Cria um barco e atribui a ele a próxima inscrição disponível.
     *
     * @param nome nome do barco
     * @param modelo modelo ou tipo da embarcação
     */
    public Barco(String nome, String modelo) {
        this.inscricao = proximaInscricao++;
        this.nome = nome;
        this.modelo = modelo;
    }

    /**
     * @return número de inscrição único do barco
     */
    public int getInscricao() {
        return inscricao;
    }

    /**
     * @return nome do barco
     */
    public String getNome() {
        return nome;
    }

    /**
     * @return modelo da embarcação
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * Compara dois barcos pela inscrição.
     *
     * @param outro barco a ser comparado
     * @return negativo, zero ou positivo conforme a inscrição seja menor, igual ou maior
     */
    @Override
    public int compareTo(Barco outro) {
        return Integer.compare(inscricao, outro.inscricao);
    }

    @Override
    public String toString() {
        return "Barco{inscricao=" + inscricao + ", nome='" + nome + "', modelo='" + modelo + "'}";
    }
}
