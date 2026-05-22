package com.aquasolution.util;

import org.springframework.stereotype.Component;

@Component
public class CalculoHidraulicoUtil {

    // Calcula volumen piscina rectangular en m³
    public double calcularVolumenRectangular(double largo, double ancho, double profundidad) {
        return largo * ancho * profundidad;
    }

    // Calcula volumen piscina circular en m³
    public double calcularVolumenCircular(double diametro, double profundidad) {
        double radio = diametro / 2;
        return Math.PI * radio * radio * profundidad;
    }

    // Calcula cloro necesario en gramos (1.5g por m³)
    public double calcularCloroNecesario(double volumenM3) {
        return volumenM3 * 1.5;
    }

    // Calcula algicida necesario en ml (10ml por m³)
    public double calcularAlgicida(double volumenM3) {
        return volumenM3 * 10;
    }

    // Calcula clarificante en ml (5ml por m³)
    public double calcularClarificante(double volumenM3) {
        return volumenM3 * 5;
    }

    // Calcula flujo de bomba recomendado en m³/h
    // Recomienda renovar el agua en 4 horas
    public double calcularFlujoBomba(double volumenM3) {
        return volumenM3 / 4;
    }

    // Redondea a 2 decimales
    public double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}