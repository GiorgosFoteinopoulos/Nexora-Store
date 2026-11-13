package com.practice.nexora.catalog;

import com.practice.nexora.security.SecurityConfig;
import org.junit.jupiter.api.Test;
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


@WebMvcTest(BrandController.class)
@Import(SecurityConfig.class)
class BrandControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BrandService brandService;

    @Test
    void returnsTheBrandsAsJson() throws Exception {
        when(brandService.listBrands()).thenReturn(List.of(
                new BrandResponse("Lumio", "lumio"),
                new BrandResponse("Nexora", "nexora")));

        mvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Lumio"))
                .andExpect(jsonPath("$[1].slug").value("nexora"));
    }

    @Test
    void returnsAnEmptyJsonArrayWhenThereAreNoBrands() throws Exception {
        when(brandService.listBrands()).thenReturn(List.of());

        mvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}