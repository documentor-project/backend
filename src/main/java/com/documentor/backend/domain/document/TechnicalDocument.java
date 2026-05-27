package com.documentor.backend.domain.document;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class TechnicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentFileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(nullable = false)
    private int chunkCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected TechnicalDocument() {
    }

    private TechnicalDocument(User owner, String title, String fileName, DocumentFileType fileType) {
        this.owner = owner;
        this.title = title;
        this.fileName = fileName;
        this.fileType = fileType;
        this.status = DocumentStatus.UPLOADED;
        this.chunkCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public static TechnicalDocument create(User owner, String title, String fileName, DocumentFileType fileType) {
        return new TechnicalDocument(owner, title, fileName, fileType);
    }

    public void markParsing() {
        ensureStatus(DocumentStatus.UPLOADED, "업로드된 문서만 파싱을 시작할 수 있습니다.");
        this.status = DocumentStatus.PARSING;
    }

    public void markEmbedding() {
        ensureStatus(DocumentStatus.PARSING, "파싱 중인 문서만 임베딩을 시작할 수 있습니다.");
        this.status = DocumentStatus.EMBEDDING;
    }

    public void markReady(int chunkCount) {
        ensureStatus(DocumentStatus.EMBEDDING, "임베딩 중인 문서만 준비 완료 상태로 전환할 수 있습니다.");
        if (chunkCount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_RESOURCE_STATE, "문서 청크가 생성되지 않았습니다.");
        }
        this.status = DocumentStatus.READY;
        this.chunkCount = chunkCount;
    }

    public void markFailed() {
        this.status = DocumentStatus.FAILED;
    }

    public boolean isOwnedBy(Long userId) {
        return owner.getId().equals(userId);
    }

    private void ensureStatus(DocumentStatus expected, String message) {
        if (this.status != expected) {
            throw new BusinessException(ErrorCode.INVALID_RESOURCE_STATE, message);
        }
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public DocumentFileType getFileType() {
        return fileType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
