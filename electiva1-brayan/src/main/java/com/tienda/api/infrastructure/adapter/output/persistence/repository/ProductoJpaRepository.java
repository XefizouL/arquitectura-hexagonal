// by from ia
package com.tienda.api.infrastructure.adapter.output.persistence.repository;

import com.tienda.api.infrastructure.adapter.output.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {
}
