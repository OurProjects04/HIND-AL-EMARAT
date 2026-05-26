package com.hindalemarat.service;

import com.hindalemarat.dto.CheckoutDto;
import com.hindalemarat.entity.Cart;
import com.hindalemarat.entity.CartItem;
import com.hindalemarat.entity.CustomerOrder;
import com.hindalemarat.entity.OrderItem;
import com.hindalemarat.entity.Product;
import com.hindalemarat.entity.User;
import com.hindalemarat.repository.OrderItemRepository;
import com.hindalemarat.repository.OrderRepository;
import com.hindalemarat.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_onlinePaymentStaysPendingVerification() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Royal Oud");
        product.setPrice(1000);
        product.setDiscountPrice(900);
        product.setStockQuantity(5);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartItems(List.of(cartItem));

        CheckoutDto dto = new CheckoutDto();
        dto.setFullName("Test User");
        dto.setEmail("test@example.com");
        dto.setPhone("9876543210");
        dto.setAddress("Address");
        dto.setCity("City");
        dto.setState("State");
        dto.setPincode("400001");
        dto.setPaymentMethod("ONLINE");

        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder saved = orderService.placeOrder(user, dto);

        assertEquals("PENDING_VERIFICATION", saved.getPaymentStatus());
        verify(cartService).clearCart(user);
    }

    @Test
    void updateStatus_cancelledRestocksInventory() {
        Product product = new Product();
        product.setId(11L);
        product.setStockQuantity(3);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        CustomerOrder order = new CustomerOrder();
        order.setId(100L);
        order.setOrderStatus("PENDING");
        order.setOrderItems(List.of(item));
        order.setPaymentMethod("COD");

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus(100L, "CANCELLED");

        assertEquals(5, product.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void updateStatus_rejectsInvalidStatus() {
        CustomerOrder order = new CustomerOrder();
        order.setId(200L);
        order.setOrderStatus("PENDING");

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> orderService.updateStatus(200L, "HACKED_STATUS"));
    }
}
