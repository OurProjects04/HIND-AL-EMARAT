package com.hindalemarat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double price;
    private double discountPrice;
    private String size;

    @Column(columnDefinition = "TEXT")
    private String fragranceNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private int stockQuantity;
    private String imageUrl;

    private boolean active = true;
    private boolean featured = false;

    private LocalDateTime createdAt;

    // ==================== DEMO/MINI PACK FIELDS ====================
    
    // Flag to indicate if this is a demo/mini pack product
    @Column(name = "is_demo_product", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isDemoProduct = false;

    // Parent product ID if this is a demo product
    @Column(name = "parent_product_id")
    private Long parentProductId;

    // Quantity in ML (used for both main and demo products)
    @Column(name = "quantity_ml")
    private Integer quantityMl;

    // Original price before discount (used for demo price calculation)
    @Column(name = "original_price")
    private Double originalPrice;

    // Discount percentage (0 for demo products)
    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    public Product() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFragranceNotes() {
        return fragranceNotes;
    }

    public void setFragranceNotes(String fragranceNotes) {
        this.fragranceNotes = fragranceNotes;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== DEMO PRODUCT GETTERS/SETTERS ====================

    public boolean isDemoProduct() {
        return isDemoProduct;
    }

    public void setDemoProduct(boolean demoProduct) {
        isDemoProduct = demoProduct;
    }

    public Long getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(Long parentProductId) {
        this.parentProductId = parentProductId;
    }

    public Integer getQuantityMl() {
        return quantityMl;
    }

    public void setQuantityMl(Integer quantityMl) {
        this.quantityMl = quantityMl;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
