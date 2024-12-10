package com.shop.repository;


import com.shop.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

   List<Comment> findByItemId(Long itemId);

   List<Comment> findByCreatedBy(String createdBy);

   List<Comment> findByCreatedByOrContent(String createdBy, String content);

   List<Comment> findByRegTimeLessThan(LocalDateTime regTime);

}
