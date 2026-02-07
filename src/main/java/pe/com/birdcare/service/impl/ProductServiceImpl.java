package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.com.birdcare.dto.ProductRequestDTO;
import pe.com.birdcare.dto.ProductResponseDTO;
import pe.com.birdcare.entity.Category;
import pe.com.birdcare.entity.Product;
import pe.com.birdcare.mapper.ProductMapper;
import pe.com.birdcare.repository.CategoryRepository;
import pe.com.birdcare.repository.ProductRepository;
import pe.com.birdcare.service.IProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductMapper pm;

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(pm::toResponse);
    }

    @Override
    public Page<ProductResponseDTO> findActives(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable).map(pm::toResponse);
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        return productRepository.findById(id).map(pm::toResponse).orElseThrow(()->new RuntimeException("Product not found with id: "+id));
    }

    @Override
    public Page<ProductResponseDTO> findByName(String name, Pageable pageable) {
        return productRepository.findAllByNameContainingIgnoreCase(name,pageable).map(pm::toResponse);
    }

    @Override
    public Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findAllByCategoryId(categoryId, pageable).map(pm::toResponse);
    }

    @Override
    public ProductResponseDTO create(ProductRequestDTO req) {
        Category existingCategory = getCategoryOrThrow(req.categoryId());

        Product product = pm.toEntity(req);

        product.setCategory(existingCategory);

        return pm.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO update(Long id, ProductRequestDTO req) {
        Product existingProduct = getProductOrThrow(id);
        pm.updateProduct(req,existingProduct);

        Category existingCategory= getCategoryOrThrow(req.categoryId());

        existingProduct.setCategory(existingCategory);

        return pm.toResponse(productRepository.save(existingProduct));
    }

    @Override
    public void delete(Long id) {
        Product product = getProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public ProductResponseDTO enable(Long id) {
        Product product = getProductOrThrow(id);
        product.setActive(true);

        return pm.toResponse(productRepository.save(product));
    }

    private Category getCategoryOrThrow(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    private Product getProductOrThrow(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
