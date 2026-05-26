package com.hindalemarat.service;

import com.hindalemarat.dto.CheckoutDto;
import com.hindalemarat.entity.*;
import com.hindalemarat.repository.OrderItemRepository;
import com.hindalemarat.repository.OrderRepository;
import com.hindalemarat.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Locale;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    private static final Set<String> VALID_PAYMENT_METHODS = Set.of("COD", "ONLINE");
    private static final Set<String> VALID_ORDER_STATUSES = Set.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

    @Transactional
    public CustomerOrder placeOrder(User user, CheckoutDto dto) {
        Cart cart = cartService.getOrCreateCart(user);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Your cart is empty");
        }

        if (dto == null || dto.getPaymentMethod() == null) {
            throw new RuntimeException("Payment method is required");
        }

        String paymentMethod = dto.getPaymentMethod().trim().toUpperCase(Locale.ROOT);
        if (!VALID_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new RuntimeException("Invalid payment method selected");
        }

        // Validate and decrement stock
        for (CartItem item : cart.getCartItems()) {
            Product p = item.getProduct();
            if (item.getQuantity() <= 0) {
                throw new RuntimeException("Invalid cart quantity for product " + p.getName());
            }
            if (!p.isActive()) {
                throw new RuntimeException("Product " + p.getName() + " is no longer available.");
            }
            if (p.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Product " + p.getName() + " is out of stock.");
            }
            p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
            productRepository.save(p);
        }

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setOrderNumber("HAE-" + System.currentTimeMillis() + "-" + (100 + new Random().nextInt(900)));
        order.setFullName(dto.getFullName());
        order.setEmail(dto.getEmail());
        order.setPhone(dto.getPhone());
        order.setAddress(dto.getAddress());
        order.setCity(dto.getCity());
        order.setState(dto.getState());
        order.setPincode(dto.getPincode());
        order.setTotalAmount(cart.getTotalAmount());
        order.setPaymentMethod(paymentMethod);
        order.setOrderStatus("PENDING");

        if ("ONLINE".equalsIgnoreCase(paymentMethod)) {
            // Online orders must be verified by payment gateway callback before marking paid.
            order.setPaymentStatus("PENDING_VERIFICATION");
        } else {
            order.setPaymentStatus("PENDING");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            
            double finalPrice = (cartItem.getProduct().getDiscountPrice() > 0 && cartItem.getProduct().getDiscountPrice() < cartItem.getProduct().getPrice())
                ? cartItem.getProduct().getDiscountPrice()
                : cartItem.getProduct().getPrice();
            orderItem.setPrice(finalPrice);
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        CustomerOrder savedOrder = orderRepository.save(order);
        cartService.clearCart(user);
        return savedOrder;
    }

    public List<CustomerOrder> findByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<CustomerOrder> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<CustomerOrder> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<CustomerOrder> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public void updateStatus(Long orderId, String status) {
        CustomerOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("Order status is required");
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!VALID_ORDER_STATUSES.contains(normalizedStatus)) {
            throw new RuntimeException("Invalid order status: " + status);
        }

        boolean wasCancelled = "CANCELLED".equalsIgnoreCase(order.getOrderStatus());
        if ("CANCELLED".equals(normalizedStatus) && !wasCancelled) {
            // Restore stock on first cancellation transition.
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setOrderStatus(normalizedStatus);
        if ("DELIVERED".equals(normalizedStatus) && "COD".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setPaymentStatus("PAID");
        }
        orderRepository.save(order);
    }

    public long count() {
        return orderRepository.count();
    }

    public List<CustomerOrder> getRecentOrders(int limit) {
        return orderRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending())).getContent();
    }

    public long countByStatus(String status) {
        return orderRepository.countByOrderStatus(status);
    }
}
