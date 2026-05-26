package com.hindalemarat.controller;

import com.hindalemarat.dto.UserRegistrationDto;
import com.hindalemarat.entity.User;
import com.hindalemarat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

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
    public String viewProfile(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        // Add basic address details directly to the model as well for better UX
        model.addAttribute("user", dto);
        model.addAttribute("address", user.getAddress());
        model.addAttribute("city", user.getCity());
        model.addAttribute("state", user.getState());
        model.addAttribute("pincode", user.getPincode());
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") UserRegistrationDto dto,
                                @RequestParam String address,
                                @RequestParam String city,
                                @RequestParam String state,
                                @RequestParam String pincode,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            userService.updateProfile(user.getId(), dto, address, city, state, pincode);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }
}
