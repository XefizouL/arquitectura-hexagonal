// by from ia
package com.tienda.api.infrastructure.adapter.input.rest;

import com.tienda.api.domain.port.in.PedidoUseCase;
import com.tienda.api.domain.port.in.PedidoUseCase.ItemCommand;
import com.tienda.api.domain.port.in.PedidoUseCase.PedidoCommand;
import com.tienda.api.infrastructure.adapter.input.rest.dto.ItemRequest;
import com.tienda.api.infrastructure.adapter.input.rest.dto.PedidoRequest;
import com.tienda.api.infrastructure.adapter.input.rest.dto.PedidoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoUseCase pedidoUseCase;

    public PedidoController(PedidoUseCase pedidoUseCase) {
        this.pedidoUseCase = pedidoUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listar() {
        return ResponseEntity.ok(pedidoUseCase.listarTodos().stream().map(PedidoResponse::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(PedidoResponse.desde(pedidoUseCase.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PedidoResponse.desde(pedidoUseCase.crear(aComando(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> actualizar(@PathVariable Long id, @RequestBody PedidoRequest request) {
        return ResponseEntity.ok(PedidoResponse.desde(pedidoUseCase.actualizar(id, aComando(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<PedidoResponse> agregarItem(@PathVariable Long id, @RequestBody ItemRequest request) {
        return ResponseEntity.ok(PedidoResponse.desde(
                pedidoUseCase.agregarItem(id, new ItemCommand(request.productoId(), request.cantidad()))));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<PedidoResponse> quitarItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(PedidoResponse.desde(pedidoUseCase.quitarItem(id, itemId)));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(PedidoResponse.desde(pedidoUseCase.confirmar(id)));
    }

    private PedidoCommand aComando(PedidoRequest r) {
        List<ItemCommand> items = r.items() == null ? List.of()
                : r.items().stream().map(i -> new ItemCommand(i.productoId(), i.cantidad())).toList();
        return new PedidoCommand(r.cliente(), items);
    }
}
