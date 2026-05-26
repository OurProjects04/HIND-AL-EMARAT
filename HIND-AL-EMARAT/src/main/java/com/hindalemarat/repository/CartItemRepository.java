package com.hindalemarat.repository;

import com.hindalemarat.entity.Cart;
import com.hindalemarat.entity.CartItem;
import com.hindalemarat.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
