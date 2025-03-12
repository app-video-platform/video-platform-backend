package com.myproject.video.video_platform.service.products.download_products;

import com.myproject.video.video_platform.repository.products.download_product.DownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download_product.FileDownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download_product.SectionDownloadProductRepository;
import org.springframework.stereotype.Service;

@Service
public class DownloadProductService {

    private final DownloadProductRepository downloadProductRepository;
    private final SectionDownloadProductRepository sectionDownloadProductRepository;
    private final FileDownloadProductRepository fileDownloadProductRepository;

    public DownloadProductService(
            DownloadProductRepository downloadProductRepository,
            SectionDownloadProductRepository sectionDownloadProductRepository,
            FileDownloadProductRepository fileDownloadProductRepository
    ) {
        this.downloadProductRepository = downloadProductRepository;
        this.sectionDownloadProductRepository = sectionDownloadProductRepository;
        this.fileDownloadProductRepository = fileDownloadProductRepository;
    }

}
