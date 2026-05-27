package com.documentor.backend.service.document;

import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentFileStorage {

    Path store(Long documentId, MultipartFile file);
}
