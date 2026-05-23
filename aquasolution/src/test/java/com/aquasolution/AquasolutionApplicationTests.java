package com.aquasolution;

import com.aquasolution.util.CalculoHidraulicoUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AquasolutionApplicationTests {

    private final CalculoHidraulicoUtil calc = new CalculoHidraulicoUtil();

    @Test
    void calcularVolumen_PiscinaRectangular_DebeRetornar100() {
        double resultado = calc.calcularVolumenRectangular(10, 5, 2);
        assertEquals(100.0, resultado, 0.01);
    }

    @Test
    void calcularVolumen_PiscinaCircular_DebeRetornar100_53() {
        double resultado = calc.calcularVolumenCircular(8, 2);
        assertEquals(100.53, resultado, 0.1);
    }

    @Test
    void calcularCloro_Volumen100_DebeRetornar150() {
        double resultado = calc.calcularCloroNecesario(100);
        assertEquals(150.0, resultado, 0.01);
    }

    @Test
    void calcularAlgicida_Volumen100_DebeRetornar1000() {
        double resultado = calc.calcularAlgicida(100);
        assertEquals(1000.0, resultado, 0.01);
    }

    @Test
    void calcularClarificante_Volumen100_DebeRetornar500() {
        double resultado = calc.calcularClarificante(100);
        assertEquals(500.0, resultado, 0.01);
    }

    @Test
    void calcularFlujoBomba_Volumen100_DebeRetornar25() {
        double resultado = calc.calcularFlujoBomba(100);
        assertEquals(25.0, resultado, 0.01);
    }

    @Test
    void redondear_Valor3_14159_DebeRetornar3_14() {
        double resultado = calc.redondear(3.14159);
        assertEquals(3.14, resultado, 0.001);
    }
}