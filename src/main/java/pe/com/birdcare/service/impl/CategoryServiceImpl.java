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
        return categoryRepository.findAllByActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        return categoryRepository.findById(id).map(this::toDTO).orElseThrow(RuntimeException::new);
    }

    @Override
    public Page<CategoryResponseDTO> findByName(String name, Pageable pageable) {
        return categoryRepository.findAllByNameContainingIgnoreCase(name,pageable).map(this::toDTO);
    }

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO req) {
        Category category = Category.builder()
                .name(req.name())
                .description(req.description()).build();

        return toDTO(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO req) {
        Category existente = categoryRepository.findById(id).orElseThrow(RuntimeException::new);

        existente.setName(req.name());
        existente.setDescription(req.description());

        return toDTO(categoryRepository.save(existente));
    }

    @Override
    public void delete(Long id) {
        Category existente = categoryRepository.findById(id).orElseThrow(RuntimeException::new);

        if(existente.getActive()){
            existente.setActive(false);
            categoryRepository.save(existente);
        }
    }

    @Override
    public CategoryResponseDTO enable(Long id) {
        Category existente = categoryRepository.findById(id).orElseThrow(RuntimeException::new);

        if(!existente.getActive()){
            existente.setActive(true);
            categoryRepository.save(existente);
        }
        return toDTO(existente);
    }

    private CategoryResponseDTO toDTO(Category category){
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive()).build();
    }
}
