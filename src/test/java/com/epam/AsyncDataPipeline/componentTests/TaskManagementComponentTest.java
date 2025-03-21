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

        mockMvc.perform(post("/taskManagement")  // Ensure the correct endpoint
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

        mockMvc.perform(post("/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTaskRequestJson))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(jsonPath("$.message").exists()) // Ensure an error message is present
                .andExpect(jsonPath("$.message").value("Name must be at most 50 characters")); // Adjust based on your validation message
    }


    @Test
    public void testGetAllTasks_Success() throws Exception {
        mockMvc.perform(get("/taskManagement")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[1].name").isNotEmpty())
                .andExpect(jsonPath("$[2].name").isNotEmpty())
                .andExpect(jsonPath("$[3].name").isNotEmpty());
    }


    @Test
    public void testGetTaskStatusById() throws Exception {
        mockMvc.perform(get("/taskManagement/status/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(savedTask.getStatus().toString()));
    }

    @Test
    public void testGetTaskById_NotFound() throws Exception {
        long invalidId = 999L; // Assuming this ID doesn't exist

        mockMvc.perform(get("/taskManagement/status/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expecting 404 status
                .andExpect(jsonPath("$.message").value("Task with id " + invalidId + " not found")); // Assert error message
    }

    @Test
    public void testGetTaskStatistics() throws Exception {
        mockMvc.perform(get("/taskManagement/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ensure HTTP 200 response
                .andExpect(jsonPath("$.totalTasks").value(5))
                .andExpect(jsonPath("$.completedTasks").value(2))
                .andExpect(jsonPath("$.failedTasks").value(1))
                .andExpect(jsonPath("$.successRate").value(40.0))
                .andExpect(jsonPath("$.failureRate").value(20.0));
    }

}
