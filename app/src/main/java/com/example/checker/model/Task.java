package com.example.checker.model;

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

    public Task(String taskID, int taskType, String processID, String process, String subprocess, String taskName, String status, String expirationDate) {
        this.taskID = taskID;
        this.taskType = taskType;
        this.processID = processID;
        this.process = process;
        this.subprocess = subprocess;
        this.taskName = taskName;
        this.status = status;
        this.expirationDate = expirationDate;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getSubprocess() {
        return subprocess;
    }

    public void setSubprocess(String subprocess) {
        this.subprocess = subprocess;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
