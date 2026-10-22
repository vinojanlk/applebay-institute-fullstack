package com.applebay.institute.controller;

import com.applebay.institute.dto.response.DashboardResponse;
import com.applebay.institute.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void summaryEndpointReturnsDashboardJson() throws Exception {
        when(dashboardService.getSummary()).thenReturn(
                new DashboardResponse(12L, 3L, 8L, new BigDecimal("950.50"))
        );

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(12))
                .andExpect(jsonPath("$.activeCourses").value(3))
                .andExpect(jsonPath("$.todayPresentCount").value(8))
                .andExpect(jsonPath("$.totalFeesCollected").value(950.50));
    }
}
