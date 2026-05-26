package com.hindalemarat.controller;

import com.hindalemarat.dto.ProductDto;
import com.hindalemarat.entity.Category;
import com.hindalemarat.entity.Product;
import com.hindalemarat.util.FileUploadUtil;
import com.hindalemarat.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalOrders", orderService.count());
        model.addAttribute("pendingOrders", orderService.countByStatus("PENDING"));
        model.addAttribute("unreadMessages", contactService.countUnread());
        model.addAttribute("recentOrders", orderService.getRecentOrders(5));
        model.addAttribute("totalCategories", categoryService.count());
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);

        java.util.Map<Long, Integer> demoCounts = new java.util.HashMap<>();
        java.util.Map<Long, Product> parentProducts = new java.util.HashMap<>();

        for (Product p : products) {
            if (p.isDemoProduct() && p.getParentProductId() != null) {
                parentProducts.put(p.getId(), productService.findById(p.getParentProductId()).orElse(null));
                demoCounts.merge(p.getParentProductId(), 1, Integer::sum);
            }
        }

        model.addAttribute("demoCounts", demoCounts);
        model.addAttribute("parentProducts", parentProducts);
        return "admin/products";
    }

    @ModelAttribute("getParentProduct")
    public java.util.function.Function<Long, Product> getParentProduct() {
        return parentId -> productService.findById(parentId).orElse(null);
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new ProductDto());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("existingImageUrl", null);
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setSize(product.getSize());
        dto.setFragranceNotes(product.getFragranceNotes());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setStockQuantity(product.getStockQuantity());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());
        dto.setQuantityMl(product.getQuantityMl());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setDiscountPercentage(product.getDiscountPercentage());

        model.addAttribute("product", dto);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("existingImageUrl", product.getImageUrl());

        Optional<Product> demoProduct = productService.findDemoProductByParentId(id);
        model.addAttribute("demoProduct", demoProduct.orElse(null));

        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(
            @ModelAttribute("product") ProductDto dto,
            @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
            @RequestParam(value = "demoQuantityMl", required = false) Integer demoQuantityMl,
            @RequestParam(value = "demoStockQuantity", required = false) Integer demoStockQuantity,
            @RequestParam(value = "demoPriceValue", required = false) Double demoPriceValue,
            @RequestParam(value = "demoImageFile", required = false) MultipartFile demoImageFile,
            @RequestParam(value = "demoActive", required = false, defaultValue = "false") boolean demoActive,
            @RequestParam(value = "removeDemo", required = false, defaultValue = "false") boolean removeDemo,
            @RequestParam(value = "existingDemoId", required = false) Long existingDemoId,
            RedirectAttributes redirectAttributes) {

        try {
            // ==================== VALIDATION ====================
            validateProductData(dto, mainImageFile, demoQuantityMl, demoStockQuantity, demoPriceValue, demoImageFile);

            // ==================== HANDLE MAIN PRODUCT IMAGE ====================
            String mainImagePath = null;
            if (mainImageFile != null && !mainImageFile.isEmpty()) {
                mainImagePath = fileUploadUtil.uploadProductImage(mainImageFile);
            }

            // ==================== SAVE MAIN PRODUCT ====================
            Product savedMainProduct;

            if (dto.getId() != null && dto.getId() > 0) {
                // UPDATE existing product
                savedMainProduct = productService.findById(dto.getId()).orElse(null);
                if (savedMainProduct == null) {
                    throw new RuntimeException("Product not found");
                }
                savedMainProduct.setName(dto.getName());
                savedMainProduct.setDescription(dto.getDescription());
                savedMainProduct.setPrice(dto.getPrice());
                savedMainProduct.setDiscountPrice(dto.getDiscountPrice());
                savedMainProduct.setSize(dto.getSize());
                savedMainProduct.setFragranceNotes(dto.getFragranceNotes());
                savedMainProduct.setStockQuantity(dto.getStockQuantity());
                savedMainProduct.setActive(dto.isActive());
                savedMainProduct.setFeatured(dto.isFeatured());
                savedMainProduct.setQuantityMl(dto.getQuantityMl());
                savedMainProduct.setOriginalPrice(dto.getOriginalPrice());
                savedMainProduct.setDiscountPercentage(dto.getDiscountPercentage());

                if (mainImagePath != null) {
                    savedMainProduct.setImageUrl(mainImagePath);
                }

                if (dto.getCategoryId() != null) {
                    Category category = categoryService.findById(dto.getCategoryId()).orElse(null);
                    savedMainProduct.setCategory(category);
                }

                savedMainProduct = productService.save(savedMainProduct);
            } else {
                // CREATE new product
                savedMainProduct = new Product();
                savedMainProduct.setName(dto.getName());
                savedMainProduct.setDescription(dto.getDescription());
                savedMainProduct.setPrice(dto.getPrice());
                savedMainProduct.setDiscountPrice(dto.getDiscountPrice());
                savedMainProduct.setSize(dto.getSize());
                savedMainProduct.setFragranceNotes(dto.getFragranceNotes());
                savedMainProduct.setStockQuantity(dto.getStockQuantity());
                savedMainProduct.setActive(dto.isActive());
                savedMainProduct.setFeatured(dto.isFeatured());
                savedMainProduct.setDemoProduct(false);
                savedMainProduct.setParentProductId(null);
                savedMainProduct.setQuantityMl(dto.getQuantityMl());
                savedMainProduct.setOriginalPrice(dto.getOriginalPrice());
                savedMainProduct.setDiscountPercentage(dto.getDiscountPercentage());

                if (mainImagePath != null) {
                    savedMainProduct.setImageUrl(mainImagePath);
                }

                if (dto.getCategoryId() != null) {
                    Category category = categoryService.findById(dto.getCategoryId()).orElse(null);
                    savedMainProduct.setCategory(category);
                }

                savedMainProduct = productService.save(savedMainProduct);
            }

            // ==================== HANDLE DEMO PRODUCT ====================
            if (removeDemo) {
                // Delete existing demo product
                if (existingDemoId != null) {
                    productService.delete(existingDemoId);
                } else {
                    Optional<Product> existingDemo = productService.findDemoProductByParentId(savedMainProduct.getId());
                    existingDemo.ifPresent(demo -> productService.delete(demo.getId()));
                }
                redirectAttributes.addFlashAttribute("success", "Product saved successfully! Demo product archived.");
            } else if (demoQuantityMl != null && demoQuantityMl > 0 && demoActive) {
                // Save or Update demo product
                Product demoProduct;

                if (existingDemoId != null && existingDemoId > 0) {
                    demoProduct = productService.findById(existingDemoId).orElse(new Product());
                } else {
                    Optional<Product> existingDemo = productService.findDemoProductByParentId(savedMainProduct.getId());
                    demoProduct = existingDemo.orElse(new Product());
                }

                demoProduct.setName(savedMainProduct.getName() + " - Demo Pack (" + demoQuantityMl + "ml)");
                demoProduct.setDescription(savedMainProduct.getDescription() + "\n\n[This is a trial/demo pack]");
                demoProduct.setPrice(demoPriceValue != null ? demoPriceValue : 0);
                demoProduct.setDiscountPrice(0);
                demoProduct.setSize(demoQuantityMl + "ml");
                demoProduct.setFragranceNotes(savedMainProduct.getFragranceNotes());
                demoProduct.setStockQuantity(demoStockQuantity != null ? demoStockQuantity : 0);
                demoProduct.setActive(true);
                demoProduct.setFeatured(false);
                demoProduct.setDemoProduct(true);
                demoProduct.setParentProductId(savedMainProduct.getId());
                demoProduct.setQuantityMl(demoQuantityMl);
                demoProduct.setOriginalPrice(demoPriceValue != null ? demoPriceValue : 0);
                demoProduct.setDiscountPercentage(0);
                demoProduct.setCategory(savedMainProduct.getCategory());

                // Handle demo image
                if (demoImageFile != null && !demoImageFile.isEmpty()) {
                    String demoImagePath = fileUploadUtil.uploadProductImage(demoImageFile);
                    demoProduct.setImageUrl(demoImagePath);
                } else if (demoProduct.getImageUrl() == null && savedMainProduct.getImageUrl() != null) {
                    demoProduct.setImageUrl(savedMainProduct.getImageUrl());
                }

                productService.save(demoProduct);
                redirectAttributes.addFlashAttribute("success", "Product and demo pack saved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Product saved successfully!");
            }

            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (dto.getId() != null && dto.getId() > 0) {
                return "redirect:/admin/products/edit/" + dto.getId();
            }
            return "redirect:/admin/products/add";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save product: " + e.getMessage());
            if (dto.getId() != null && dto.getId() > 0) {
                return "redirect:/admin/products/edit/" + dto.getId();
            }
            return "redirect:/admin/products/add";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product product = productService.findById(id).orElse(null);

        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found!");
            return "redirect:/admin/products";
        }

        if (product.isDemoProduct()) {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Demo product archived successfully.");
        } else {
            productService.findDemoProductByParentId(id).ifPresent(demo -> productService.delete(demo.getId()));
            productService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Product and associated demo pack archived successfully.");
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin/orders";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(orderId, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("newCategory", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("newCategory") Category category, RedirectAttributes redirectAttributes) {
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/messages")
    public String listMessages(Model model) {
        model.addAttribute("messages", contactService.findAll());
        return "admin/messages";
    }

    @PostMapping("/messages/read/{id}")
    public String markMessageAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactService.markAsRead(id);
        redirectAttributes.addFlashAttribute("success", "Message marked as read.");
        return "redirect:/admin/messages";
    }

    // ==================== VALIDATION METHOD ====================
    private void validateProductData(ProductDto dto, MultipartFile mainImageFile,
                                     Integer demoQuantityMl, Integer demoStockQuantity,
                                     Double demoPriceValue, MultipartFile demoImageFile) {

        // ========== MAIN PRODUCT VALIDATIONS ==========

        // 1. Product Name Validation
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (dto.getName().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters");
        }
        if (dto.getName().length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }

        // 2. Category Validation
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("Please select a category");
        }

        // 3. Bottle Size Validation
        if (dto.getQuantityMl() == null || dto.getQuantityMl() <= 0) {
            throw new IllegalArgumentException("Bottle size must be greater than 0");
        }
        if (dto.getQuantityMl() > 1000) {
            throw new IllegalArgumentException("Bottle size cannot exceed 1000ml");
        }

        // 4. Size Label Validation
        if (dto.getSize() == null || dto.getSize().trim().isEmpty()) {
            throw new IllegalArgumentException("Size label is required");
        }

        // 5. Original Price Validation
        if (dto.getOriginalPrice() == null || dto.getOriginalPrice() <= 0) {
            throw new IllegalArgumentException("Original price must be greater than 0");
        }
        if (dto.getOriginalPrice() > 100000) {
            throw new IllegalArgumentException("Original price cannot exceed ₹100,000");
        }

        // 6. Discount Validation (0-100 only)
        if (dto.getDiscountPercentage() != null) {
            if (dto.getDiscountPercentage() < 0) {
                throw new IllegalArgumentException("Discount cannot be negative");
            }
            if (dto.getDiscountPercentage() > 100) {
                throw new IllegalArgumentException("Discount cannot exceed 100%");
            }
        }

        // 7. Final Price Validation (auto-calculated, should be >0)
        if (dto.getPrice() <= 0) {
            throw new IllegalArgumentException("Final price must be greater than 0");
        }

        // 8. Stock Quantity Validation
        if (dto.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (dto.getStockQuantity() > 99999) {
            throw new IllegalArgumentException("Stock quantity cannot exceed 99,999");
        }

        // 9. Fragrance Notes Validation (optional, but max length)
        if (dto.getFragranceNotes() != null && dto.getFragranceNotes().length() > 500) {
            throw new IllegalArgumentException("Fragrance notes cannot exceed 500 characters");
        }

        // 10. Description Validation (optional, but max length)
        if (dto.getDescription() != null && dto.getDescription().length() > 2000) {
            throw new IllegalArgumentException("Description cannot exceed 2000 characters");
        }

        // 11. Main Product Image Validation
        if (dto.getId() == null && (mainImageFile == null || mainImageFile.isEmpty())) {
            throw new IllegalArgumentException("Product image is required");
        }

        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String contentType = mainImageFile.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png"))) {
                throw new IllegalArgumentException("Only JPG, JPEG, and PNG images are allowed for product image");
            }
            if (mainImageFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Product image size must be less than 5MB");
            }
        }

        // ========== DEMO PRODUCT VALIDATIONS ==========
        if (demoQuantityMl != null && demoQuantityMl > 0) {
            // Demo quantity must be positive
            if (demoQuantityMl <= 0) {
                throw new IllegalArgumentException("Demo quantity must be greater than 0");
            }

            // Demo quantity cannot exceed main bottle size
            if (demoQuantityMl > dto.getQuantityMl()) {
                throw new IllegalArgumentException("Demo quantity cannot exceed main bottle size (" + dto.getQuantityMl() + "ml)");
            }

            // Demo stock validation
            if (demoStockQuantity == null) {
                throw new IllegalArgumentException("Demo stock quantity is required");
            }
            if (demoStockQuantity < 0) {
                throw new IllegalArgumentException("Demo stock quantity cannot be negative");
            }
            if (demoStockQuantity > 99999) {
                throw new IllegalArgumentException("Demo stock quantity cannot exceed 99,999");
            }

            // Demo price validation
            if (demoPriceValue == null || demoPriceValue <= 0) {
                throw new IllegalArgumentException("Demo price must be greater than 0");
            }
            if (demoPriceValue > 100000) {
                throw new IllegalArgumentException("Demo price cannot exceed ₹100,000");
            }

            // Demo image validation (optional, but validate if provided)
            if (demoImageFile != null && !demoImageFile.isEmpty()) {
                String demoContentType = demoImageFile.getContentType();
                if (demoContentType == null || (!demoContentType.equals("image/jpeg") && !demoContentType.equals("image/jpg") && !demoContentType.equals("image/png"))) {
                    throw new IllegalArgumentException("Demo image must be JPG, JPEG, or PNG format");
                }
                if (demoImageFile.getSize() > 5 * 1024 * 1024) {
                    throw new IllegalArgumentException("Demo image size must be less than 5MB");
                }
            }
        }
    }
}
