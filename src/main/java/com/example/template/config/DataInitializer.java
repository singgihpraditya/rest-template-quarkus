package com.example.template.config;

import com.example.template.entity.*;
import com.example.template.repository.*;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DataInitializer: seed data awal untuk profile local.
 *
 * Menggantikan Spring Boot's DataInitializer (@Profile("local") + ApplicationRunner).
 * Di Quarkus: @Observes StartupEvent dengan @Priority(1) agar berjalan sebelum
 * EndpointPermissionService yang di-load pada priority 10.
 */
@ApplicationScoped
@Slf4j
public class DataInitializer {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    TagRepository tagRepository;

    @Inject
    EndpointPermissionRepository endpointPermissionRepository;

    @ConfigProperty(name = "quarkus.profile")
    String activeProfile;

    @Transactional
    void onStart(@Observes @Priority(1) StartupEvent ev) {
        if (!"local".equals(activeProfile)) {
            log.info("Profile bukan local, skip data initialization");
            return;
        }

        // Cek apakah data sudah ada
        if (roleRepository.count() > 0) {
            log.info("Data sudah ada, skip seed data");
            return;
        }

        log.info("Mulai seed data untuk profile local...");
        seedRoles();
        seedUsers();
        seedCategories();
        seedTags();
        seedProducts();
        seedPermissions();
        log.info("Seed data selesai");
    }

