package com.aquasolution.service;

import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuditoriaService auditoriaService;

    public TicketService(TicketRepository ticketRepository, AuditoriaService auditoriaService) {
        this.ticketRepository = ticketRepository;
        this.auditoriaService = auditoriaService;
    }

    public Ticket guardar(Ticket ticket, String usuarioActual, String rolActual) {
        boolean esNuevo = (ticket.getId() == null);
        Ticket guardado = ticketRepository.save(ticket);
        if (esNuevo) {
            auditoriaService.registrar(usuarioActual, rolActual, "CREATE", "Tickets",
                    "Se creó el ticket #" + guardado.getId() + " - " + guardado.getDescripcion());
        } else {
            auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Tickets",
                    "Se actualizó el ticket #" + guardado.getId());
        }
        return guardado;
    }

    public Ticket guardar(Ticket ticket) {
        return guardar(ticket, "Sistema", "SISTEMA");
    }

    public List<Ticket> obtenerTodos() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> obtenerPorId(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> obtenerPorCliente(Usuario cliente) {
        return ticketRepository.findByCliente(cliente);
    }

    public List<Ticket> obtenerPorTecnico(Usuario tecnico) {
        return ticketRepository.findByTecnico(tecnico);
    }

    public List<Ticket> obtenerPorEstado(Ticket.EstadoTicket estado) {
        return ticketRepository.findByEstado(estado);
    }

    public List<Ticket> obtenerPorTecnicoYEstado(Usuario tecnico, Ticket.EstadoTicket estado) {
        return ticketRepository.findByTecnicoAndEstado(tecnico, estado);
    }

    public List<Usuario> obtenerClientesPorTecnico(Usuario tecnico) {
        return ticketRepository.findByTecnico(tecnico)
                .stream()
                .map(Ticket::getCliente)
                .distinct()
                .collect(Collectors.toList());
    }

    public Ticket cambiarEstado(Long id, Ticket.EstadoTicket nuevoEstado, String usuarioActual, String rolActual) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Ticket.EstadoTicket estadoAnterior = ticket.getEstado();
        ticket.setEstado(nuevoEstado);
        if (nuevoEstado == Ticket.EstadoTicket.CERRADO) {
            ticket.setFechaCierre(LocalDateTime.now());
        }
        Ticket guardado = ticketRepository.save(ticket);
        auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Tickets",
                "Ticket #" + id + " cambió estado de " + estadoAnterior + " a " + nuevoEstado);
        return guardado;
    }

    public Ticket cambiarEstado(Long id, Ticket.EstadoTicket nuevoEstado) {
        return cambiarEstado(id, nuevoEstado, "Sistema", "SISTEMA");
    }

    public Ticket asignarTecnico(Long ticketId, Usuario tecnico, String usuarioActual, String rolActual) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setTecnico(tecnico);
        ticket.setEstado(Ticket.EstadoTicket.EN_PROCESO);
        Ticket guardado = ticketRepository.save(ticket);
        auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Tickets",
                "Ticket #" + ticketId + " asignado a técnico: " + tecnico.getNombreCompleto());
        return guardado;
    }

    public Ticket asignarTecnico(Long ticketId, Usuario tecnico) {
        return asignarTecnico(ticketId, tecnico, "Sistema", "SISTEMA");
    }

    public void eliminar(Long id, String usuarioActual, String rolActual) {
        auditoriaService.registrar(usuarioActual, rolActual, "DELETE", "Tickets",
                "Se eliminó el ticket #" + id);
        ticketRepository.deleteById(id);
    }

    public void eliminar(Long id) {
        eliminar(id, "Sistema", "SISTEMA");
    }

    public List<Ticket> obtenerUltimos5() {
        return ticketRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()))
                .limit(5)
                .collect(Collectors.toList());
    }
}