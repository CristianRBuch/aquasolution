package com.aquasolution.util;

import org.springframework.stereotype.Component;

@Component
public class CalculoHidraulicoUtil {

    // ══════════════════════════════════════════════
    // PASO 1 — CÁLCULO DE VOLUMEN (Fórmulas I-CPO)
    // Unidades: pies → galones
    // ══════════════════════════════════════════════

    // Rectangular / Cuadrado: Largo x Ancho x Prof x 7.5
    public double calcularVolumenRectangularGalones(double largoPies, double anchoPies, double profPies) {
        return largoPies * anchoPies * profPies * 7.5;
    }

    // Circular: 3.14 x Radio x Radio x Prof x 7.5
    public double calcularVolumenCircularGalones(double diametroPies, double profPies) {
        double radio = diametroPies / 2;
        return Math.PI * radio * radio * profPies * 7.5;
    }

    // Oval: Largo x Ancho x Prof x 6.7
    public double calcularVolumenOvalGalones(double largoPies, double anchoPies, double profPies) {
        return largoPies * anchoPies * profPies * 6.7;
    }

    // Riñón: Largo x Anchura Promedio x Prof x 7.0
    public double calcularVolumenRinonGalones(double largoPies, double anchoPies, double profPies) {
        return largoPies * anchoPies * profPies * 7.0;
    }

    // Conversión metros a pies
    public double metrosAPies(double metros) {
        return metros * 3.28084;
    }

    // Conversión galones a m³
    public double galonesAMetrosCubicos(double galones) {
        return galones * 0.003785;
    }

    // ══════════════════════════════════════════════
    // PASO 2 — DESINFECTANTE (Chlor 65 / Trichlor 90)
    // Fórmula I-CPO: Dosis = base x (vol/10000) x (ppm/1)
    // ══════════════════════════════════════════════

    // Chlor 65: 2.0 oz / 10,000 gal / 1 ppm
    public double calcularChlor65(double volumenGalones, double ppmRequerido) {
        return 2.0 * (volumenGalones / 10000.0) * ppmRequerido;
    }

    // Trichlor 90: 1.5 oz / 10,000 gal / 1 ppm
    public double calcularTrichlor90(double volumenGalones, double ppmRequerido) {
        return 1.5 * (volumenGalones / 10000.0) * ppmRequerido;
    }

    // ══════════════════════════════════════════════
    // PASO 3 — CÁLCULOS POR PARÁMETRO AQUACHECK 7
    // ══════════════════════════════════════════════

    // pH Increaser (Soda Ash): sube 0.1 pH por 1.5 oz / 10,000 gal
    public double calcularPhIncreaser(double volumenGalones, double phActual, double phObjetivo) {
        if (phActual >= phObjetivo) return 0;
        double diferencia = phObjetivo - phActual;
        return 1.5 * (volumenGalones / 10000.0) * (diferencia / 0.1);
    }

    // pH Decreaser (Ácido): baja 0.1 pH por 2.0 oz / 10,000 gal
    public double calcularPhDecreaser(double volumenGalones, double phActual, double phObjetivo) {
        if (phActual <= phObjetivo) return 0;
        double diferencia = phActual - phObjetivo;
        return 2.0 * (volumenGalones / 10000.0) * (diferencia / 0.1);
    }

    // Algicida: 1 fl.oz / 1,550 gal
    public double calcularAlgicidaGalones(double volumenGalones) {
        return volumenGalones / 1550.0;
    }

    // Algicida dosis doble (algas verdes)
    public double calcularAlgicidaDoble(double volumenGalones) {
        return calcularAlgicidaGalones(volumenGalones) * 2;
    }

    // Algicida dosis triple (algas negras/mostaza)
    public double calcularAlgicidaTriple(double volumenGalones) {
        return calcularAlgicidaGalones(volumenGalones) * 3;
    }

    // Clarificador Super Blue: 1 fl.oz / 5,000 gal
    public double calcularClarificadorSuperBlue(double volumenGalones) {
        return volumenGalones / 5000.0;
    }

    // Clarificador ClearAqua: 0.38 fl.oz / 1,000 gal
    public double calcularClarificadorClearAqua(double volumenGalones) {
        return (volumenGalones / 1000.0) * 0.38;
    }

    // ══════════════════════════════════════════════
    // ÍNDICE DE SATURACIÓN DE LANGELIER (LSI)
    // LSI = pH + Temp + Calcio + Alcalinidad - 12.1
    // ══════════════════════════════════════════════

