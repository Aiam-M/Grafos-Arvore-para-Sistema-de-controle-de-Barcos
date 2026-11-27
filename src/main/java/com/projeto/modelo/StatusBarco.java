package com.projeto.modelo;

/**
 * Situação de um barco registrado: se pode aparecer no mapa e agendar atracações ou não.
 */
public enum StatusBarco {

    /** Barco operando: aparece no mapa (kD) e pode agendar atracações. */
    ATIVO,

    /** Barco trancado para manutenção: continua no registro, mas sai do mapa e da agenda. */
    EM_MANUTENCAO
}
