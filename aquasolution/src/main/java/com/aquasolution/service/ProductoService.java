package com.aquasolution.service;

import com.aquasolution.model.Producto;
import com.aquasolution.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto guardar(Producto producto) {
        producto.calcularPrecioConIva();
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> obtenerConStockBajo() {
        // Retorna productos donde stock <= stockMinimo
        return productoRepository.findAll().stream()
                .filter(p -> p.getActivo() && p.getStock() <= p.getStockMinimo())
                .collect(java.util.stream.Collectors.toList());
    }

    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    public Producto actualizarStock(Long id, Integer cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()
                    + ". Stock disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidad);
        return productoRepository.save(producto);
    }
}