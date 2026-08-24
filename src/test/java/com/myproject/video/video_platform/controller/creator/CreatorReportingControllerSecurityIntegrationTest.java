package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.dto.creator.analytics.CreatorAnalyticsDtos;
import com.myproject.video.video_platform.dto.creator.customers.CreatorCustomerDtos;
import com.myproject.video.video_platform.dto.creator.sales.CreatorSalesDtos;
import com.myproject.video.video_platform.service.creator.CreatorAnalyticsService;
import com.myproject.video.video_platform.service.creator.CreatorCustomersService;
import com.myproject.video.video_platform.service.creator.CreatorSalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CreatorReportingControllerSecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CreatorSalesService salesService;
    @MockBean
    private CreatorCustomersService customersService;
    @MockBean
    private CreatorAnalyticsService analyticsService;

    @Test
    @WithMockUser(roles = "CREATOR")
    void creatorCanAccessEveryReportingEndpoint() throws Exception {
        UUID id = UUID.randomUUID();
        when(salesService.summary(anyString())).thenReturn(new CreatorSalesDtos.Summary("30d", List.of()));
        when(salesService.orders(anyInt(), anyInt(), any(), anyString(), any(), anyString(), anyString()))
                .thenReturn(new CreatorSalesDtos.OrdersPage(List.of(), 0, 0, 10, 0, true, true, true, List.of()));
        when(salesService.order(id)).thenReturn(new CreatorSalesDtos.OrderDetail(id.toString(), null, "paid",
                "one-time", 0, "EUR", null, null, List.of(), null, null, null, null, List.of(), null, null, null));
        when(customersService.customers(anyInt(), anyInt(), any(), anyString(), any(), anyString(), anyString()))
                .thenReturn(new CreatorCustomerDtos.Page(List.of(), 0, 0, 10, 0, true, true, true, List.of()));
        when(customersService.customer(id)).thenReturn(new CreatorCustomerDtos.Detail(id.toString(), null,
                "buyer@example.test", null, "buyer", "none", List.of(), 0, 0, 0, null, null,
                null, null, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of()));
        when(analyticsService.overview(anyString())).thenReturn(new CreatorAnalyticsDtos.Overview(
                "30d", "last 30 days", "previous 30 days", List.of(),
                new CreatorAnalyticsDtos.Performance(List.of(), 0, 0), List.of(),
                new CreatorAnalyticsDtos.CustomerGrowth(new CreatorAnalyticsDtos.CustomerGrowthSummary(0, 0, "No change"), List.of()),
                new CreatorAnalyticsDtos.Memberships(null, List.of()),
                new CreatorAnalyticsDtos.PaymentHealth(List.of(), List.of())));

        mockMvc.perform(get("/api/creator/sales/summary")).andExpect(status().isOk()).andExpect(jsonPath("$.period").value("30d"));
        mockMvc.perform(get("/api/creator/orders")).andExpect(status().isOk()).andExpect(jsonPath("$.empty").value(true));
        mockMvc.perform(get("/api/creator/orders/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(get("/api/creator/customers")).andExpect(status().isOk()).andExpect(jsonPath("$.empty").value(true));
        mockMvc.perform(get("/api/creator/customers/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(get("/api/creator/analytics/overview")).andExpect(status().isOk())
                .andExpect(jsonPath("$.memberships.summary").value(nullValue()))
                .andExpect(jsonPath("$.memberships.series").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminIsRejectedFromEveryCreatorReportingEndpoint() throws Exception {
        assertForbiddenForAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void userIsRejectedFromEveryCreatorReportingEndpoint() throws Exception {
        assertForbiddenForAll();
    }

    private void assertForbiddenForAll() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/creator/sales/summary")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/creator/orders")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/creator/orders/{id}", id)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/creator/customers")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/creator/customers/{id}", id)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/creator/analytics/overview")).andExpect(status().isForbidden());
    }
}
