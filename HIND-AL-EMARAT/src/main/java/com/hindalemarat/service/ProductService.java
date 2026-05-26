package com.hindalemarat.service;

import com.hindalemarat.dto.ProductDto;
import com.hindalemarat.entity.Category;
import com.hindalemarat.entity.Product;
import com.hindalemarat.repository.CategoryRepository;
import com.hindalemarat.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findActive() {
        return productRepository.findByActiveTrueAndIsDemoProductFalse();
    }

    public List<Product> findFeatured() {
        return productRepository.findByActiveTrueAndFeaturedTrueAndIsDemoProductFalse();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategoryAndActiveTrueAndIsDemoProductFalse(category);
    }

    public List<Product> search(String keyword) {
        return productRepository.findByActiveTrueAndIsDemoProductFalseAndNameContainingIgnoreCase(keyword);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product saveFromDto(ProductDto dto) {
        Product product;
        if (dto.getId() != null) {
            product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        } else {
            product = new Product();
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setSize(dto.getSize());
        product.setFragranceNotes(dto.getFragranceNotes());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Always soft-delete in ecommerce to preserve referential integrity and order history.
        product.setActive(false);
        product.setFeatured(false);
        productRepository.save(product);
    }

    public long count() {
        return productRepository.countByActiveTrue();
    }

    // ==================== DEMO PRODUCT METHODS ====================

    /**
     * Find demo product for a main product
     */
    public Optional<Product> findDemoProductByParentId(Long parentProductId) {
        return productRepository.findByParentProductIdAndActive(parentProductId, true).stream()
            .filter(Product::isDemoProduct)
            .findFirst();
    }

    /**
     * Calculate demo product price based on main product pricing
     * Formula: (originalPrice / mainQuantityMl) * demoQuantityMl
     */
    public double calculateDemoPrice(double originalPrice, int mainQuantityMl, int demoQuantityMl) {
        if (mainQuantityMl <= 0 || demoQuantityMl <= 0) {
            throw new IllegalArgumentException("Quantity values must be greater than 0");
        }
        double pricePerMl = originalPrice / mainQuantityMl;
        return Math.round(pricePerMl * demoQuantityMl * 100.0) / 100.0;
    }

    /**
     * Save main product with optional demo product in a transaction
     */
    @Transactional
    public Product saveMainProductWithDemo(Product mainProduct, Product demoProduct) {
        // First save the main product
        Product savedMain = productRepository.save(mainProduct);

        // If demo product is provided, link it and save
        if (demoProduct != null) {
            demoProduct.setParentProductId(savedMain.getId());
            demoProduct.setDemoProduct(true);
            demoProduct.setDiscountPercentage(0); // Demo products have no discount
            productRepository.save(demoProduct);
        }

        return savedMain;
    }

    /**
     * Get main product with its demo product (if exists)
     */
    public ProductWithDemo getMainProductWithDemo(Long mainProductId) {
        Optional<Product> main = productRepository.findById(mainProductId);
        if (main.isEmpty() || main.get().isDemoProduct()) {
            return null;
        }

        Product mainProduct = main.get();
        Optional<Product> demo = findDemoProductByParentId(mainProductId);

        return new ProductWithDemo(mainProduct, demo.orElse(null));
    }

    /**
     * Helper class to hold main product and its associated demo product
     */
    public static class ProductWithDemo {
        public final Product mainProduct;
        public final Product demoProduct;

        public ProductWithDemo(Product mainProduct, Product demoProduct) {
            this.mainProduct = mainProduct;
            this.demoProduct = demoProduct;
        }

        public boolean hasDemoProduct() {
            return demoProduct != null;
        }
    }
}
