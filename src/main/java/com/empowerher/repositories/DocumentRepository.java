package com.empowerher.repositories;

import com.empowerher.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findBySchemeId(Long schemeId);
    void deleteBySchemeId(Long schemeId);
}