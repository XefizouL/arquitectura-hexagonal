// by from ia
package com.tienda.api.domain.model;

import com.tienda.api.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;

public class Producto {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;

    public Producto(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
    }

    public void descontarStock(int cantidad) {
        if (stock < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente para " + nombre);
        }
        stock = stock - cantidad;
    }

    public void actualizarDatos(String nombre, String descripcion, BigDecimal precio, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStock() { return stock; }
}
