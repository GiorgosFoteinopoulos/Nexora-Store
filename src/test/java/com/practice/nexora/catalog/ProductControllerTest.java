package com.practice.nexora.catalog;

import com.practice.nexora.common.NotFoundException;
import com.practice.nexora.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.practice.nexora.common.PageResponse;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService productService;

    private static ProductResponse phoneResponse() {
        ProductSpecs specs = new ProductSpecs.PhoneSpecs(6.1, 4500, true);
        PriceResponse price = new PriceResponse("799.00", "EUR");
        return new ProductResponse("nexora-phone-15", "Nexora Phone 15", Category.PHONE,
                new BrandResponse("Nexora", "nexora"), "A phone", price, true, specs.summary(), specs,
                List.of(new ProductResponse.Variant("NX-PH15-128-BLK", "Black", 128, price, true)));
    }

    @Test
    void returnsTheProductAsJson() throws Exception {
        when(productService.getProduct("nexora-phone-15")).thenReturn(phoneResponse());

        mvc.perform(get("/api/products/{slug}", "nexora-phone-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nexora Phone 15"))
                .andExpect(jsonPath("$.category").value("PHONE"))
                .andExpect(jsonPath("$.brand.slug").value("nexora"))
                .andExpect(jsonPath("$.startingPrice.amount").value("799.00"))
                .andExpect(jsonPath("$.specs.batteryMah").value(4500))
                .andExpect(jsonPath("$.variants[0].sku").value("NX-PH15-128-BLK"));
    }

    @Test
    void anUnknownProductIs404ProblemDetail() throws Exception {
        when(productService.getProduct("nope")).thenThrow(new NotFoundException("No product with slug 'nope'"));


        mvc.perform(get("/api/products/{slug}", "nope"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No product with slug 'nope'"));
    }

    private static ProductSummaryResponse phoneSummary() {
        return new ProductSummaryResponse("nexora-phone-15", "Nexora Phone 15", Category.PHONE,
                new BrandResponse("Nexora", "nexora"), new PriceResponse("799.00", "EUR"), true,
                "6.1\" screen, 4500 mAh, 5G");
    }

    @Test
    void listsProductsAsAPage() throws Exception {
        when(productService.listProducts(any(), any()))
                .thenReturn(new PageResponse<>(List.of(phoneSummary()), 0, 20, 1, 1));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("nexora-phone-15"))
                .andExpect(jsonPath("$.content[0].variants").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).listProducts(eq(ProductFilter.none()), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void passesQueryParametersAsFilterAndPage() throws Exception {
        mvc.perform(get("/api/products")
                .param("category", "PHONE")
                .param("brand", "nexora")
                .param("minPrice", "300")
                .param("q", "pro")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).listProducts(
                eq(new ProductFilter(Category.PHONE, "nexora", new BigDecimal("300"), null, "pro")),
                pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void aPriceThatIsNotANumberisA400() throws Exception {
        mvc.perform(get("/api/products").param("minPrice", "cheap"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
}
