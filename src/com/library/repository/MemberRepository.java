package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.library.model.Member;

public class MemberRepository {

    // Insert a member and return the database-generated ID
    public int addMember(Member member) throws SQLException {

        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        String sql = """
                INSERT INTO members (full_name, email, phone_number)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, member.getFullName());
            statement.setString(2, member.getEmail());
            statement.setString(3, member.getPhoneNumber());

            statement.executeUpdate();

            // Retrieve the generated member ID
            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    return keys.getInt(1);
                }

                throw new SQLException("Member ID was not generated");
            }
        }
    }

    // Find a member using their database ID
    public Member findMemberById(int memberId) throws SQLException {

        String sql = """
                SELECT member_id, full_name, email, phone_number
                FROM members
                WHERE member_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new Member(
                            result.getString("full_name"),
                            result.getString("email"),
                            result.getString("phone_number"),
                            result.getInt("member_id")
                    );
                }

                return null;
            }
        }
    }
}
