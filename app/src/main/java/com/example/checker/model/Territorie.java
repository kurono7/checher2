package com.example.checker.model;

import java.io.Serializable;

public class Territorie implements Serializable {

    private String territorieName;
    private String territorieID;
    private String projectID;

    public Territorie(String territorieName, String territorieID, String projectID) {
        this.territorieName = territorieName;
        this.territorieID = territorieID;
        this.projectID = projectID;
    }

    public String getTerritorieName() {
        return territorieName;
    }

    public void setTerritorieName(String territorieName) {
        this.territorieName = territorieName;
    }

    public String getTerritorieID() {
        return territorieID;
    }

    public void setTerritorieID(String territorieID) {
        this.territorieID = territorieID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }
}
