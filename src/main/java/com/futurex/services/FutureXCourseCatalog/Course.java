package com.futurex.services.FutureXCourseCatalog;

public class Course {
    private String coursename;
    private String coursedesc;

    public Course() {
    }

    public Course(String coursename, String coursedesc) {
        this.coursename = coursename;
        this.coursedesc = coursedesc;
    }

    public String getCoursename() {
        return coursename;
    }

    public void setCoursename(String coursename) {
        this.coursename = coursename;
    }

    public String getCoursedesc() {
        return coursedesc;
    }

    public void setCoursedesc(String coursedesc) {
        this.coursedesc = coursedesc;
    }
}
