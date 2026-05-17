package com.coworking.booking.repository;

import com.coworking.booking.model.Workspace;
import com.coworking.booking.model.WorkspaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByAvailableTrue();
    List<Workspace> findByType(WorkspaceType type);
    List<Workspace> findByIdNotIn(List<Long> ids);
}