package com.projeto.kd;

import com.projeto.modelo.Barco;

/**
 * Guarda onde um barco está no mapa: o barco, a latitude e a longitude.
 */
public class Posicao {

    private final Barco barco;
    private final double lat;
    private final double lon;

    public Posicao(Barco barco, double lat, double lon) {
        this.barco = barco;
        this.lat = lat;
        this.lon = lon;
    }

    public Barco getBarco() {
        return barco;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return barco.getNome() + " (" + lat + ", " + lon + ")";
    }
}