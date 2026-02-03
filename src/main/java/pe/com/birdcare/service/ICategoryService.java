package pe.com.birdcare.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.CategoryRequestDTO;
import pe.com.birdcare.dto.CategoryResponseDTO;
import pe.com.birdcare.entity.Category;

public interface ICategoryService {
    Page<CategoryResponseDTO> findAll(Pageable pageable);
    Page<CategoryResponseDTO> findActives(Pageable pageable);
    CategoryResponseDTO findById(Long id);
    Page<CategoryResponseDTO> findByName(String name, Pageable pageable);
    CategoryResponseDTO add (CategoryRequestDTO req);
    CategoryResponseDTO update (CategoryRequestDTO req);
    void delete(Long id);
    CategoryResponseDTO enable(Long id);
}
