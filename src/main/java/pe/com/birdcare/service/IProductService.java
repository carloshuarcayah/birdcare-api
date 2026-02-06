package pe.com.birdcare.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.ProductRequestDTO;
import pe.com.birdcare.dto.ProductResponseDTO;

public interface IProductService {

    Page<ProductResponseDTO> findAll(Pageable pageable);
    Page<ProductResponseDTO> findActives(Pageable pageable);
    ProductResponseDTO findById(Long id);

    Page<ProductResponseDTO> findByName(String name, Pageable pageable);
    Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable);

    ProductResponseDTO create(ProductRequestDTO req);
    ProductResponseDTO update(Long id, ProductRequestDTO req);

    void delete(Long id);
    ProductResponseDTO enable(Long id);
}