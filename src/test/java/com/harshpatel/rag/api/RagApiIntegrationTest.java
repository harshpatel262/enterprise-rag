package com.harshpatel.rag.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full ingest -> retrieve -> answer cycle against the in-memory profile.
 * No database, no API keys: the offline extractive answerer is wired in.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RagApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void ingest(String title, String content) throws Exception {
        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "%s", "content": "%s"}
                                """.formatted(title, content)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chunkCount", greaterThan(0)));
    }

    @Test
    void ingestAndQueryReturnsCitedAnswer() throws Exception {
        ingest("Expense Policy",
                "Reimbursable expenses must be submitted within 60 days. "
                        + "Meals are capped at 75 dollars per day during travel.");
        ingest("Onboarding Guide",
                "New hires receive laptops on day one and complete security training "
                        + "during their first week.");

        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "what is the daily meal cap for travel expenses", "topK": 2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer", containsString("75 dollars")))
                .andExpect(jsonPath("$.citations[0].documentTitle").value("Expense Policy"));
    }

    @Test
    void blankQuestionIsRejected() throws Exception {
        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankDocumentIsRejected() throws Exception {
        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"x\", \"content\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
