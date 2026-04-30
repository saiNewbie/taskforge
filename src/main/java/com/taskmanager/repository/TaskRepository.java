package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.Task.Status;
import com.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignee(User assignee);

    List<Task> findByAssigneeAndStatus(User assignee, Status status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Status status);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user AND t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee = :user AND t.status != 'DONE'")
    long countOpenTasksByAssignee(@Param("user") User user);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id IN :projectIds AND t.status = :status")
    long countByProjectIdsAndStatus(@Param("projectIds") List<Long> projectIds, @Param("status") Status status);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueByProjectIds(@Param("projectIds") List<Long> projectIds, @Param("now") LocalDateTime now);
}
