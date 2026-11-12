package com.practice.nexora.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandRepository brands;

    @InjectMocks
    private BrandService service;

    @Test
    void turnsBrandsIntoResponses() {
        when(brands.findAll(any(Sort.class)))
        .thenReturn(List.of(new Brand("Lumio"), new Brand("Nexora Labs")));

        List<BrandResponse> result = service.listBrands();

        assertThat(result).containsExactly(
                new BrandResponse("Lumio", "lumio"),
                new BrandResponse("Nexora Labs", "nexora-labs")
        );
    }


    @Test
    void asksTheRepositoryToSortByName() {
        when(brands.findAll(any(Sort.class))).thenReturn(List.of());

        service.listBrands();

        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(brands).findAll(sort.capture()); // was findAll(Sort) called? catch the argument
        assertThat(sort.getValue()).isEqualTo(Sort.by("name"));
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoBrands() {
        when(brands.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(service.listBrands()).isEmpty();
    }
}