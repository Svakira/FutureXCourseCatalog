package com.futurex.services.FutureXCourseCatalog;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

@WebMvcTest(CatalogController.class)
class CatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EurekaClient eurekaClient;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private InstanceInfo instanceInfo;

    @Test
    void testCatalogHomeEndpoint() throws Exception {
        when(eurekaClient.getNextServerFromEureka("fx-course-service", false))
                .thenReturn(instanceInfo);
        when(instanceInfo.getHomePageUrl()).thenReturn("http://localhost:8080/");
        when(restTemplate.getForObject("http://localhost:8080/", String.class))
                .thenReturn("Hello from Course Service");

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to FutureX Course Catalog")))
                .andExpect(content().string(containsString("Hello from Course Service")));
    }

    @Test
    void testCatalogEndpoint() throws Exception {
        when(eurekaClient.getNextServerFromEureka("fx-course-service", false))
                .thenReturn(instanceInfo);
        when(instanceInfo.getHomePageUrl()).thenReturn("http://localhost:8080/");
        when(restTemplate.getForObject("http://localhost:8080/courses", String.class))
                .thenReturn("Java, Spring Boot, Microservices");

        // Act & Assert
        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Our courses are")))
                .andExpect(content().string(containsString("Java, Spring Boot, Microservices")));
    }

    @Test
    void testFirstCourseEndpoint() throws Exception {
        when(eurekaClient.getNextServerFromEureka("fx-course-service", false))
                .thenReturn(instanceInfo);
        when(instanceInfo.getHomePageUrl()).thenReturn("http://localhost:8080/");
        Course course = new Course("Advanced Java", "Deep dive into Java programming");
        when(restTemplate.getForObject("http://localhost:8080/1", Course.class))
                .thenReturn(course);

        mockMvc.perform(get("/firstcourse"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Our first course is")))
                .andExpect(content().string(containsString("Advanced Java")));
    }
}