package pe.com.birdcare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.birdcare.entity.Product;


@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    Page<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findAllByActiveTrue(Pageable pageable);

    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findAllByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
}
