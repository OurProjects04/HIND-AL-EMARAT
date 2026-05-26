package com.hindalemarat.controller;

import com.hindalemarat.dto.CheckoutDto;
import com.hindalemarat.entity.Cart;
import com.hindalemarat.entity.CustomerOrder;
import com.hindalemarat.entity.User;
import com.hindalemarat.service.CartService;
import com.hindalemarat.service.OrderService;
import com.hindalemarat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userService.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        Cart cart = cartService.getOrCreateCart(user);
        
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutDto checkoutDto = new CheckoutDto();
        // Prefill shipping information from user profile
        checkoutDto.setFullName(user.getFirstName() + " " + user.getLastName());
        checkoutDto.setEmail(user.getEmail());
        checkoutDto.setPhone(user.getPhone());
        checkoutDto.setAddress(user.getAddress());
        checkoutDto.setCity(user.getCity());
        checkoutDto.setState(user.getState());
        checkoutDto.setPincode(user.getPincode());

        model.addAttribute("checkout", checkoutDto);
        model.addAttribute("cart", cart);
        return "checkout/checkout";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkout") CheckoutDto dto,
                             BindingResult bindingResult,
                             Principal principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            Cart cart = cartService.getOrCreateCart(user);

            if (bindingResult.hasErrors()) {
                model.addAttribute("cart", cart);
                return "checkout/checkout";
            }

            CustomerOrder order = orderService.placeOrder(user, dto);

            userService.updateShippingAddressIfMissing(
                user,
                dto.getPhone(),
                dto.getAddress(),
                dto.getCity(),
                dto.getState(),
                dto.getPincode()
            );

            redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
            return "redirect:/orders/success";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/orders/success")
    public String orderSuccess() {
        return "orders/success";
    }

    @GetMapping("/orders/my-orders")
    public String myOrders(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        model.addAttribute("orders", orderService.findByUser(user));
        return "orders/my-orders";
    }
}
