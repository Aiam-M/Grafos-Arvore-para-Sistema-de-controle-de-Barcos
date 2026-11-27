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
    private StatusBarco status = StatusBarco.ATIVO;

    /**
     * Cria um barco (ativo) e atribui a ele a próxima inscrição disponível.
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
     * @return situação atual do barco (ativo ou em manutenção)
     */
    public StatusBarco getStatus() {
        return status;
    }

    /**
     * Muda a situação do barco. Para trancar ou reativar um barco do sistema, use
     * {@code Capitania.trancar} e {@code Capitania.reativar}, que também atualizam mapa e agenda.
     *
     * @param status nova situação
     */
    public void setStatus(StatusBarco status) {
        this.status = status;
    }

    /**
     * @return {@code true} se o barco está ativo (não está em manutenção)
     */
    public boolean estaAtivo() {
        return status == StatusBarco.ATIVO;
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
