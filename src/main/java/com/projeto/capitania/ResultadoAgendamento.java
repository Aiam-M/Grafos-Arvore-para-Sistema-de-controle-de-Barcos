package com.projeto.capitania;

/**
 * Resultado de uma tentativa de agendar atracação na {@link Capitania}.
 */
public enum ResultadoAgendamento {

    /** A atracação foi agendada. */
    AGENDADA,

    /** A inscrição não existe no registro de barcos. */
    BARCO_INEXISTENTE,

    /** O barco está em manutenção e não pode agendar até ser reativado. */
    EM_MANUTENCAO,

    /** O horário já tem outra atracação (conflito). */
    HORARIO_OCUPADO,

    /** O horário está fora do dia (precisa estar entre 0 e 1439 minutos). */
    HORARIO_INVALIDO
}
