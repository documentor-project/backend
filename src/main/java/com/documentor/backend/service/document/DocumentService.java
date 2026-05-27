package com.documentor.backend.service.document;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.document.DocumentFileType;
import com.documentor.backend.domain.document.TechnicalDocument;
import com.documentor.backend.domain.user.User;
import com.documentor.backend.infra.document.TechnicalDocumentRepository;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import com.documentor.backend.infra.user.UserRepository;
import java.nio.file.Path;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final TechnicalDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final DocumentFileStorage documentFileStorage;
    private final DocumentEmbeddingService documentEmbeddingService;

    public DocumentService(
            TechnicalDocumentRepository documentRepository,
            UserRepository userRepository,
            AuthenticatedUserResolver authenticatedUserResolver,
            DocumentFileStorage documentFileStorage,
            DocumentEmbeddingService documentEmbeddingService
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.documentFileStorage = documentFileStorage;
        this.documentEmbeddingService = documentEmbeddingService;
    }

    public DocumentResult upload(String authorizationHeader, String title, MultipartFile file) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateFile(file);
        DocumentFileType fileType = DocumentFileType.fromFileName(file.getOriginalFilename());
        TechnicalDocument document = documentRepository.save(TechnicalDocument.create(owner, title, file.getOriginalFilename(), fileType));
        Path filePath = documentFileStorage.store(document.getId(), file);
        documentEmbeddingService.process(document.getId(), filePath);
        return DocumentResult.from(document);
    }

    public Page<DocumentResult> getDocuments(String authorizationHeader, Pageable pageable) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return documentRepository.findAllByOwnerId(userId, pageable)
                .map(DocumentResult::from);
    }

    public DocumentResult getDocument(String authorizationHeader, Long documentId) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return DocumentResult.from(getOwnedDocument(userId, documentId));
    }

    public void deleteDocument(String authorizationHeader, Long documentId) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        TechnicalDocument document = getOwnedDocument(userId, documentId);
        documentRepository.delete(document);
    }

    private TechnicalDocument getOwnedDocument(Long userId, Long documentId) {
        TechnicalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다."));
        if (!document.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "문서에 접근할 수 없습니다.");
        }
        return document;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드할 파일이 필요합니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }
}
