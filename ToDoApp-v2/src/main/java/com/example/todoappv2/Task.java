package com.example.todoappv2;

import java.time.LocalDateTime;

public class Task {
    private String title;
    private String priority;
    private LocalDateTime dueDate;
    private boolean completed;

    public Task(String title, String priority, LocalDateTime dueDate) {
        this.title = title;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public Task(String title, String priority, LocalDateTime dueDate, boolean completed) {
        this.title = title;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public String getTitle() { return title; }
    public String getPriority() { return priority; }
    public LocalDateTime getDueDate() { return dueDate; }
    public boolean isCompleted() { return completed; }

    public void setTitle(String title) { this.title = title; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
