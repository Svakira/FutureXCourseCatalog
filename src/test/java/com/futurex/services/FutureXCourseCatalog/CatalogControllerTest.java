package com.futurex.services.FutureXCourseCatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    private EurekaClient eurekaClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InstanceInfo instanceInfo;

    @InjectMocks
    private CatalogController catalogController;

    @BeforeEach
    void setUp() {
        lenient().when(eurekaClient.getNextServerFromEureka("fx-course-service", false))
                .thenReturn(instanceInfo);
        lenient().when(instanceInfo.getHomePageUrl()).thenReturn("http://localhost:8080/");
    }

    @Test
    void testGetCatalogHome_Success() {
        when(restTemplate.getForObject("http://localhost:8080/", String.class))
                .thenReturn("from Course Service");

        String result = catalogController.getCatalogHome();

        assertEquals("Welcome to FutureX Course Catalog from Course Service", result);
    }

    @Test
    void testGetCatalog_Success() {
        when(restTemplate.getForObject("http://localhost:8080/courses", String.class))
                .thenReturn("Spring Boot, Microservices");

        String result = catalogController.getCatalog();

        assertEquals("Our courses are Spring Boot, Microservices", result);
    }

    @Test
    void testGetSpecificCourse_Success() {
        Course course = new Course("Java Fundamentals", "Basic Java programming");
        when(restTemplate.getForObject("http://localhost:8080/1", Course.class))
                .thenReturn(course);

        String result = catalogController.getSpecificCourse();

        assertEquals("Our first course is Java Fundamentals", result);
    }

    @Test
    void testFallbackGetCatalogHome() {
        String result = catalogController.fallbackGetCatalogHome(new RuntimeException());

        assertTrue(result.contains("temporarily unavailable"));
    }

    @Test
    void testFallbackGetCatalog() {
        String result = catalogController.fallbackGetCatalog(new RuntimeException());

        assertTrue(result.contains("currently unavailable"));
    }

    @Test
    void testFallbackGetSpecificCourse() {
        String result = catalogController.fallbackGetSpecificCourse(new RuntimeException());

        assertTrue(result.contains("currently unavailable"));
    }
}