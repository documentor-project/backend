package com.documentor.backend.infra.document;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.service.document.DocumentFileStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalDocumentFileStorage implements DocumentFileStorage {

    private final Path storageRoot;

    public LocalDocumentFileStorage(@Value("${app.document.storage-path}") Path storageRoot) {
        this.storageRoot = storageRoot;
    }

    @Override
    public Path store(Long documentId, MultipartFile file) {
        try {
            Files.createDirectories(storageRoot);
            String originalFileName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
            String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path targetPath = storageRoot.resolve(documentId + "-" + safeFileName).normalize();
            file.transferTo(targetPath);
            return targetPath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문서 파일 저장에 실패했습니다.");
        }
    }
}
