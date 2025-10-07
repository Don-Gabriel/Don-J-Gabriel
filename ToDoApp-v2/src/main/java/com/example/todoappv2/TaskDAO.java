package com.example.todoappv2;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public void addTask(String title, String priority, LocalDateTime dueDateTime) {
        String sql = "INSERT INTO tasks (title, priority, due_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, priority);
            stmt.setString(3, dueDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.setString(4, "Pending");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTaskStatus(Task task) {
        String sql = "UPDATE tasks SET status=? WHERE title=? AND due_date=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.isCompleted() ? "Completed" : "Pending");
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                String priority = rs.getString("priority");
                LocalDateTime dueDateTime = LocalDateTime.parse(
                        rs.getString("due_date"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                boolean completed = rs.getString("status").equalsIgnoreCase("Completed");

                tasks.add(new Task(title, priority, dueDateTime, completed));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public void deleteTask(Task task) {
        String sql = "DELETE FROM tasks WHERE title=? AND due_date=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
