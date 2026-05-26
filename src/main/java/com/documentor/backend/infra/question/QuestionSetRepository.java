package com.documentor.backend.infra.question;

import com.documentor.backend.domain.question.QuestionSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {

    Page<QuestionSet> findAllByOwnerId(Long ownerId, Pageable pageable);

    Page<QuestionSet> findAllByOwnerIdAndDocumentId(Long ownerId, Long documentId, Pageable pageable);
}
