package com.epam.AsyncDataPipeline.service;

import com.epam.AsyncDataPipeline.dto.TaskManagementRequest;
import com.epam.AsyncDataPipeline.dto.TaskManagementResponse;
import com.epam.AsyncDataPipeline.dto.TaskManagementStatusResponse;
import com.epam.AsyncDataPipeline.dto.TaskStatisticsResponse;
import com.epam.AsyncDataPipeline.entity.TaskManagement;
import com.epam.AsyncDataPipeline.enums.TaskStatus;
import com.epam.AsyncDataPipeline.exception.TaskNotFoundException;
import com.epam.AsyncDataPipeline.exception.TaskProcessingException;
import com.epam.AsyncDataPipeline.mapper.EntityToModelMapper;
import com.epam.AsyncDataPipeline.repository.TaskManagementRepository;
import com.epam.AsyncDataPipeline.service.impl.TaskManagementServiceImpl;
import com.epam.AsyncDataPipeline.service.impl.TaskMetricsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceImplTest {

    @InjectMocks
    private static TaskManagementServiceImpl taskManagementService;

    @Mock
    private static TaskManagementRepository taskManagementRepository;

    @Mock
    private static KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private static EntityToModelMapper entityToModelMapper;

    @Mock
    private static TaskMetricsService taskMetricsService;

    private static TaskManagement taskEntity;
    private static TaskManagementResponse taskResponse;
    private static TaskManagementStatusResponse taskManagementStatusResponse;
    private static TaskManagementRequest taskRequest;

    private static Pageable pageable;
    private static Page<TaskManagement> taskPage;


    @BeforeAll
    static void setUp() {
        taskManagementService = new TaskManagementServiceImpl();

        taskRequest = new TaskManagementRequest();
        taskEntity = new TaskManagement();
        taskEntity.setId(9L);
        taskEntity.setStatus(TaskStatus.PENDING);

        ReflectionTestUtils.setField(taskManagementService, "topicName", "taskManagementTest2");

        taskResponse = new TaskManagementResponse();
        taskResponse.setStatus(TaskStatus.PENDING);

        taskManagementStatusResponse = new TaskManagementStatusResponse();
        taskManagementStatusResponse.setStatus(TaskStatus.PENDING);

        pageable = PageRequest.of(0, 5, Sort.by("id").ascending());
        taskPage = new PageImpl<>(List.of(taskEntity), pageable, 1);
    }

    @Test
    void testSubmitTask_Success() {

        when(entityToModelMapper.mapRequestToEntity(taskRequest)).thenReturn(taskEntity);
        when(taskManagementRepository.save(any(TaskManagement.class))).thenReturn(taskEntity);

        taskManagementService.submitTask(taskRequest);

        verify(taskManagementRepository, times(1)).save(taskEntity);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
        verify(taskMetricsService, times(1)).incrementTasksSubmitted();
    }

    @Test
    void testSubmitTask_TaskProcessingException() {
        when(entityToModelMapper.mapRequestToEntity(taskRequest)).thenReturn(taskEntity);
        when(taskManagementRepository.save(any(TaskManagement.class))).thenReturn(taskEntity);

        doThrow(new TaskProcessingException("Kafka send failed"))
                .when(kafkaTemplate).send( anyString(), anyString());

        TaskProcessingException exception = assertThrows(TaskProcessingException.class, () -> {
            taskManagementService.submitTask(taskRequest);
        });

        assertEquals("Kafka send failed", exception.getMessage());

        verify(taskManagementRepository, times(1)).save(any(TaskManagement.class));
        verify(kafkaTemplate, times(1)).send( anyString(), anyString());
        verifyNoInteractions(taskMetricsService);
    }

    @Test
    void testGetAllTasks() {
        when(taskManagementRepository.findAll(pageable)).thenReturn(taskPage);
        when(entityToModelMapper.mapEntityToDto(taskEntity)).thenReturn(taskResponse);

        // Call service method
        Page<TaskManagementResponse> responsePage = taskManagementService.getAllTasks(pageable);

        // Assertions
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(1, responsePage.getContent().size());
        assertEquals(taskResponse, responsePage.getContent().get(0));

        // Verify method calls
        verify(taskManagementRepository, times(1)).findAll(pageable);
        verify(entityToModelMapper, times(1)).mapEntityToDto(taskEntity);
    }

    @Test
    void testGetTaskById_Success() {
        when(taskManagementRepository.findById(9L)).thenReturn(Optional.of(taskEntity));
        when(entityToModelMapper.mapEntityToStatusDto(any())).thenReturn(taskManagementStatusResponse);

        TaskManagementStatusResponse response = taskManagementService.getTaskStatusById(9L);

        assertNotNull(response);
        assertEquals(taskManagementStatusResponse.getStatus(), response.getStatus());

        verify(taskManagementRepository, times(1)).findById(9L);
        verify(entityToModelMapper, times(1)).mapEntityToStatusDto(taskEntity);
    }

    @Test
    void testGetTaskById_NotFound() {
        when(taskManagementRepository.findById(99L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskManagementService.getTaskStatusById(99L));

        assertEquals("Task with id 99 not found", exception.getMessage());

        verify(taskManagementRepository, times(1)).findById(99L);
        verifyNoMoreInteractions(taskManagementRepository);
    }

    @Test
    void testUpdateTaskStatus_Success() {
        Long taskId = 9L;
        TaskStatus newStatus = TaskStatus.COMPLETED;

        when(taskManagementRepository.findById(taskId)).thenReturn(Optional.of(taskEntity));

        when(taskManagementRepository.save(any(TaskManagement.class))).thenReturn(taskEntity);

        taskManagementService.updateTaskStatus(taskId, newStatus);

        assertEquals(newStatus, taskEntity.getStatus());

        verify(taskManagementRepository, times(1)).findById(taskId);
        verify(taskManagementRepository, times(1)).save(taskEntity);

        verify(taskMetricsService, times(1)).incrementTasksCompleted();
    }

    @Test
    void testUpdateTaskStatus_TaskNotFound() {
        Long taskId = 9L;
        TaskStatus newStatus = TaskStatus.PENDING;

        when(taskManagementRepository.findById(taskId)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            taskManagementService.updateTaskStatus(taskId, newStatus);
        });

        assertEquals("Task with ID 9 not found", exception.getMessage());

        verify(taskManagementRepository, times(1)).findById(taskId);
        verifyNoMoreInteractions(taskManagementRepository); // Save should not be called
    }

    @Test
    void testGetTaskStatistics_Success() {
        List<TaskManagement> tasks = List.of(
                new TaskManagement("Task 1", "Payload 1", TaskStatus.COMPLETED),
                new TaskManagement("Task 2", "Payload 2", TaskStatus.FAILED),
                new TaskManagement("Task 3", "Payload 3", TaskStatus.PENDING),
                new TaskManagement("Task 4", "Payload 4", TaskStatus.COMPLETED),
                new TaskManagement("Task 5", "Payload 5", TaskStatus.COMPLETED)
        );


        when(taskManagementRepository.findAll()).thenReturn(tasks);

        TaskStatisticsResponse response = taskManagementService.getTaskStatistics();

        assertNotNull(response);
        assertEquals(5, response.getTotalTasks());         // 5 tasks in total
        assertEquals(3, response.getCompletedTasks());     // 3 tasks are completed
        assertEquals(1, response.getFailedTasks());        // 1 task is failed
        assertEquals(60.0, response.getSuccessRate());     // (3/5) * 100 = 60%
        assertEquals(20.0, response.getFailureRate());     // (1/5) * 100 = 20%

        verify(taskManagementRepository, times(1)).findAll();
        verify(taskMetricsService, times(1)).incrementTasksSubmitted(5);
        verify(taskMetricsService, times(1)).incrementTasksCompleted(3);
        verify(taskMetricsService, times(1)).incrementTasksFailed(1);
    }


}
