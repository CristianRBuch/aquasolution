package com.aquasolution.service;

import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.PiscinaRepository;
import com.aquasolution.util.CalculoHidraulicoUtil;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PiscinaService {

    private final PiscinaRepository piscinaRepository;
    private final CalculoHidraulicoUtil calculoUtil;

    public PiscinaService(PiscinaRepository piscinaRepository,
                          CalculoHidraulicoUtil calculoUtil) {
        this.piscinaRepository = piscinaRepository;
        this.calculoUtil = calculoUtil;
    }

    public Piscina guardar(Piscina piscina) {
        // Calcular volumen automáticamente según tipo
        double volumen;
        if (piscina.getTipo() == Piscina.TipoPiscina.CIRCULAR) {
            volumen = calculoUtil.calcularVolumenCircular(
                    piscina.getLargo(), piscina.getProfundidadPromedio());
        } else {
            volumen = calculoUtil.calcularVolumenRectangular(
                    piscina.getLargo(), piscina.getAncho(), piscina.getProfundidadPromedio());
        }
        piscina.setVolumen(calculoUtil.redondear(volumen));
        return piscinaRepository.save(piscina);
    }

    public List<Piscina> obtenerTodas() {
        return piscinaRepository.findAll();
    }

    public List<Piscina> obtenerPorCliente(Usuario cliente) {
        return piscinaRepository.findByCliente(cliente);
    }

    public Optional<Piscina> obtenerPorId(Long id) {
        return piscinaRepository.findById(id);
    }

    public void eliminar(Long id) {
        piscinaRepository.deleteById(id);
    }

    // Calcular dosificación química para una piscina
    public double calcularCloro(double volumen) {
        return calculoUtil.redondear(calculoUtil.calcularCloroNecesario(volumen));
    }

    public double calcularAlgicida(double volumen) {
        return calculoUtil.redondear(calculoUtil.calcularAlgicida(volumen));
    }

    public double calcularClarificante(double volumen) {
        return calculoUtil.redondear(calculoUtil.calcularClarificante(volumen));
    }

    public double calcularFlujoBomba(double volumen) {
        return calculoUtil.redondear(calculoUtil.calcularFlujoBomba(volumen));
    }
}