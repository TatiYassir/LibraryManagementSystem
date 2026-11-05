package com.library.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database connection details
	private static final String URL =
	        "jdbc:mariadb://localhost:3306/library_management?sslMode=disable";

    private static final String USER = "root";

    // XAMPP root account: use an empty password if none is configured
    private static final String PASSWORD = "";

    // Establish and return a database connection
    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}