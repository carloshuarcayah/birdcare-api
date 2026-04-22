package pe.com.birdcare.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.CategoryRequestDTO;
import pe.com.birdcare.dto.CategoryResponseDTO;
import pe.com.birdcare.entity.Category;

public interface ICategoryService {
    //ADMIN
    Page<CategoryResponseDTO> findAll(Pageable pageable);
    void delete(Long id);
    CategoryResponseDTO enable(Long id);
    CategoryResponseDTO create (CategoryRequestDTO req);
    CategoryResponseDTO update (Long id,CategoryRequestDTO req);

    //CUSTOMER - GENERAL
    Page<CategoryResponseDTO> findActiveCategories(Pageable pageable);
    CategoryResponseDTO findById(Long id);
    Page<CategoryResponseDTO> findByName(String name, Pageable pageable);
}
