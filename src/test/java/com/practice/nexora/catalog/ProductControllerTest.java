package com.practice.nexora.catalog;

import com.practice.nexora.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
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
}
