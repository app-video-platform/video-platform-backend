package com.myproject.video.video_platform.entity.products.course;

import com.myproject.video.video_platform.entity.products.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "course_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseProduct extends Product {

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    private Set<com.myproject.video.video_platform.entity.products.course_product.CourseSection> sections = new LinkedHashSet<>();
}
