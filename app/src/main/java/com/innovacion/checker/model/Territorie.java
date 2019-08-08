package com.innovacion.checker.model;

import java.io.Serializable;

public class Territorie implements Serializable {

    private String territorieName;
    private String territorieID;
    private String projectID;
    private String projectName;

    public Territorie(String territorieName, String territorieID, String projectName, String projectID) {
        this.territorieName = territorieName;
        this.territorieID = territorieID;
        this.projectName = projectName;
        this.projectID = projectID;
    }

    public String getProjectName() { return projectName; }

    public String getTerritorieName() {
        return territorieName;
    }

    public String getTerritorieID() {
        return territorieID;
    }

    public String getProjectID() {
        return projectID;
    }
}
