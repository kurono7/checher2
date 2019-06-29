package com.example.checker.model;

import java.io.Serializable;

public class Project implements Serializable {
    private String projectName;
    private String projectID;

    public Project(String projectName, String projectID) {
        this.projectName = projectName;
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectID() {
        return projectID;
    }
}
