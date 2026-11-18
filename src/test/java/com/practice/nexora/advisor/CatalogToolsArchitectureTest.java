package com.practice.nexora.advisor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogToolsArchitectureTest {

    @Test
    void nexaHasOnlyTheThreeReadOnlyTools() {
        List<String> tools = Arrays.stream(CatalogTools.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Tool.class))
                .map(Method::getName)
                .sorted()
                .toList();

        assertThat(tools).containsExactly("compareProducts", "getProductDetails", "searchProducts");
    }
}
