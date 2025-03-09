package org.example.productbacklog.repository;

import org.example.productbacklog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.id IN (SELECT t.assignedUser.id FROM Task t WHERE t.userStory.id = :userStoryId)")
    List<User> findUsersByUserStoryId(@Param("userStoryId") Long userStoryId);

    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT t.assignedUser.id FROM Task t WHERE t.assignedUser IS NOT NULL)")
    List<User> findUsersWithoutTasks();

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.assignedUser.id = :userId")
    boolean hasAssignedTasks(@Param("userId") Long userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}