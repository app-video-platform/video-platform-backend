package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.download.DownloadProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.download.FileDownloadProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.download.SectionDownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.download.SectionDownloadProductResponseDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DownloadProductConverter {

    @Value("${digitalocean.spaces.cdnEndpointUrl}")
    private String cdnEndpoint;

    @Value("${digitalocean.spaces.bucket-media}")
    private String bucketName;

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
            Set<SectionDownloadProduct> sections = dto.getSections().stream()
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
                            .collect(Collectors.toSet());
            product.setSectionDownloadProducts(sections);
        }

        return product;
    }

    public AbstractProductResponseDto mapDownloadProductToResponse(DownloadProduct product) {
        DownloadProductResponseDto dto = new DownloadProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setType(product.getType().name());
        dto.setStatus(product.getStatus().toString());
        dto.setPrice(product.getPrice().compareTo(BigDecimal.ZERO) == 0 ? "free" : product.getPrice().toString());
        dto.setUserId(product.getUser().getUserId());

        if (product.getSectionDownloadProducts() != null) {
            List<SectionDownloadProductResponseDto> sections = product.getSectionDownloadProducts()
                    .stream()
                    .sorted(Comparator.comparing(SectionDownloadProduct::getPosition))
                    .map(this::mapSection)
                    .toList();
            dto.setSections(sections);
        }

        return dto;
    }

    private SectionDownloadProductResponseDto mapSection(SectionDownloadProduct sec) {
        SectionDownloadProductResponseDto dto = new SectionDownloadProductResponseDto();
        dto.setId(sec.getId());
        dto.setTitle(sec.getTitle());
        dto.setDescription(sec.getDescription());
        dto.setPosition(sec.getPosition());

        List<FileDownloadProductResponseDto> fileDtos = sec.getFiles()
                .stream()
                .map(this::mapFile)
                .toList();
        dto.setFiles(fileDtos);
        return dto;
    }

    private FileDownloadProductResponseDto mapFile(FileDownloadProduct f) {
        FileDownloadProductResponseDto dto = new FileDownloadProductResponseDto();
        dto.setId(f.getId());
        dto.setFileName(f.getFileName());
        dto.setSize(f.getSize());
        dto.setFileType(f.getFileType());

        dto.setUrl(String.format("%s/%s/%s", cdnEndpoint, bucketName, f.getPath()));
        return dto;
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

        Set<SectionDownloadProduct> existingSections = product.getSectionDownloadProducts();
        Map<UUID, SectionDownloadProduct> existingById = existingSections.stream()
                .collect(Collectors.toMap(SectionDownloadProduct::getId, Function.identity()));

        List<SectionDownloadProduct> sectionsToKeep = new ArrayList<>();

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
                    sectionsToKeep.add(existingSec);
                    existingById.remove(secId); // mark handled
                } else {
                    // CREATE NEW
                    SectionDownloadProduct newSec = new SectionDownloadProduct();
                    newSec.setTitle(secDto.getTitle());
                    newSec.setDescription(secDto.getDescription());
                    newSec.setPosition(secDto.getPosition());
                    newSec.setDownloadProduct(product);
                    sectionsToKeep.add(newSec);
                }
            }
        }

        existingSections.clear();
        existingSections.addAll(sectionsToKeep);
        return product;
    }
}
