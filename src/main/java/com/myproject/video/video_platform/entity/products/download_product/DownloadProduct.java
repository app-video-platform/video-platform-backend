package com.myproject.video.video_platform.entity.products.download_product;

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

/**
 * Download Product extending from Product.
 * In TABLE_PER_CLASS strategy, this concrete class gets its own table containing all Product fields.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "download_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DownloadProduct extends Product {
    @OneToMany(mappedBy = "downloadProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<SectionDownloadProduct> sectionDownloadProducts = new LinkedHashSet<>();
}
