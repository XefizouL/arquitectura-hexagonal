// by from ia
package com.tienda.api.infrastructure.config;

import com.tienda.api.application.service.PedidoService;
import com.tienda.api.application.service.ProductoService;
import com.tienda.api.domain.port.in.PedidoUseCase;
import com.tienda.api.domain.port.in.ProductoUseCase;
import com.tienda.api.domain.port.out.PedidoRepositoryPort;
import com.tienda.api.domain.port.out.ProductoRepositoryPort;
import com.tienda.api.domain.port.out.TransaccionPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ProductoUseCase productoUseCase(ProductoRepositoryPort productoRepository) {
        return new ProductoService(productoRepository);
    }

    @Bean
    public PedidoUseCase pedidoUseCase(PedidoRepositoryPort pedidoRepository,
                                       ProductoRepositoryPort productoRepository,
                                       TransaccionPort transaccion) {
        return new PedidoService(pedidoRepository, productoRepository, transaccion);
    }
}
