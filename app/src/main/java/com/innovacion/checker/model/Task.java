package com.innovacion.checker.model;

import org.json.JSONObject;

import java.io.Serializable;

public class Task implements Serializable {

    private String taskID;
    private int taskType;
    private String processID;
    private String process;
    private String subprocess;
    private String taskName;
    private String status;
    private String expirationDate;
    private String extensionArchivo;
    private JSONObject json;

    public Task(String taskID, int taskType, String processID, String process, String subprocess, String taskName, String status, String expirationDate, String extensionArchivo, JSONObject json) {
        this.taskID = taskID;
        this.taskType = taskType;
        this.processID = processID;
        this.process = process;
        this.subprocess = subprocess;
        this.taskName = taskName;
        this.status = status;
        this.expirationDate = expirationDate;
        this.extensionArchivo = extensionArchivo;
        this.json = json;
    }

    public String getTaskID() {
        return taskID;
    }

    public int getTaskType() {
        return taskType;
    }

    public String getProcessID() {
        return processID;
    }

    public String getProcess() {
        return process;
    }

    public String getSubprocess() {
        return subprocess;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getStatus() {
        return status;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getExtensionArchivo() {
        return extensionArchivo;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
}
