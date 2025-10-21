package com.futurex.services.FutureXCourseCatalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
public class CatalogController {

    @Autowired
    private EurekaClient client;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/")
    @CircuitBreaker(name = "courseService", fallbackMethod = "fallbackGetCatalogHome")
    public String getCatalogHome() {
        InstanceInfo instanceInfo = client.getNextServerFromEureka("fx-course-service", false);
        String courseAppURL = instanceInfo.getHomePageUrl();
        String courseAppMesage = restTemplate.getForObject(courseAppURL, String.class);
        return "Welcome to FutureX Course Catalog " + courseAppMesage;
    }

    public String fallbackGetCatalogHome(Exception ex) {
        return "Welcome to FutureX Course Catalog - Course service is temporarily unavailable. Please try again later.";
    }

    @RequestMapping("/catalog")
    @CircuitBreaker(name = "courseService", fallbackMethod = "fallbackGetCatalog")
    public String getCatalog() {
        InstanceInfo instanceInfo = client.getNextServerFromEureka("fx-course-service", false);
        String courseAppURL = instanceInfo.getHomePageUrl();
        String courses = restTemplate.getForObject(courseAppURL + "courses", String.class);
        return "Our courses are " + courses;
    }

    public String fallbackGetCatalog(Exception ex) {
        return "Our courses are currently unavailable. The course service is down. Please try again later.";
    }

    @RequestMapping("/firstcourse")
    @CircuitBreaker(name = "courseService", fallbackMethod = "fallbackGetSpecificCourse")
    public String getSpecificCourse() {
        InstanceInfo instanceInfo = client.getNextServerFromEureka("fx-course-service", false);
        String courseAppURL = instanceInfo.getHomePageUrl();
        Course course = restTemplate.getForObject(courseAppURL + "1", Course.class);
        return "Our first course is " + (course != null ? course.getCoursename() : "Unknown");
    }

    public String fallbackGetSpecificCourse(Exception ex) {
        return "Our first course information is currently unavailable. The course service is down. Please try again later.";
    }
}
