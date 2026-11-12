// by from ia
package com.tienda.api.domain.model;

import com.tienda.api.domain.exception.RecursoNoEncontradoException;
import com.tienda.api.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pedido {

    private Long id;
    private String cliente;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private BigDecimal total;
    private List<DetallePedido> items;

    public Pedido(Long id, String cliente, LocalDateTime fecha, EstadoPedido estado,
                  BigDecimal total, List<DetallePedido> items) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public static Pedido nuevo(String cliente) {
        return new Pedido(null, validarCliente(cliente), LocalDateTime.now(),
                EstadoPedido.CARRITO, BigDecimal.ZERO, new ArrayList<>());
    }

    public void agregarItem(DetallePedido item) {
        asegurarCarrito();
        items.add(item);
        recalcularTotal();
    }

    public void quitarItem(Long itemId) {
        asegurarCarrito();
        boolean eliminado = items.removeIf(i -> i.getId() != null && i.getId().equals(itemId));
        if (!eliminado) {
            throw new RecursoNoEncontradoException("Ítem no encontrado con id " + itemId);
        }
        recalcularTotal();
    }

    public void reemplazar(String nuevoCliente, List<DetallePedido> nuevosItems) {
        asegurarCarrito();
        this.cliente = validarCliente(nuevoCliente);
        this.items.clear();
        this.items.addAll(nuevosItems);
        recalcularTotal();
    }

    public void confirmar(Map<Long, Producto> inventario) {
        asegurarCarrito();
        if (items.isEmpty()) {
            throw new ReglaNegocioException("El carrito está vacío");
        }
        for (DetallePedido item : items) {
            inventario.get(item.getProducto().getId()).descontarStock(item.getCantidad());
        }
        estado = EstadoPedido.CONFIRMADO;
    }

    public void recalcularTotal() {
        total = items.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void asegurarCarrito() {
        if (estado != EstadoPedido.CARRITO) {
            throw new ReglaNegocioException("Solo se pueden modificar pedidos en estado CARRITO");
        }
    }

    private static String validarCliente(String cliente) {
        if (cliente == null || cliente.trim().isEmpty()) {
            throw new ReglaNegocioException("El cliente es obligatorio");
        }
        return cliente.trim();
    }

    public Long getId() { return id; }
    public String getCliente() { return cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoPedido getEstado() { return estado; }
    public BigDecimal getTotal() { return total; }
    public List<DetallePedido> getItems() { return List.copyOf(items); }
}
