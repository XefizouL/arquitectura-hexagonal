// by from ia
package com.tienda.api.application.service;

import com.tienda.api.domain.exception.RecursoNoEncontradoException;
import com.tienda.api.domain.model.DetallePedido;
import com.tienda.api.domain.model.Pedido;
import com.tienda.api.domain.model.Producto;
import com.tienda.api.domain.port.in.PedidoUseCase;
import com.tienda.api.domain.port.out.PedidoRepositoryPort;
import com.tienda.api.domain.port.out.ProductoRepositoryPort;
import com.tienda.api.domain.port.out.TransaccionPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoService implements PedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ProductoRepositoryPort productoRepository;
    private final TransaccionPort transaccion;

    public PedidoService(PedidoRepositoryPort pedidoRepository,
                         ProductoRepositoryPort productoRepository,
                         TransaccionPort transaccion) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.transaccion = transaccion;
    }

    @Override
    public List<Pedido> listarTodos() {
        return pedidoRepository.buscarTodos();
    }

    @Override
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));
    }

    @Override
    public Pedido crear(PedidoCommand comando) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = Pedido.nuevo(comando.cliente());
            pedido.reemplazar(comando.cliente(), construirItems(comando.items()));
            return pedidoRepository.guardar(pedido);
        });
    }

    @Override
    public Pedido actualizar(Long id, PedidoCommand comando) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = buscarPorId(id);
            pedido.reemplazar(comando.cliente(), construirItems(comando.items()));
            return pedidoRepository.guardar(pedido);
        });
    }

    @Override
    public Pedido agregarItem(Long id, ItemCommand item) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = buscarPorId(id);
            pedido.agregarItem(construirItem(item));
            return pedidoRepository.guardar(pedido);
        });
    }

    @Override
    public Pedido quitarItem(Long id, Long itemId) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = buscarPorId(id);
            pedido.quitarItem(itemId);
            return pedidoRepository.guardar(pedido);
        });
    }

    @Override
    public Pedido confirmar(Long id) {
        return transaccion.ejecutar(() -> {
            Pedido pedido = buscarPorId(id);
            Map<Long, Producto> inventario = new HashMap<>();
            for (DetallePedido item : pedido.getItems()) {
                Long productoId = item.getProducto().getId();
                inventario.computeIfAbsent(productoId, this::buscarProducto);
            }

            pedido.confirmar(inventario);

            inventario.values().forEach(productoRepository::guardar);
            return pedidoRepository.guardar(pedido);
        });
    }

    @Override
    public void eliminar(Long id) {
        transaccion.ejecutar(() -> {
            pedidoRepository.eliminar(buscarPorId(id));
            return null;
        });
    }

    private List<DetallePedido> construirItems(List<ItemCommand> comandos) {
        List<DetallePedido> items = new ArrayList<>();
        if (comandos != null) {
            for (ItemCommand c : comandos) {
                items.add(construirItem(c));
            }
        }
        return items;
    }

    private DetallePedido construirItem(ItemCommand comando) {
        return DetallePedido.crear(buscarProducto(comando.productoId()), comando.cantidad());
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }
}
