package com.documentor.backend.presentation.document;

import com.documentor.backend.service.document.DocumentResult;
import com.documentor.backend.service.document.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @ModelAttribute DocumentUploadRequest request
    ) {
        DocumentResult result = documentService.upload(authorizationHeader, request.title(), request.file());
        return DocumentResponse.from(result);
    }

    @GetMapping
    public PageResponse<DocumentSummaryResponse> getDocuments(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DocumentResult> result = documentService.getDocuments(authorizationHeader, PageRequest.of(page, size));
        return PageResponse.from(result.map(DocumentSummaryResponse::from));
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long documentId
    ) {
        return DocumentResponse.from(documentService.getDocument(authorizationHeader, documentId));
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long documentId
    ) {
        documentService.deleteDocument(authorizationHeader, documentId);
    }

    public record DocumentUploadRequest(
            MultipartFile file,

            @NotBlank
            @Size(max = 100)
            String title
    ) {
    }
}
