// by from ia
package com.tienda.api.domain.model;

import com.tienda.api.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;

public class DetallePedido {

    private Long id;
    private Producto producto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetallePedido(Long id, Producto producto, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public static DetallePedido crear(Producto producto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        return new DetallePedido(null, producto, cantidad, producto.getPrecio(), subtotal);
    }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
