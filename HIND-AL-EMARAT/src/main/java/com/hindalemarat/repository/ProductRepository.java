package com.hindalemarat.repository;

import com.hindalemarat.entity.Category;
import com.hindalemarat.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueAndIsDemoProductFalse();
    List<Product> findByActiveTrueAndFeaturedTrue();
    List<Product> findByActiveTrueAndFeaturedTrueAndIsDemoProductFalse();
    List<Product> findByCategoryAndActiveTrue(Category category);
    List<Product> findByCategoryAndActiveTrueAndIsDemoProductFalse(Category category);
    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String name);
    List<Product> findByActiveTrueAndIsDemoProductFalseAndNameContainingIgnoreCase(String name);
    Page<Product> findByActiveTrue(Pageable pageable);
    long countByActiveTrue();

    // Demo product queries
    Optional<Product> findByParentProductIdAndIsDemoProductTrue(Long parentProductId);
    List<Product> findByIsDemoProductFalse();
    List<Product> findByParentProductIdAndActive(Long parentProductId, boolean active);
}
