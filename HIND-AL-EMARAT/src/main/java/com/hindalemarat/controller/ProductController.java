package com.hindalemarat.controller;

import com.hindalemarat.entity.Category;
import com.hindalemarat.entity.Product;
import com.hindalemarat.service.CategoryService;
import com.hindalemarat.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(@RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String search,
                               Model model) {
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.search(search);
            model.addAttribute("searchQuery", search);
        } else if (categoryId != null) {
            Category category = categoryService.findById(categoryId).orElse(null);
            if (category != null) {
                products = productService.findByCategory(category);
                model.addAttribute("selectedCategory", categoryId);
            } else {
                products = productService.findActive();
            }
        } else {
            products = productService.findActive();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findActive());
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        // If viewing a demo product, fetch main product as well
        Product mainProduct;
        Product demoProduct = null;

        if (product.isDemoProduct() && product.getParentProductId() != null) {
            // This is a demo, fetch the main product
            mainProduct = productService.findById(product.getParentProductId())
                .orElse(product);
            demoProduct = product;
        } else {
            // This is a main product, check if it has a demo
            mainProduct = product;
            demoProduct = productService.findDemoProductByParentId(id).orElse(null);
        }

        // Get related products from main product's category
        final Product finalMainProduct = mainProduct;
        List<Product> relatedProducts = List.of();
        if (mainProduct.getCategory() != null) {
            relatedProducts = productService.findByCategory(mainProduct.getCategory()).stream()
                .filter(p -> !p.getId().equals(finalMainProduct.getId()) && !p.isDemoProduct())
                .limit(4)
                .collect(Collectors.toList());
        }

        model.addAttribute("product", mainProduct);
        model.addAttribute("demoProduct", demoProduct);
        model.addAttribute("relatedProducts", relatedProducts);
        return "products/detail";
    }
}
