package com.safaricom.taskmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.taskmanager.dto.TaskRequest;
import com.safaricom.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskManagerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.safaricom.taskmanager.repository.TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        // Clear all tasks before each test for isolation
        // Use reflection or just recreate — simplest: get all and delete
        taskRepository.findAll().forEach(t -> taskRepository.deleteById(t.getId()));
    }

    @Test
    void contextLoads() {
    }

    // --- POST tests ---

    @Test
    void postValidTask_returns201() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("Test Task");
        req.setDescription("A test description");
        req.setStatus(TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postWithoutStatus_defaultsTodo() throws Exception {
        String json = "{\"title\": \"No status task\", \"description\": \"desc\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postMissingTitle_returns400() throws Exception {
        String json = "{\"description\": \"no title\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void postBlankTitle_returns400() throws Exception {
        String json = "{\"title\": \"   \", \"description\": \"blank title\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void postTitleTooShort_returns400() throws Exception {
        String json = "{\"title\": \"ab\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void postTitleTooLong_returns400() throws Exception {
        String longTitle = "a".repeat(101);
        String json = "{\"title\": \"" + longTitle + "\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void postDescriptionTooLong_returns400() throws Exception {
        String longDesc = "d".repeat(501);
        TaskRequest req = new TaskRequest();
        req.setTitle("Valid Title");
        req.setDescription(longDesc);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void postInvalidStatus_returns400() throws Exception {
        String json = "{\"title\": \"Valid Title\", \"status\": \"INVALID\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- GET tests ---

    @Test
    void getAllTasks_returns200() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExistingTask_returns200() throws Exception {
        // Create a task first
        String json = "{\"title\": \"Get Test\"}";
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Get Test"));
    }

    @Test
    void getMissingTask_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // --- PUT tests ---

    @Test
    void putValidTask_returns200() throws Exception {
        // Create first
        String createJson = "{\"title\": \"Original\"}";
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();

        String updateJson = "{\"title\": \"Updated\", \"description\": \"new desc\", \"status\": \"DONE\"}";
        mockMvc.perform(put("/api/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void putInvalidTask_returns400() throws Exception {
        // Create first
        String createJson = "{\"title\": \"Original\"}";
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();

        String updateJson = "{\"title\": \"ab\"}"; // too short
        mockMvc.perform(put("/api/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putMissingId_returns404() throws Exception {
        String json = "{\"title\": \"Valid Title\", \"status\": \"TODO\"}";
        mockMvc.perform(put("/api/tasks/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // --- DELETE tests ---

    @Test
    void deleteExistingTask_returns204() throws Exception {
        // Create first
        String json = "{\"title\": \"Delete Me\"}";
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(delete("/api/tasks/" + id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void deleteMissingTask_returns404() throws Exception {
        mockMvc.perform(delete("/api/tasks/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
