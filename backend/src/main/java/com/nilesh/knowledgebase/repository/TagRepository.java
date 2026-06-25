package com.nilesh.knowledgebase.repository;

import com.nilesh.knowledgebase.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);
    List<Tag> findAllByOrderByNameAsc();
}
