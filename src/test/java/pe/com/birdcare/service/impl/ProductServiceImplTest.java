package pe.com.birdcare.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.ProductRequestDTO;
import pe.com.birdcare.dto.ProductResponseDTO;
import pe.com.birdcare.entity.Category;
import pe.com.birdcare.entity.Product;
import pe.com.birdcare.exception.ResourceNotFoundException;
import pe.com.birdcare.mapper.ProductMapper;
import pe.com.birdcare.repository.CategoryRepository;
import pe.com.birdcare.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long CATEGORY_ID = 10L;

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper pm;

    @InjectMocks private ProductServiceImpl service;

    private Category category;
    private Product existingProduct;
    private ProductResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        category = new Category("Alimento", "Comida para aves");
        existingProduct = Product.create("Semillas", "Mix", new BigDecimal("25"), 10, category);
        responseDto = ProductResponseDTO.builder()
                .id(PRODUCT_ID)
                .name("Semillas")
                .description("Mix")
                .price(new BigDecimal("25"))
                .stock(10)
                .active(true)
                .categoryName("Alimento")
                .build();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void returnsDtoWhenFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            assertThat(service.findById(PRODUCT_ID)).isEqualTo(responseDto);
        }

        @Test
        void throwsWhenNotFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(PRODUCT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("paged queries")
    class PagedQueries {

        private final Pageable pageable = PageRequest.of(0, 10);

        @Test
        void findAllDelegates() {
            when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingProduct)));
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            assertThat(service.findAll(pageable).getContent()).containsExactly(responseDto);
        }

        @Test
        void findActivesDelegatesToActiveQuery() {
            when(productRepository.findAllByActiveTrue(pageable)).thenReturn(new PageImpl<>(List.of(existingProduct)));
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            service.findActives(pageable);

            verify(productRepository).findAllByActiveTrue(pageable);
        }

        @Test
        void findByNameDelegates() {
            when(productRepository.findAllByNameContainingIgnoreCase("se", pageable)).thenReturn(new PageImpl<>(List.of(existingProduct)));
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            service.findByName("se", pageable);

            verify(productRepository).findAllByNameContainingIgnoreCase("se", pageable);
        }

        @Test
        void findByCategoryDelegates() {
            when(productRepository.findAllByCategoryId(CATEGORY_ID, pageable)).thenReturn(new PageImpl<>(List.of(existingProduct)));
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            service.findByCategory(CATEGORY_ID, pageable);

            verify(productRepository).findAllByCategoryId(CATEGORY_ID, pageable);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        private final ProductRequestDTO dto =
                new ProductRequestDTO(CATEGORY_ID, "Semillas", "Mix", new BigDecimal("25"), 10);

        @Test
        void savesProductWithResolvedCategory() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(pm.toResponse(any(Product.class))).thenReturn(responseDto);

            service.create(dto);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            Product saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo("Semillas");
            assertThat(saved.getCategory()).isEqualTo(category);
            assertThat(saved.getStock()).isEqualTo(10);
            assertThat(saved.getActive()).isTrue();
        }

        @Test
        void throwsWhenCategoryNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        private final ProductRequestDTO dto =
                new ProductRequestDTO(CATEGORY_ID, "Nuevo", "Nueva desc", new BigDecimal("30"), 10);

        @Test
        void updatesDomainFieldsAndSaves() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            service.update(PRODUCT_ID, dto);

            assertThat(existingProduct.getName()).isEqualTo("Nuevo");
            assertThat(existingProduct.getPrice()).isEqualByComparingTo("30");
            verify(productRepository).save(existingProduct);
        }

        @Test
        void throwsWhenProductNotFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(PRODUCT_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).save(any());
        }

        @Test
        void throwsWhenCategoryNotFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(PRODUCT_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).save(any());
            assertThat(existingProduct.getName()).isEqualTo("Semillas");
        }
    }

    @Nested
    @DisplayName("delete / enable")
    class DeleteEnable {

        @Test
        void deleteTurnsActiveFalseAndSaves() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));

            service.delete(PRODUCT_ID);

            assertThat(existingProduct.getActive()).isFalse();
            verify(productRepository).save(existingProduct);
        }

        @Test
        void enableTurnsActiveTrueAndSaves() {
            existingProduct.disable();
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(existingProduct)).thenReturn(existingProduct);
            when(pm.toResponse(existingProduct)).thenReturn(responseDto);

            service.enable(PRODUCT_ID);

            assertThat(existingProduct.getActive()).isTrue();
            verify(productRepository).save(existingProduct);
        }

        @Test
        void deleteThrowsWhenNotFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(PRODUCT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
