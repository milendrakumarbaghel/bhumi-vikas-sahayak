package org.springboot.backend.repository;

import java.util.Optional;

import org.springboot.backend.entity.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {
    Optional<SourceDocument> findFirstByLanguageIgnoreCaseOrderByVersionDateDesc(String language);
}
