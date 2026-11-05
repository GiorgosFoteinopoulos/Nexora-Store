package com.practice.nexora.catalog;

import com.practice.nexora.common.Slug;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static com.practice.nexora.common.Text.requireText;


@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private Slug slug;

    protected Brand() {

    }

    public Brand(String name) {
        this.name = requireText(name, "brand name");
        this.slug = Slug.from(this.name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slug getSlug() {
        return slug;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Brand other)) {
            return false;
        }
        return slug.equals(other.slug);
    }

    @Override
    public int hashCode() {
        return slug.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}