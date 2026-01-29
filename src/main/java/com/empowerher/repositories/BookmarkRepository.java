package com.empowerher.repositories;

import com.empowerher.entities.Bookmark;
import com.empowerher.entities.Scheme;
import com.empowerher.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    Optional<Bookmark> findByUserAndScheme(User user, Scheme scheme);
    boolean existsByUserAndScheme(User user, com.empowerher.entities.Scheme scheme);
    void deleteByUserAndScheme(User user, com.empowerher.entities.Scheme scheme);
    

    
    
}