package com.practice.nexora.catalog;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class SkuConverter implements AttributeConverter<Sku, String> {

    @Override
    public String convertToDatabaseColumn(Sku sku) {
        return sku == null ? null : sku.value();
    }

    @Override
    public Sku convertToEntityAttribute(String value) {
        return value == null ? null : new Sku(value);
    }
}
