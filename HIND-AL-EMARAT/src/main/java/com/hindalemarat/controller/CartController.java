package com.hindalemarat.controller;

import com.hindalemarat.entity.Cart;
import com.hindalemarat.entity.User;
import com.hindalemarat.service.CartService;
import com.hindalemarat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class CartController {

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

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        Cart cart = cartService.getOrCreateCart(user);
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            cartService.updateQuantity(user, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Long cartItemId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            cartService.removeItem(user, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
}
