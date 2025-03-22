package com.epam.AsyncDataPipeline.componentTests;

import com.epam.AsyncDataPipeline.entity.TaskManagement;
import com.epam.AsyncDataPipeline.enums.TaskStatus;
import com.epam.AsyncDataPipeline.repository.TaskManagementRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskManagementComponentTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskManagementRepository taskRepository;

    private static TaskManagement savedTask;

    @BeforeAll
    public static void setup(@Autowired TaskManagementRepository taskRepository) {  // Inject repository here
        // Clear the database before running tests
        taskRepository.deleteAll();

        taskRepository.save(new TaskManagement("Task 1", "Payload 1", TaskStatus.COMPLETED));
        taskRepository.save(new TaskManagement("Task 2", "Payload 2", TaskStatus.COMPLETED));
        taskRepository.save(new TaskManagement("Task 3", "Payload 3", TaskStatus.FAILED));
        savedTask = taskRepository.save(new TaskManagement("Task 4", "Payload 4", TaskStatus.PENDING));
        taskRepository.save(new TaskManagement("Task 5", "Payload for Task 5", TaskStatus.COMPLETED));
        taskRepository.save(new TaskManagement("Task 6", "Payload for Task 6", TaskStatus.FAILED));

    }

    @Test
    public void testSubmitTask_Success() throws Exception {
        // Create a valid request payload
        String taskRequestJson = """
                {
                    "name": "New Task",
                    "payload": "Sample Payload"
                }
                """;

        mockMvc.perform(post("/api/v1/taskManagement")  // Ensure the correct endpoint
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequestJson))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.name").value("New Task")) // Validate response
                .andExpect(jsonPath("$.creationMessage").value("Task Management Initiation Started"));
    }

    @Test
    public void testSubmitTask_InvalidInput_EmptyName() throws Exception {
        // JSON payload with an empty name
        String invalidTaskRequestJson = """
                        {
                            "name": "thisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongwordthisisaverylongword",
                            "payload": "Sample Payload"
                        }
                            """;

        mockMvc.perform(post("/api/v1/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTaskRequestJson))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(jsonPath("$.message").exists()) // Ensure an error message is present
                .andExpect(jsonPath("$.message").value("Name must be at most 50 characters")); // Adjust based on your validation message
    }


    @Test
    public void testGetAllTasks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[1].name").isNotEmpty())
                .andExpect(jsonPath("$[2].name").isNotEmpty())
                .andExpect(jsonPath("$[3].name").isNotEmpty());
    }


    @Test
    public void testGetTaskStatusById() throws Exception {
        mockMvc.perform(get("/api/v1/taskManagement/status/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(savedTask.getStatus().toString()));
    }

    @Test
    public void testGetTaskById_NotFound() throws Exception {
        long invalidId = 999L; // Assuming this ID doesn't exist

        mockMvc.perform(get("/api/v1/taskManagement/status/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expecting 404 status
                .andExpect(jsonPath("$.message").value("Task with id " + invalidId + " not found")); // Assert error message
    }

    @Test
    public void testGetTaskStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/taskManagement/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ensure HTTP 200 response
                .andExpect(jsonPath("$.totalTasks").value(7))
                .andExpect(jsonPath("$.completedTasks").value(3))
                .andExpect(jsonPath("$.failedTasks").value(2))
                .andExpect(jsonPath("$.successRate").value(42.857142857142854))
                .andExpect(jsonPath("$.failureRate").value(28.571428571428573));
    }

    @Test
    public void testGetAllTasks_WithPaginationAndSorting() throws Exception {

        mockMvc.perform(get("/api/v1/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0") // Page number
                        .param("size", "2") // Page size
                        .param("sortBy", "name") // Sort by name
                        .param("sortDir", "asc")) // Sort direction
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.length()").value(2)) // Check that only 2 tasks are returned
                .andExpect(jsonPath("$[0].name").value("New Task")) // First task should be "Task 1"
                .andExpect(jsonPath("$[1].name").value("Task 1")); // Second task should be "Task 2"
    }

    @Test
    public void testGetAllTasks_WithPaginationAndSorting_DescendingOrder() throws Exception {

        mockMvc.perform(get("/api/v1/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0") // Page number
                        .param("size", "2") // Page size
                        .param("sortBy", "name") // Sort by name
                        .param("sortDir", "desc")) // Sort direction descending
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.length()").value(2)) // Check that only 2 tasks are returned
                .andExpect(jsonPath("$[0].name").value("Task 6")) // First task should be "Task 6"
                .andExpect(jsonPath("$[1].name").value("Task 5")); // Second task should be "Task 5"
    }

    @Test
    public void testGetAllTasks_InvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/v1/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "name")
                        .param("sortDir", "desc1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sorting direction: 'desc1'. Allowed values: 'asc' or 'desc'."));
    }

    @Test
    public void testGetAllTasks_InvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "random")
                        .param("sortDir", "asc"))
                .andExpect(status().isBadRequest());
    }

}
