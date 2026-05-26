package com.hindalemarat.controller;

import com.hindalemarat.entity.Cart;
import com.hindalemarat.entity.CartItem;
import com.hindalemarat.entity.CustomerOrder;
import com.hindalemarat.entity.Product;
import com.hindalemarat.entity.User;
import com.hindalemarat.service.CartService;
import com.hindalemarat.service.OrderService;
import com.hindalemarat.service.UserService;
import com.hindalemarat.util.FileUploadUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserService userService;

    @MockBean(name = "fileUploadUtil")
    private FileUploadUtil fileUploadUtil;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    private Cart buildCart(User user) {
        Product product = new Product();
        product.setId(10L);
        product.setName("Royal Oud");
        product.setPrice(1000);
        product.setStockQuantity(10);
        product.setActive(true);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(1);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartItems(List.of(item));
        return cart;
    }

    @Test
    void placeOrder_withValidationErrorsReturnsCheckoutView() throws Exception {
        User user = buildUser();
        Cart cart = buildCart(user);

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(fileUploadUtil.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/checkout/place-order")
                .with(csrf())
                .principal(() -> "user@example.com")
                .param("fullName", "")
                .param("email", "bad-email")
                .param("phone", "123")
                .param("address", "")
                .param("city", "")
                .param("state", "")
                .param("pincode", "12")
                .param("paymentMethod", "ONLINE"))
            .andExpect(status().isOk())
            .andExpect(view().name("checkout/checkout"));

        verify(orderService, never()).placeOrder(any(), any());
    }

    @Test
    void placeOrder_successRedirectsToSuccessPage() throws Exception {
        User user = buildUser();
        Cart cart = buildCart(user);
        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber("HAE-TEST-001");

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(orderService.placeOrder(any(), any())).thenReturn(order);
        when(fileUploadUtil.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/checkout/place-order")
                .with(csrf())
                .principal(() -> "user@example.com")
                .param("fullName", "Test User")
                .param("email", "user@example.com")
                .param("phone", "9876543210")
                .param("address", "Address")
                .param("city", "Mumbai")
                .param("state", "Maharashtra")
                .param("pincode", "400001")
                .param("paymentMethod", "COD"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/orders/success"));
    }
}
