package com.projeto.arvorerubronegra;

import com.projeto.modelo.Barco;

/**
 * Uma atracação agendada: o horário (em minutos desde 00:00) e o barco que vai atracar.
 * Exemplo: 14:30 = 14 * 60 + 30 = 870.
 */
public class Atracacao {

    public static final int MINUTOS_POR_DIA = 24 * 60;

    private final int horario;
    private final Barco barco;

    /**
     * @param horario minutos do dia, entre 0 (00:00) e 1439 (23:59)
     * @param barco barco que vai atracar
     * @throws IllegalArgumentException se o horário estiver fora do dia ou o barco for nulo
     */
    public Atracacao(int horario, Barco barco) {
        if (horario < 0 || horario >= MINUTOS_POR_DIA) {
            throw new IllegalArgumentException("Horário fora do dia (0 a 1439): " + horario);
        }
        if (barco == null) {
            throw new IllegalArgumentException("A atracação precisa de um barco");
        }
        this.horario = horario;
        this.barco = barco;
    }

    /**
     * Converte hora e minuto em minutos do dia (ex.: 14, 30 -> 870).
     */
    public static int emMinutos(int hora, int minuto) {
        return hora * 60 + minuto;
    }

    /**
     * Converte minutos do dia em texto "HH:MM" (ex.: 870 -> "14:30").
     */
    public static String formatar(int minutosDoDia) {
        return String.format("%02d:%02d", minutosDoDia / 60, minutosDoDia % 60);
    }

    public int getHorario() {
        return horario;
    }

    public Barco getBarco() {
        return barco;
    }

    @Override
    public String toString() {
        return formatar(horario) + " -> " + barco.getNome();
    }
}
