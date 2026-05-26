package com.hindalemarat.controller;

import com.hindalemarat.entity.ContactMessage;
import com.hindalemarat.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("message", new ContactMessage());
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@ModelAttribute("message") ContactMessage message,
                                RedirectAttributes redirectAttributes) {
        contactService.save(message);
        redirectAttributes.addFlashAttribute("success", "Your message has been submitted. We will get back to you soon!");
        return "redirect:/contact";
    }
}
