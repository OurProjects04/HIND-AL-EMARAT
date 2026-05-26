package com.hindalemarat.config;

import com.hindalemarat.entity.Category;
import com.hindalemarat.entity.Product;
import com.hindalemarat.entity.Role;
import com.hindalemarat.entity.User;
import com.hindalemarat.repository.CategoryRepository;
import com.hindalemarat.repository.ProductRepository;
import com.hindalemarat.repository.RoleRepository;
import com.hindalemarat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${APP_SEED_ADMIN_EMAIL:}")
    private String seedAdminEmail;

    @Value("${APP_SEED_ADMIN_PASSWORD:}")
    private String seedAdminPassword;

    @Value("${APP_SEED_USER_EMAIL:}")
    private String seedUserEmail;

    @Value("${APP_SEED_USER_PASSWORD:}")
    private String seedUserPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            return;
        }

        // Seed Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role("ROLE_ADMIN");
            return roleRepository.save(role);
        });

        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role("ROLE_USER");
            return roleRepository.save(role);
        });

        // Seed Users
        if (!seedAdminEmail.isBlank() && !seedAdminPassword.isBlank() && !userRepository.existsByEmail(seedAdminEmail)) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail(seedAdminEmail);
            admin.setPassword(passwordEncoder.encode(seedAdminPassword));
            admin.setPhone("9876543210");
            admin.setAddress("Luxury Perfume HQ, Marine Drive");
            admin.setCity("Mumbai");
            admin.setState("Maharashtra");
            admin.setPincode("400001");
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);
            userRepository.save(admin);
        }

        if (!seedUserEmail.isBlank() && !seedUserPassword.isBlank() && !userRepository.existsByEmail(seedUserEmail)) {
            User testUser = new User();
            testUser.setFirstName("Demo");
            testUser.setLastName("Customer");
            testUser.setEmail(seedUserEmail);
            testUser.setPassword(passwordEncoder.encode(seedUserPassword));
            testUser.setPhone("9876543211");
            testUser.setAddress("Sea Breeze Apartments, Juhu");
            testUser.setCity("Mumbai");
            testUser.setState("Maharashtra");
            testUser.setPincode("400049");
            
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            testUser.setRoles(roles);
            userRepository.save(testUser);
        }

        // Seed Categories
        Category oud = seedCategory("Oud Collection", "Precious agarwood formulations blending premium Arabian and Indian notes.");
        Category attar = seedCategory("Attar Collection", "Traditional non-alcoholic perfume concentrates crafted from natural botanical extracts.");
        Category musk = seedCategory("Musk Collection", "Sophisticated and long-lasting animalic and white musk signature scents.");
        Category floral = seedCategory("Floral Collection", "Elegant and fresh compositions utilizing rich jasmine, rose, and saffron elements.");

        // Seed Products
        if (productRepository.count() == 0) {
            seedProduct("Royal Oud Majesty", 
                "An opulent blend of finest Assam Oud, Cambodian wood, and light sweet honey notes, designed for royalty.", 
                4999.00, 3999.00, "50ml", "Top: Bergamot, Honey; Heart: Rose, Patchouli; Base: Indian Oud, Amber", 
                oud, 15, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Royal+Oud+Majesty", true, true);

            seedProduct("Arabian Oud Nights", 
                "An intense and mysterious fragrance combining smoky incense with dark oud wood and deep spices.", 
                6999.00, 5499.00, "100ml", "Top: Incense, Saffron; Heart: Myrrh, Leather; Base: Cambodian Oud, Sandalwood", 
                oud, 10, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Arabian+Oud+Nights", true, true);

            seedProduct("Pure Gulab Attar", 
                "A rare distillation of high-quality Indian Damask roses in a base of fine sandalwood oil. 100% natural.", 
                2499.00, 1999.00, "12ml", "Top: Damask Rose; Heart: Bulgarian Rose; Base: Sandalwood Oil", 
                attar, 25, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Pure+Gulab+Attar", true, true);

            seedProduct("Kashmiri Saffron Attar", 
                "A rich, warm, and highly aromatic perfume oil extracted from Kashmiri saffron threads.", 
                3499.00, 0, "12ml", "Top: Kashmiri Saffron; Heart: Cardamom, Clove; Base: Amber, Musk", 
                attar, 20, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Kashmiri+Saffron+Attar", true, false);

            seedProduct("White Musk Elegance", 
                "A clean, soft, powdery fragrance that sits close to the skin, offering a timeless essence of purity.", 
                1999.00, 1499.00, "50ml", "Top: Aldehydes, Lily; Heart: Jasmine, Ylang-Ylang; Base: White Musk, Vanilla", 
                musk, 30, "https://placehold.co/400x500/1a1a2e/c9a84c?text=White+Musk+Elegance", true, true);

            seedProduct("Dark Musk Intense", 
                "A bold and heavy black musk perfume with undertones of dark woody elements and warm amber.", 
                2999.00, 0, "100ml", "Top: Spices; Heart: Cedarwood, Vetiver; Base: Black Musk, Oakmoss, Amber", 
                musk, 18, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Dark+Musk+Intense", true, false);

            seedProduct("Jasmine Dreams", 
                "A fresh floral bouquet honoring the sacred Indian Madurai Jasmine, sparkling with light citrus top notes.", 
                1799.00, 1299.00, "50ml", "Top: Lemon, Neroli; Heart: Madurai Jasmine, Orange Blossom; Base: Musk", 
                floral, 22, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Jasmine+Dreams", true, false);

            seedProduct("Rose Garden Premium", 
                "A romantic and refreshing EDP capturing the essence of roses in full bloom after a summer monsoon.", 
                2299.00, 0, "50ml", "Top: Green Notes, Pink Pepper; Heart: Rose, Peony; Base: Cedarwood, White Musk", 
                floral, 20, "https://placehold.co/400x500/1a1a2e/c9a84c?text=Rose+Garden+Premium", true, false);
        }
    }

    private Category seedCategory(String name, String description) {
        return categoryRepository.findByActiveTrue().stream()
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseGet(() -> {
                Category cat = new Category();
                cat.setName(name);
                cat.setDescription(description);
                cat.setImageUrl("https://placehold.co/400x300/1a1a2e/c9a84c?text=" + name.replace(" ", "+"));
                cat.setActive(true);
                return categoryRepository.save(cat);
            });
    }

    private void seedProduct(String name, String description, double price, double discountPrice, 
                             String size, String notes, Category category, int stock, String imageUrl, 
                             boolean active, boolean featured) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setDiscountPrice(discountPrice);
        p.setSize(size);
        p.setFragranceNotes(notes);
        p.setCategory(category);
        p.setStockQuantity(stock);
        p.setImageUrl(imageUrl);
        p.setActive(active);
        p.setFeatured(featured);
        productRepository.save(p);
    }
}
