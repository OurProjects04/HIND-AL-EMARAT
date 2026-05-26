package com.hindalemarat.controller;

import com.hindalemarat.service.CategoryService;
import com.hindalemarat.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productService.findFeatured());
        model.addAttribute("categories", categoryService.findActive());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
