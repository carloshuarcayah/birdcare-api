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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pe.com.birdcare.dto.CategoryRequestDTO;
import pe.com.birdcare.dto.CategoryResponseDTO;
import pe.com.birdcare.entity.Category;
import pe.com.birdcare.exception.ConflictException;
import pe.com.birdcare.exception.ResourceNotFoundException;
import pe.com.birdcare.mapper.CategoryMapper;
import pe.com.birdcare.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    private static final Long CATEGORY_ID = 1L;
    private static final String NAME = "Alimento";
    private static final String DESCRIPTION = "Comida para aves";

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper mapper;

    @InjectMocks private CategoryServiceImpl service;

    private Category existingCategory;
    private CategoryResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        existingCategory = new Category(NAME, DESCRIPTION);
        responseDto = new CategoryResponseDTO(CATEGORY_ID, NAME, DESCRIPTION, true);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void returnsDtoWhenFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory));
            when(mapper.toResponse(existingCategory)).thenReturn(responseDto);

            assertThat(service.findById(CATEGORY_ID)).isEqualTo(responseDto);
        }

        @Test
        void throwsWhenNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(CATEGORY_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(CATEGORY_ID.toString());
        }
    }

    @Nested
    @DisplayName("findAll / findActiveCategories / findByName")
    class PagedQueries {

        private final Pageable pageable = PageRequest.of(0, 10);

        @Test
        void findAllMapsEachEntity() {
            when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingCategory)));
            when(mapper.toResponse(existingCategory)).thenReturn(responseDto);

            Page<CategoryResponseDTO> result = service.findAll(pageable);

            assertThat(result.getContent()).containsExactly(responseDto);
        }

        @Test
        void findActiveDelegatesToActiveQuery() {
            when(categoryRepository.findAllByActiveTrue(pageable))
                    .thenReturn(new PageImpl<>(List.of(existingCategory)));
            when(mapper.toResponse(existingCategory)).thenReturn(responseDto);

            service.findActiveCategories(pageable);

            verify(categoryRepository).findAllByActiveTrue(pageable);
            verify(categoryRepository, never()).findAll(pageable);
        }

        @Test
        void findByNameDelegatesToNameQuery() {
            when(categoryRepository.findAllByNameContainingIgnoreCase("ali", pageable))
                    .thenReturn(new PageImpl<>(List.of(existingCategory)));
            when(mapper.toResponse(existingCategory)).thenReturn(responseDto);

            service.findByName("ali", pageable);

            verify(categoryRepository).findAllByNameContainingIgnoreCase("ali", pageable);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        private final CategoryRequestDTO dto = new CategoryRequestDTO(NAME, DESCRIPTION);

        @Test
        void savesNewCategoryWhenNameIsUnique() {
            when(categoryRepository.existsByName(NAME)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toResponse(any(Category.class))).thenReturn(responseDto);

            service.create(dto);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(captor.capture());
            Category saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo(NAME);
            assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(saved.getActive()).isTrue();
        }

        @Test
        void throwsConflictWhenNameExists() {
            when(categoryRepository.existsByName(NAME)).thenReturn(true);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(NAME);

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        private final CategoryRequestDTO dto = new CategoryRequestDTO("Juguetes", "Para distraerse");

        @Test
        void updatesDomainFieldsAndSaves() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.existsByNameAndIdNot("Juguetes", CATEGORY_ID)).thenReturn(false);
            when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);
            when(mapper.toResponse(existingCategory)).thenReturn(responseDto);

            service.update(CATEGORY_ID, dto);

            assertThat(existingCategory.getName()).isEqualTo("Juguetes");
            assertThat(existingCategory.getDescription()).isEqualTo("Para distraerse");
            verify(categoryRepository).save(existingCategory);
        }

        @Test
        void throwsWhenNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(CATEGORY_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        void throwsConflictWhenAnotherCategoryHasSameName() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.existsByNameAndIdNot("Juguetes", CATEGORY_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.update(CATEGORY_ID, dto))
                    .isInstanceOf(ConflictException.class);

            assertThat(existingCategory.getName()).isEqualTo(NAME);
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete / enable")
    class DeleteEnable {

        @Test
        void deleteTurnsActiveFalseAndSaves() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory));

            service.delete(CATEGORY_ID);

            assertThat(existingCategory.getActive()).isFalse();
            verify(categoryRepository).save(existingCategory);
        }

        @Test
        void enableTurnsActiveTrueAndSaves() {
            existingCategory.disable();
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);
            when(mapper.toResponse(existingCategory)).thenReturn(responseDto);

            service.enable(CATEGORY_ID);

            assertThat(existingCategory.getActive()).isTrue();
            verify(categoryRepository).save(existingCategory);
        }

        @Test
        void deleteThrowsWhenNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(CATEGORY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        void enableThrowsWhenNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.enable(CATEGORY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).save(any());
        }
    }
}