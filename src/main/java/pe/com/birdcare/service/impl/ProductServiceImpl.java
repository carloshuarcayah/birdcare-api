package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.com.birdcare.dto.ProductRequestDTO;
import pe.com.birdcare.dto.ProductResponseDTO;
import pe.com.birdcare.entity.Product;
import pe.com.birdcare.repository.ProductRepository;
import pe.com.birdcare.service.IProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public Page<ProductResponseDTO> findActives(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        return productRepository.findById(id).map(this::toDTO).orElseThrow(()->new RuntimeException("Product not found with id: "+id));
    }

    @Override
    public Page<ProductResponseDTO> findByName(String name, Pageable pageable) {
        return productRepository.findAllByNameContainingIgnoreCase(name,pageable).map(this::toDTO);
    }

    @Override
    public Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findAllByCategoryId(categoryId, pageable).map(this::toDTO);
    }

    @Override
    public ProductResponseDTO create(ProductRequestDTO req) {
        return null;
    }

    @Override
    public ProductResponseDTO update(Long id, ProductRequestDTO req) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public ProductResponseDTO enable(Long id) {
        return null;
    }

    private ProductResponseDTO toDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.getActive())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
