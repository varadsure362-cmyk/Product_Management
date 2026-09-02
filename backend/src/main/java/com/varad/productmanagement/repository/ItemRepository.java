package com.varad.productmanagement.repository;

import com.varad.productmanagement.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByProductId(Long productId);

    Optional<Item> findByIdAndProductId(Long id, Long productId);
}
