package com.example.template.repository;

import com.example.template.entity.Category;
import com.example.template.repository.projection.CategoryProductCountProjection;
import com.example.template.repository.projection.CategoryProductCountResult;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoryRepository implements PanacheRepository<Category> {

    @Inject
    EntityManager em;

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    public Optional<Category> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public PanacheQuery<Category> findByNameContainingIgnoreCase(String keyword) {
        return find("LOWER(name) LIKE LOWER(?1)", Sort.by("name").ascending(), "%" + keyword + "%");
    }

    /**
     * Native query: jumlah produk per kategori.
     * Mengembalikan projection interface via concrete result class.
     */
    @SuppressWarnings("unchecked")
    public List<CategoryProductCountProjection> findCategoriesWithProductCount() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT c.id, c.name, c.description, COUNT(p.id) AS product_count " +
                "FROM categories c LEFT JOIN products p ON p.category_id = c.id " +
                "GROUP BY c.id, c.name, c.description ORDER BY c.name"
        ).getResultList();

        return rows.stream()
                .map(row -> (CategoryProductCountProjection) new CategoryProductCountResult(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()
                ))
                .toList();
    }
}
