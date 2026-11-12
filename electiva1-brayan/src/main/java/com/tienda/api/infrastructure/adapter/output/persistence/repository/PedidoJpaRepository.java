// by from ia
package com.tienda.api.infrastructure.adapter.output.persistence.repository;

import com.tienda.api.infrastructure.adapter.output.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.producto"})
    List<PedidoEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"items", "items.producto"})
    Optional<PedidoEntity> findById(Long id);
}
