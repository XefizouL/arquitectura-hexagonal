// by from ia
package com.tienda.api.application;

import com.tienda.api.application.service.PedidoService;
import com.tienda.api.application.service.ProductoService;
import com.tienda.api.domain.exception.RecursoNoEncontradoException;
import com.tienda.api.domain.exception.ReglaNegocioException;
import com.tienda.api.domain.model.EstadoPedido;
import com.tienda.api.domain.model.Pedido;
import com.tienda.api.domain.model.Producto;
import com.tienda.api.domain.port.in.PedidoUseCase.ItemCommand;
import com.tienda.api.domain.port.in.PedidoUseCase.PedidoCommand;
import com.tienda.api.domain.port.in.ProductoUseCase.ProductoCommand;
import com.tienda.api.domain.port.out.PedidoRepositoryPort;
import com.tienda.api.domain.port.out.ProductoRepositoryPort;
import com.tienda.api.domain.port.out.TransaccionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasosDeUsoTest {

    private ProductoService productos;
    private PedidoService pedidos;
    private ProductoRepositoryPort productoRepo;

    @BeforeEach
    void setUp() {
        productoRepo = new ProductoEnMemoria();
        PedidoRepositoryPort pedidoRepo = new PedidoEnMemoria();
        TransaccionPort sinTransaccion = new TransaccionPort() {
            @Override
            public <T> T ejecutar(java.util.function.Supplier<T> operacion) {
                return operacion.get();
            }
        };
        productos = new ProductoService(productoRepo);
        pedidos = new PedidoService(pedidoRepo, productoRepo, sinTransaccion);
    }

    private Producto crearProducto(String nombre, String precio, int stock) {
        return productos.crear(new ProductoCommand(nombre, null, new BigDecimal(precio), stock));
    }

    @Test
    void producto_crearYConsultar() {
        Producto creado = crearProducto("Teclado", "189900.00", 15);

        assertEquals(creado.getId(), productos.buscarPorId(creado.getId()).getId());
        assertEquals(1, productos.listarTodos().size());
    }

    @Test
    void producto_actualizar() {
        Producto creado = crearProducto("Teclado", "100.00", 5);

        Producto actualizado = productos.actualizar(creado.getId(),
                new ProductoCommand("Teclado Pro", "RGB", new BigDecimal("200.00"), 9));

        assertEquals("Teclado Pro", actualizado.getNombre());
        assertEquals(9, actualizado.getStock());
    }

    @Test
    void producto_noExistente_lanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> productos.buscarPorId(99L));
    }

    @Test
    void pedido_crearCalculaTotal() {
        Producto mouse = crearProducto("Mouse", "50000.00", 10);

        Pedido pedido = pedidos.crear(new PedidoCommand("Brayan",
                List.of(new ItemCommand(mouse.getId(), 2))));

        assertEquals(EstadoPedido.CARRITO, pedido.getEstado());
        assertEquals(new BigDecimal("100000.00"), pedido.getTotal());
    }

    @Test
    void pedido_conProductoInexistente_lanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidos.crear(new PedidoCommand("Brayan", List.of(new ItemCommand(99L, 1)))));
    }

    @Test
    void pedido_agregarYQuitarItem() {
        Producto mouse = crearProducto("Mouse", "50000.00", 10);
        Pedido pedido = pedidos.crear(new PedidoCommand("Brayan", List.of()));

        Pedido conItem = pedidos.agregarItem(pedido.getId(), new ItemCommand(mouse.getId(), 1));
        assertEquals(new BigDecimal("50000.00"), conItem.getTotal());

        Long itemId = conItem.getItems().get(0).getId();
        Pedido sinItem = pedidos.quitarItem(pedido.getId(), itemId);
        assertTrue(sinItem.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, sinItem.getTotal());
    }

    @Test
    void pedido_actualizarReemplazaItems() {
        Producto mouse = crearProducto("Mouse", "50000.00", 10);
        Pedido pedido = pedidos.crear(new PedidoCommand("Brayan",
                List.of(new ItemCommand(mouse.getId(), 1))));

        Pedido actualizado = pedidos.actualizar(pedido.getId(),
                new PedidoCommand("Otro Cliente", List.of(new ItemCommand(mouse.getId(), 3))));

        assertEquals("Otro Cliente", actualizado.getCliente());
        assertEquals(1, actualizado.getItems().size());
        assertEquals(new BigDecimal("150000.00"), actualizado.getTotal());
    }

    @Test
    void pedido_confirmarDescuentaStockYBloqueaModificaciones() {
        Producto mouse = crearProducto("Mouse", "50000.00", 10);
        Pedido pedido = pedidos.crear(new PedidoCommand("Brayan",
                List.of(new ItemCommand(mouse.getId(), 3))));

        Pedido confirmado = pedidos.confirmar(pedido.getId());

        assertEquals(EstadoPedido.CONFIRMADO, confirmado.getEstado());
        assertEquals(7, productos.buscarPorId(mouse.getId()).getStock());
        assertThrows(ReglaNegocioException.class,
                () -> pedidos.agregarItem(pedido.getId(), new ItemCommand(mouse.getId(), 1)));
    }

    @Test
    void pedido_confirmarConMismoProductoRepetidoValidaStockAcumulado() {
        Producto mouse = crearProducto("Mouse", "50000.00", 5);
        Pedido pedido = pedidos.crear(new PedidoCommand("Brayan",
                List.of(new ItemCommand(mouse.getId(), 3), new ItemCommand(mouse.getId(), 3))));

        assertThrows(ReglaNegocioException.class, () -> pedidos.confirmar(pedido.getId()));
        assertEquals(5, productos.buscarPorId(mouse.getId()).getStock());
    }

    @Test
    void pedido_eliminar() {
        Pedido pedido = pedidos.crear(new PedidoCommand("Brayan", List.of()));
        pedidos.eliminar(pedido.getId());
        assertThrows(RecursoNoEncontradoException.class, () -> pedidos.buscarPorId(pedido.getId()));
    }

    private static class ProductoEnMemoria implements ProductoRepositoryPort {
    private final Map<Long, Producto> datos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

        @Override
        public List<Producto> buscarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public Optional<Producto> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id)).map(p ->
                    new Producto(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock()));
        }

        @Override
        public Producto guardar(Producto p) {
            Long id = p.getId() != null ? p.getId() : secuencia.incrementAndGet();
            Producto guardado = new Producto(id, p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock());
            datos.put(id, guardado);
            return guardado;
        }

        @Override
        public void eliminar(Producto p) {
            datos.remove(p.getId());
        }
    }

    private static class PedidoEnMemoria implements PedidoRepositoryPort {
    private final Map<Long, Pedido> datos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();
    private final AtomicLong secuenciaItems = new AtomicLong();

        @Override
        public List<Pedido> buscarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public Optional<Pedido> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id)).map(this::copiar);
        }

        @Override
        public Pedido guardar(Pedido p) {
            Long id = p.getId() != null ? p.getId() : secuencia.incrementAndGet();
            List<com.tienda.api.domain.model.DetallePedido> items = p.getItems().stream()
                    .map(d -> new com.tienda.api.domain.model.DetallePedido(
                            d.getId() != null ? d.getId() : secuenciaItems.incrementAndGet(),
                            d.getProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                    .toList();
            Pedido guardado = new Pedido(id, p.getCliente(), p.getFecha(), p.getEstado(), p.getTotal(), items);
            datos.put(id, guardado);
            return copiar(guardado);
        }

        @Override
        public void eliminar(Pedido p) {
            datos.remove(p.getId());
        }

        private Pedido copiar(Pedido p) {
            return new Pedido(p.getId(), p.getCliente(), p.getFecha(), p.getEstado(), p.getTotal(), p.getItems());
        }
    }
}
