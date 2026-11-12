// by from ia
package com.tienda.api.infrastructure.adapter.input.rest;

import com.tienda.api.domain.port.in.ProductoUseCase;
import com.tienda.api.domain.port.in.ProductoUseCase.ProductoCommand;
import com.tienda.api.infrastructure.adapter.input.rest.dto.ProductoRequest;
import com.tienda.api.infrastructure.adapter.input.rest.dto.ProductoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoUseCase productoUseCase;

    public ProductoController(ProductoUseCase productoUseCase) {
        this.productoUseCase = productoUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar() {
        return ResponseEntity.ok(productoUseCase.listarTodos().stream().map(ProductoResponse::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponse.desde(productoUseCase.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoResponse.desde(productoUseCase.crear(aComando(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id, @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ProductoResponse.desde(productoUseCase.actualizar(id, aComando(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ProductoCommand aComando(ProductoRequest r) {
        return new ProductoCommand(r.nombre(), r.descripcion(), r.precio(), r.stock());
    }
}
