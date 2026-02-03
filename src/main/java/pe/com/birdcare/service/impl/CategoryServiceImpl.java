package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.com.birdcare.dto.CategoryRequestDTO;
import pe.com.birdcare.dto.CategoryResponseDTO;
import pe.com.birdcare.entity.Category;
import pe.com.birdcare.repository.CategoryRepository;
import pe.com.birdcare.service.ICategoryService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public Page<CategoryResponseDTO> findActives(Pageable pageable) {
        return null;
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        return null;
    }

    @Override
    public Page<CategoryResponseDTO> findByName(String name, Pageable pageable) {
        return null;
    }

    @Override
    public CategoryResponseDTO add(CategoryRequestDTO req) {
        return null;
    }

    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO req) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public CategoryResponseDTO enable(Long id) {
        return null;
    }

    private CategoryResponseDTO toDTO(Category category){
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .nombre(category.getName())
                .description(category.getDescription())
                .active(category.getActive()).build();
    }
}
