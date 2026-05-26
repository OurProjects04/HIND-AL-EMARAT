package com.hindalemarat.service;

import com.hindalemarat.entity.Cart;
import com.hindalemarat.entity.CartItem;
import com.hindalemarat.entity.Product;
import com.hindalemarat.entity.User;
import com.hindalemarat.repository.CartItemRepository;
import com.hindalemarat.repository.CartRepository;
import com.hindalemarat.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUser(user);
                return cartRepository.save(cart);
            });
    }

    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isActive()) {
            throw new RuntimeException("This product is no longer available.");
        }

        if (product.getStockQuantity() <= 0) {
            throw new RuntimeException("This product is out of stock.");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available.");
        }

        Cart cart = getOrCreateCart(user);
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Cannot add more. Insufficient stock.");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            cart.getCartItems().add(item);
        }
    }

    @Transactional
    public void updateQuantity(User user, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security check
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        if (!item.getProduct().isActive()) {
            throw new RuntimeException("This product is no longer available.");
        }

        if (item.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security check
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        Cart cart = item.getCart();
        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public int getCartItemCount(User user) {
        return cartRepository.findByUser(user)
            .map(cart -> cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum())
            .orElse(0);
    }
}
