package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.birdcare.dto.CategoryRequestDTO;
import pe.com.birdcare.dto.CategoryResponseDTO;
import pe.com.birdcare.entity.Category;
import pe.com.birdcare.exception.ConflictException;
import pe.com.birdcare.exception.ResourceNotFoundException;
import pe.com.birdcare.mapper.CategoryMapper;
import pe.com.birdcare.repository.CategoryRepository;
import pe.com.birdcare.service.ICategoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public Page<CategoryResponseDTO> findActiveCategories(Pageable pageable) {
        return categoryRepository.findAllByActiveTrue(pageable).map(mapper::toResponse);
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        return mapper.toResponse(getCategoryOrThrow(id));
    }

    @Override
    public Page<CategoryResponseDTO> findByName(String name, Pageable pageable) {
        return categoryRepository.findAllByNameContainingIgnoreCase(name,pageable).map(mapper::toResponse);
    }

    @Transactional
    @Override
    public CategoryResponseDTO create(CategoryRequestDTO req) {
        if (categoryRepository.existsByName(req.name())){
            throw new ConflictException("Category with the name '" + req.name() + "' already exists.");
        }

        Category category = new Category(req.name(), req.description());

        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO req) {

        Category category = getCategoryOrThrow(id);

        if (categoryRepository.existsByNameAndIdNot(req.name(), id)) {
            throw new ConflictException("This name is already used by another category");
        }

        category.update(req.name(), req.description());

        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public CategoryResponseDTO delete(Long id) {
        Category existingCategory = getCategoryOrThrow(id);
        existingCategory.disable();
        return mapper.toResponse(categoryRepository.save(existingCategory));
    }

    @Transactional
    @Override
    public CategoryResponseDTO enable(Long id) {
        Category existingCategory = getCategoryOrThrow(id);
        existingCategory.enable();
        return mapper.toResponse(categoryRepository.save(existingCategory));
    }

    private Category getCategoryOrThrow(Long id){
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
