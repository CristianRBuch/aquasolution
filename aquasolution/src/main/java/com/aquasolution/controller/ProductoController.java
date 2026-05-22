package com.aquasolution.controller;

import com.aquasolution.model.Producto;
import com.aquasolution.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerActivos());
        model.addAttribute("alertasStock", productoService.obtenerConStockBajo());
        return "productos/lista";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@RequestParam String nombre,
                                  @RequestParam(required = false) String descripcion,
                                  @RequestParam String unidad,
                                  @RequestParam BigDecimal precioBase,
                                  @RequestParam Integer stock,
                                  @RequestParam Integer stockMinimo,
                                  RedirectAttributes redirect) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setUnidad(unidad);
        producto.setPrecioBase(precioBase);
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        productoService.guardar(producto);
        redirect.addFlashAttribute("exito", "Producto registrado correctamente.");
        return "redirect:/productos";
    }

    @PostMapping("/editar")
    public String editarProducto(@RequestParam Long id,
                                 @RequestParam String nombre,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam String unidad,
                                 @RequestParam BigDecimal precioBase,
                                 @RequestParam Integer stock,
                                 @RequestParam Integer stockMinimo,
                                 RedirectAttributes redirect) {
        Producto producto = productoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setUnidad(unidad);
        producto.setPrecioBase(precioBase);
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        productoService.guardar(producto);
        redirect.addFlashAttribute("exito", "Producto actualizado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirect) {
        productoService.eliminar(id);
        redirect.addFlashAttribute("exito", "Producto desactivado correctamente.");
        return "redirect:/productos";
    }
}