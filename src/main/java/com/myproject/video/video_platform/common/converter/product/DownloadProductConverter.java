package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.DownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.DownloadProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.SectionDownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.SectionDownloadProductResponseDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.download_product.DownloadProduct;
import com.myproject.video.video_platform.entity.products.download_product.SectionDownloadProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DownloadProductConverter {

    public DownloadProduct mapDownloadProductRequestDtoToEntity(DownloadProductRequestDto dto, User user) {
        DownloadProduct product = new DownloadProduct();
        // In case is an update of product, we get the ID
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            product.setId(UUID.fromString(dto.getId()));
        }
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setType(ProductType.DOWNLOAD);
        product.setStatus(parseStatus(dto.getStatus()));
        product.setPrice(parsePrice(dto.getPrice()));
        product.setUser(user);

        // Build sections
        if (dto.getSections() != null) {
            List<SectionDownloadProduct> sections = dto.getSections().stream()
                    .map(secDto -> {
                        SectionDownloadProduct sec = new SectionDownloadProduct();
                        // In case is an update of product, we get the ID
                        if (secDto.getId() != null) {
                            sec.setId(UUID.fromString(secDto.getId()));
                        }
                        sec.setTitle(secDto.getTitle());
                        sec.setDescription(secDto.getDescription());
                        sec.setPosition(secDto.getPosition());
                        sec.setDownloadProduct(product);  // link back
                        return sec;
                    })
                            .toList();
            product.setSectionDownloadProducts(sections);
        }

        return product;
    }

    public AbstractProductResponseDto mapDownloadProductToResponse(DownloadProduct product) {
        DownloadProductResponseDto response = new DownloadProductResponseDto();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setType(product.getType().name());
        response.setStatus(product.getStatus().toString());
        response.setPrice(product.getPrice().compareTo(BigDecimal.ZERO) == 0 ? "free" : product.getPrice().toString());
        response.setUserId(product.getUser().getUserId());

        if (product.getSectionDownloadProducts() != null) {
            List<SectionDownloadProductResponseDto> sectionResponses =  product.getSectionDownloadProducts().stream().map(sec -> {
                SectionDownloadProductResponseDto sr = new SectionDownloadProductResponseDto();
                sr.setId(sec.getId());
                sr.setTitle(sec.getTitle());
                sr.setDescription(sec.getDescription());
                sr.setPosition(sec.getPosition());
                return sr;
            }).toList();
            response.setSections(sectionResponses);
        }

        return response;
    }

    private ProductStatus parseStatus(String statusStr) {
        if (statusStr == null) {
            return ProductStatus.DRAFT;
        }
        try {
            return ProductStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProductStatus.DRAFT;
        }
    }

    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.equalsIgnoreCase("free")) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(priceStr);
    }

    public DownloadProduct mapDownloadProductUpdate(DownloadProduct product, DownloadProductRequestDto dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setType(ProductType.DOWNLOAD);
        product.setStatus(parseStatus(dto.getStatus()));
        product.setPrice(parsePrice(dto.getPrice()));

        List<SectionDownloadProduct> existingSections = product.getSectionDownloadProducts();
        Map<UUID, SectionDownloadProduct> existingById = existingSections.stream()
                .collect(Collectors.toMap(SectionDownloadProduct::getId, Function.identity()));

        List<SectionDownloadProduct> updatedSections = new ArrayList<>();

        if (dto.getSections() != null) {
            for (SectionDownloadProductRequestDto secDto : dto.getSections()) {
                UUID secId = null;
                if (secDto.getId() != null && !secDto.getId().isEmpty()) {
                    secId = UUID.fromString(secDto.getId());
                }

                if (secId != null && existingById.containsKey(secId)) {
                    // UPDATE EXISTING
                    SectionDownloadProduct existingSec = existingById.get(secId);
                    existingSec.setTitle(secDto.getTitle());
                    existingSec.setDescription(secDto.getDescription());
                    existingSec.setPosition(secDto.getPosition());
                    updatedSections.add(existingSec);
                    existingById.remove(secId); // mark handled
                } else {
                    // CREATE NEW
                    SectionDownloadProduct newSec = new SectionDownloadProduct();
                    newSec.setTitle(secDto.getTitle());
                    newSec.setDescription(secDto.getDescription());
                    newSec.setPosition(secDto.getPosition());
                    newSec.setDownloadProduct(product);
                    updatedSections.add(newSec);
                }
            }
        }

        product.setSectionDownloadProducts(updatedSections);
        return product;
    }
}
