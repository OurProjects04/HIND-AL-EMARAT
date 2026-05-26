package com.hindalemarat.controller;

import com.hindalemarat.entity.User;
import com.hindalemarat.service.CartService;
import com.hindalemarat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal, HttpServletRequest request) {
        // Add requestURI to model for template navigation checks
        model.addAttribute("requestURI", request.getRequestURI());
        
        if (principal != null) {
            User user = userService.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("cartItemCount", cartService.getCartItemCount(user));
            }
        } else {
            model.addAttribute("cartItemCount", 0);
        }
    }
}
