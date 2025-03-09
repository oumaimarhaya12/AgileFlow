package org.example.productbacklog.repository;

import org.example.productbacklog.entity.Comment;
import org.example.productbacklog.entity.Task;
import org.example.productbacklog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByUser(User user);

    List<Comment> findByTask(Task task);

    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);

    List<Comment> findByUserOrderByCreatedAtDesc(User user);

    void deleteByTask(Task task);

    void deleteByUser(User user);
}