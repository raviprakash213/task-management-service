package com.epam.AsyncDataPipeline.mapper;

import com.epam.AsyncDataPipeline.dto.TaskManagementRequest;
import com.epam.AsyncDataPipeline.dto.TaskManagementResponse;
import com.epam.AsyncDataPipeline.dto.TaskManagementStatusResponse;
import com.epam.AsyncDataPipeline.entity.TaskManagement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper interface for converting between entity and DTO objects using MapStruct.
 * This interface defines mapping methods for transforming entities
 * to various DTO representations and vice versa. MapStruct generates the implementation
 * at compile-time.
 */
@Mapper(componentModel = "spring")
public interface EntityToModelMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    TaskManagement mapRequestToEntity(TaskManagementRequest taskManagementRequest);
    TaskManagementResponse mapEntityToDto(TaskManagement taskManagement);

    TaskManagementStatusResponse mapEntityToStatusDto(TaskManagement taskManagement);
    List<TaskManagementResponse> mapEntityToDtoList(List<TaskManagement> taskManagementList);
}
