package com.epam.AsyncDataPipeline.repository;

import com.epam.AsyncDataPipeline.entity.TaskManagement;
import com.epam.AsyncDataPipeline.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskManagementRepository extends JpaRepository<TaskManagement, Long> {

}