    public double calcularLSI(double ph, double tempF, double calcio, double alcalinidad) {
        double factorPH = ph;
        double factorTemp = calcularFactorTemperatura(tempF);
        double factorCalcio = calcularFactorCalcio(calcio);
        double factorAlcalinidad = calcularFactorAlcalinidad(alcalinidad);
        return redondear(factorPH + factorTemp + factorCalcio + factorAlcalinidad - 12.1);
    }

    private double calcularFactorTemperatura(double tempF) {
        if (tempF <= 32) return 0.0;
        else if (tempF <= 53) return 0.1;
        else if (tempF <= 60) return 0.2;
        else if (tempF <= 66) return 0.3;
        else if (tempF <= 76) return 0.4;
        else if (tempF <= 84) return 0.5;
        else if (tempF <= 94) return 0.6;
        else if (tempF <= 105) return 0.7;
        else return 0.8;
    }

    private double calcularFactorCalcio(double calcioppm) {
        if (calcioppm <= 25) return 1.0;
        else if (calcioppm <= 50) return 1.3;
        else if (calcioppm <= 75) return 1.5;
        else if (calcioppm <= 100) return 1.6;
        else if (calcioppm <= 150) return 1.8;
        else if (calcioppm <= 200) return 1.9;
        else if (calcioppm <= 300) return 2.1;
        else if (calcioppm <= 400) return 2.2;
        else if (calcioppm <= 800) return 2.5;
        else return 2.7;
    }

    private double calcularFactorAlcalinidad(double alcalinidadPpm) {
        if (alcalinidadPpm <= 25) return 1.4;
        else if (alcalinidadPpm <= 50) return 1.7;
        else if (alcalinidadPpm <= 75) return 1.9;
        else if (alcalinidadPpm <= 100) return 2.0;
        else if (alcalinidadPpm <= 150) return 2.2;
        else if (alcalinidadPpm <= 200) return 2.3;
        else if (alcalinidadPpm <= 300) return 2.5;
        else if (alcalinidadPpm <= 400) return 2.6;
        else return 2.7;
    }

    public String interpretarLSI(double lsi) {
        if (lsi < -0.5) return "CORROSIVA";
        else if (lsi < -0.3) return "LIGERAMENTE_CORROSIVA";
        else if (lsi <= 0.3) return "BALANCEADA";
        else if (lsi <= 0.5) return "TENDENCIA_SARRO";
        else return "INCRUSTANTE";
    }

    // ══════════════════════════════════════════════
    // SEMÁFORO DE PARÁMETROS (I-CPO)
    // ══════════════════════════════════════════════

    public String estadoCloro(double ppm) {
        if (ppm >= 2.0 && ppm <= 4.0) return "VERDE";
        else if (ppm >= 1.0 && ppm <= 5.0) return "AMARILLO";
        else return "ROJO";
    }

    public String estadoPH(double ph) {
        if (ph >= 7.4 && ph <= 7.6) return "VERDE";
        else if (ph >= 7.2 && ph <= 7.8) return "AMARILLO";
        else return "ROJO";
    }

    public String estadoAlcalinidad(double ppm) {
        if (ppm >= 80 && ppm <= 120) return "VERDE";
        else if (ppm >= 60 && ppm <= 180) return "AMARILLO";
        else return "ROJO";
    }

    public String estadoCalcio(double ppm) {
        if (ppm >= 200 && ppm <= 400) return "VERDE";
        else if (ppm >= 150 && ppm <= 1000) return "AMARILLO";
        else return "ROJO";
    }

    public String estadoCYA(double ppm) {
        if (ppm >= 30 && ppm <= 50) return "VERDE";
        else if (ppm >= 30 && ppm <= 100) return "AMARILLO";
        else return "ROJO";
    }

    // ══════════════════════════════════════════════
    // MÉTODOS LEGACY (m³) — compatibilidad existente
    // ══════════════════════════════════════════════

    public double calcularVolumenRectangular(double largo, double ancho, double profundidad) {
        return largo * ancho * profundidad;
    }

    public double calcularVolumenCircular(double diametro, double profundidad) {
        double radio = diametro / 2;
        return Math.PI * radio * radio * profundidad;
    }

    public double calcularCloroNecesario(double volumenM3) {
        return volumenM3 * 1.5;
    }

    public double calcularAlgicida(double volumenM3) {
        return volumenM3 * 10;
    }

    public double calcularClarificante(double volumenM3) {
        return volumenM3 * 5;
    }

    public double calcularFlujoBomba(double volumenM3) {
        return volumenM3 / 4;
    }

    public double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}