// by from ia
package com.tienda.api.domain;

import com.tienda.api.domain.exception.RecursoNoEncontradoException;
import com.tienda.api.domain.exception.ReglaNegocioException;
import com.tienda.api.domain.model.DetallePedido;
import com.tienda.api.domain.model.EstadoPedido;
import com.tienda.api.domain.model.Pedido;
import com.tienda.api.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoDominioTest {

    private Producto mouse;

    @BeforeEach
    void setUp() {
        mouse = new Producto(1L, "Mouse Inalámbrico", null, new BigDecimal("50000.00"), 10);
    }

    @Test
    void agregarItem_calculaSubtotalYTotal() {
        Pedido pedido = Pedido.nuevo("Brayan");
        pedido.agregarItem(DetallePedido.crear(mouse, 2));

        assertEquals(new BigDecimal("100000.00"), pedido.getTotal());
        assertEquals(EstadoPedido.CARRITO, pedido.getEstado());
    }

    @Test
    void crearItem_conCantidadCero_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> DetallePedido.crear(mouse, 0));
    }

    @Test
    void nuevo_sinCliente_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> Pedido.nuevo("  "));
    }

    @Test
    void confirmar_conStock_descuentaYCambiaEstado() {
        Pedido pedido = Pedido.nuevo("Brayan");
        pedido.agregarItem(DetallePedido.crear(mouse, 3));

        pedido.confirmar(Map.of(1L, mouse));

        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
        assertEquals(7, mouse.getStock());
    }

    @Test
    void confirmar_sinStock_lanzaReglaNegocio() {
        Pedido pedido = Pedido.nuevo("Brayan");
        pedido.agregarItem(DetallePedido.crear(mouse, 15));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> pedido.confirmar(Map.of(1L, mouse)));
        assertEquals("Stock insuficiente para Mouse Inalámbrico", ex.getMessage());
    }

    @Test
    void confirmar_carritoVacio_lanzaReglaNegocio() {
        Pedido pedido = Pedido.nuevo("Brayan");
        assertThrows(ReglaNegocioException.class, () -> pedido.confirmar(Map.of()));
    }

    @Test
    void pedidoConfirmado_noSePuedeModificar() {
        Pedido pedido = new Pedido(1L, "Brayan", LocalDateTime.now(), EstadoPedido.CONFIRMADO,
                BigDecimal.ZERO, List.of());
        assertThrows(ReglaNegocioException.class,
                () -> pedido.agregarItem(DetallePedido.crear(mouse, 1)));
    }

    @Test
    void quitarItem_recalculaTotal() {
        DetallePedido d1 = new DetallePedido(101L, mouse, 1, mouse.getPrecio(), new BigDecimal("50000.00"));
        DetallePedido d2 = new DetallePedido(102L, mouse, 2, mouse.getPrecio(), new BigDecimal("100000.00"));
        Pedido pedido = new Pedido(1L, "Brayan", LocalDateTime.now(), EstadoPedido.CARRITO,
                new BigDecimal("150000.00"), List.of(d1, d2));

        pedido.quitarItem(101L);

        assertEquals(1, pedido.getItems().size());
        assertEquals(new BigDecimal("100000.00"), pedido.getTotal());
    }

    @Test
    void quitarItem_inexistente_lanzaNoEncontrado() {
        Pedido pedido = Pedido.nuevo("Brayan");
        assertThrows(RecursoNoEncontradoException.class, () -> pedido.quitarItem(99L));
    }
}
