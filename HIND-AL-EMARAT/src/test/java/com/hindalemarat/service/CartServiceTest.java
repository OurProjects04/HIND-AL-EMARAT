package com.hindalemarat.service;

import com.hindalemarat.entity.Product;
import com.hindalemarat.entity.User;
import com.hindalemarat.repository.CartItemRepository;
import com.hindalemarat.repository.CartRepository;
import com.hindalemarat.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_rejectsZeroOrNegativeQuantity() {
        User user = new User();

        assertThrows(RuntimeException.class, () -> cartService.addToCart(user, 10L, 0));
        assertThrows(RuntimeException.class, () -> cartService.addToCart(user, 10L, -3));

        verify(productRepository, never()).findById(10L);
    }

    @Test
    void addToCart_rejectsInactiveProduct() {
        User user = new User();
        Product product = new Product();
        product.setId(10L);
        product.setActive(false);
        product.setStockQuantity(20);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> cartService.addToCart(user, 10L, 1));
    }
}