    private void seedRoles() {
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        Role userRole = Role.builder().name("ROLE_USER").build();
        roleRepository.persist(List.of(adminRole, userRole));
        log.info("Role seeded: ROLE_ADMIN, ROLE_USER");
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

        User admin = User.builder()
                .username("admin")
                .password(BCrypt.hashpw("admin123", BCrypt.gensalt()))
                .email("admin@example.com")
                .fullName("Administrator")
                .active(true)
                .roles(Set.of(adminRole, userRole))
                .build();

        User user = User.builder()
                .username("user")
                .password(BCrypt.hashpw("user123", BCrypt.gensalt()))
                .email("user@example.com")
                .fullName("Regular User")
                .active(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.persist(List.of(admin, user));
        log.info("Users seeded: admin, user");
    }

    private void seedCategories() {
        Category electronics = Category.builder().name("Electronics").description("Gadgets and electronic devices").build();
        Category clothing = Category.builder().name("Clothing").description("Fashion and apparel").build();
        Category books = Category.builder().name("Books").description("Books and literature").build();
        categoryRepository.persist(List.of(electronics, clothing, books));
        log.info("Categories seeded: Electronics, Clothing, Books");
    }

    private void seedTags() {
        List<String> tagNames = List.of("smartphone", "laptop", "apple", "fashion", "java", "programming");
        tagNames.forEach(name -> tagRepository.persist(Tag.builder().name(name).build()));
        log.info("Tags seeded: {}", tagNames);
    }

    private void seedProducts() {
        Category electronics = categoryRepository.find("name", "Electronics").firstResult();
        Category clothing = categoryRepository.find("name", "Clothing").firstResult();
        Category books = categoryRepository.find("name", "Books").firstResult();

        Tag smartphone = tagRepository.find("name", "smartphone").firstResult();
        Tag laptop = tagRepository.find("name", "laptop").firstResult();
        Tag apple = tagRepository.find("name", "apple").firstResult();
        Tag fashion = tagRepository.find("name", "fashion").firstResult();
        Tag java = tagRepository.find("name", "java").firstResult();
        Tag programming = tagRepository.find("name", "programming").firstResult();

        Product iphone = Product.builder()
                .name("iPhone 15 Pro")
                .description("Apple iPhone 15 Pro with A17 chip")
                .price(new BigDecimal("19999000"))
                .stock(50)
                .publishedAt(LocalDateTime.now().minusDays(7))
                .category(electronics)
                .tags(Set.of(smartphone, apple))
                .build();

        Product macbook = Product.builder()
                .name("MacBook Pro")
                .description("Apple MacBook Pro with M3 chip")
                .price(new BigDecimal("29999000"))
                .stock(30)
                .publishedAt(LocalDateTime.now().minusDays(14))
                .category(electronics)
                .tags(Set.of(laptop, apple))
                .build();

        Product kaos = Product.builder()
                .name("Kaos Polos")
                .description("Kaos polos berbagai warna")
                .price(new BigDecimal("75000"))
                .stock(200)
                .publishedAt(LocalDateTime.now().minusDays(3))
                .category(clothing)
                .tags(Set.of(fashion))
                .build();

        Product springBook = Product.builder()
                .name("Spring Boot in Action")
                .description("Buku panduan Spring Boot dari Craig Walls")
                .price(new BigDecimal("350000"))
                .stock(100)
                .publishedAt(LocalDateTime.now().minusDays(30))
                .category(books)
                .tags(Set.of(java, programming))
                .build();

        Product cleanCode = Product.builder()
                .name("Clean Code")
                .description("A Handbook of Agile Software Craftsmanship by Robert C. Martin")
                .price(new BigDecimal("320000"))
                .stock(80)
                .publishedAt(LocalDateTime.now().minusDays(60))
                .category(books)
                .tags(Set.of(programming))
                .build();

        productRepository.persist(List.of(iphone, macbook, kaos, springBook, cleanCode));
        log.info("Products seeded: 5 produk");
    }

    private void seedPermissions() {
        List<EndpointPermission> permissions = List.of(
                perm(1, "*", "/error", null, "Error handler"),
                perm(2, "*", "/api/auth/**", null, "Login & register publik"),
                perm(3, "GET", "/q/swagger-ui/**", null, "Swagger UI"),
                perm(4, "GET", "/q/openapi", null, "OpenAPI spec"),
                perm(5, "GET", "/q/health/**", null, "Health check"),
                perm(6, "GET", "/q/metrics", null, "Metrics"),
                perm(7, "*", "/h2-console/**", null, "H2 Console (local only)"),
                perm(10, "GET", "/api/products/**", null, "Baca produk (publik)"),
                perm(11, "GET", "/api/categories/**", null, "Baca kategori (publik)"),
                perm(12, "GET", "/api/external/**", null, "External API (publik)"),
                perm(13, "GET", "/api/diagnostic/**", null, "Diagnostic (publik)"),
                perm(20, "POST", "/api/categories", "ROLE_ADMIN", "Tambah kategori (admin)"),
                perm(21, "PUT", "/api/categories/**", "ROLE_ADMIN", "Update kategori (admin)"),
                perm(22, "DELETE", "/api/categories/**", "ROLE_ADMIN", "Hapus kategori (admin)"),
                perm(23, "POST", "/api/products", "ROLE_ADMIN", "Tambah produk (admin)"),
                perm(24, "PUT", "/api/products/**", "ROLE_ADMIN", "Update produk (admin)"),
                perm(25, "DELETE", "/api/products/**", "ROLE_ADMIN", "Hapus produk (admin)"),
                perm(26, "*", "/api/permissions/**", "ROLE_ADMIN", "Kelola permission (admin)"),
                perm(27, "*", "/api/cache/**", "ROLE_ADMIN", "Cache management (admin)"),
                perm(30, "*", "/api/files/**", "ROLE_USER", "Upload/download file"),
                perm(999, "*", "/api/**", "ROLE_USER", "Default fallback semua /api/**")
        );

        endpointPermissionRepository.persist(permissions);
        log.info("Permissions seeded: {} rules", permissions.size());
    }

    private EndpointPermission perm(int sortOrder, String method, String pattern, String role, String desc) {
        return EndpointPermission.builder()
                .sortOrder(sortOrder)
                .httpMethod(method)
                .urlPattern(pattern)
                .requiredRole(role)
                .description(desc)
                .active(true)
                .build();
    }
}
