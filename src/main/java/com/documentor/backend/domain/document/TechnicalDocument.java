package com.documentor.backend.domain.document;

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
        this.status = DocumentStatus.READY;
        this.chunkCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public static TechnicalDocument create(User owner, String title, String fileName, DocumentFileType fileType) {
        return new TechnicalDocument(owner, title, fileName, fileType);
    }

    public boolean isOwnedBy(Long userId) {
        return owner.getId().equals(userId);
    }

    public Long getId() {
        return id;
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
