package com.practice.nexora;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocumentationTest {

    private static final String LIST = "$.paths['/api/products'].get";

    @Autowired
    private MockMvc mvc;

    @Test
    void describesEveryEndpoint() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Nexora Store API"))
                .andExpect(jsonPath("$.paths['/api/brands'].get").exists())
                .andExpect(jsonPath(LIST).exists())
                .andExpect(jsonPath("$.paths['/api/products/{slug}'].get").exists());
    }

    @Test
    void theListDocumentsItsFiltersAndPagingButNoSort() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(LIST + ".parameters[*].name",
                        hasItems("category", "brand", "minPrice", "maxPrice", "q", "page", "size")))
                .andExpect(jsonPath(LIST + ".parameters[*].name", not(hasItem("sort")))); // the service ignores sort
    }

    @Test
    void onlyTheProductPageDocumentsA404() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/products/{slug}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/brands'].get.responses['404']").doesNotExist());
    }

    @Test
    void servesSwaggerUi() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); 
    }
}