package com.futurex.services.FutureXCourseCatalog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
public class CatalogController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/catalog")
    @CircuitBreaker(name = "courseService", fallbackMethod = "fallbackGetCatalog")
    public String getCatalog() {
        List<ServiceInstance> instances = discoveryClient.getInstances("fx-course-service");
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances of fx-course-service available");
        }
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri().toString();
        System.out.println("URL: " + url);
        
        String course = restTemplate.getForObject(url + "/course", String.class);
        return course;
    }

    @RequestMapping("/")
    public String home() {
        return "This is the Course Catalog Service running on port: 8765";
    }

    @GetMapping("/first-course")  
    @CircuitBreaker(name = "courseService", fallbackMethod = "fallbackGetFirstCourse")
    public String getFirstCourse() {
        List<ServiceInstance> instances = discoveryClient.getInstances("fx-course-service");
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances of fx-course-service available");
        }
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri().toString();
        System.out.println("URL: " + url);
        
        String course = restTemplate.getForObject(url + "/course-first", String.class);
        return course;
    }

    // Fallback methods
    public String fallbackGetCatalog(Exception ex) {
        return "El servicio de catálogo no está disponible temporalmente. Por favor, inténtelo más tarde.";
    }

    public String fallbackGetFirstCourse(Exception ex) {
        return "El servicio de cursos no está disponible temporalmente. Por favor, inténtelo más tarde.";
    }
}
