package com.example.todoappv2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

        private static final String URL = "jdbc:mysql://localhost:3306/todoapp";
        private static final String USER = "root"; // your MySQL username
        private static final String PASSWORD = "Pass123"; // your MySQL password

        public static Connection getConnection () throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
}

