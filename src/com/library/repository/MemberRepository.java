package com.library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    	if (memberId <= 0) {
    	    throw new IllegalArgumentException("Invalid member ID");
    	}
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
 // Search members by name, email, or phone number
    public ArrayList<Member> searchMembers(String keyword)
            throws SQLException {

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty");
        }

        String sql = """
                SELECT member_id, full_name, email, phone_number
                FROM members
                WHERE LOWER(full_name) LIKE LOWER(?)
                   OR LOWER(email) LIKE LOWER(?)
                   OR phone_number LIKE ?
                ORDER BY full_name
                """;

        ArrayList<Member> members = new ArrayList<>();

        String searchPattern = "%" + keyword.trim() + "%";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Member member = new Member(
                            result.getString("full_name"),
                            result.getString("email"),
                            result.getString("phone_number"),
                            result.getInt("member_id")
                    );

                    members.add(member);
                }
            }
        }

        return members;
    }
    // Update an existing member's information
    public void updateMember(Member member) throws SQLException {

        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        String sql = """
                UPDATE members
                SET full_name = ?, email = ?, phone_number = ?
                WHERE member_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, member.getFullName());
            statement.setString(2, member.getEmail());
            statement.setString(3, member.getPhoneNumber());
            statement.setInt(4, member.getMemberId());

            statement.executeUpdate();
        }
    }
 // Retrieve all registered members
    public ArrayList<Member> findAllMembers() throws SQLException {

        String sql = """
                SELECT member_id, full_name, email, phone_number
                FROM members
                ORDER BY member_id
                """;

        ArrayList<Member> members = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Member member = new Member(
                        result.getString("full_name"),
                        result.getString("email"),
                        result.getString("phone_number"),
                        result.getInt("member_id")
                );

                members.add(member);
            }
        }

        return members;
    }
 // Delete a member using their database ID
    public void deleteMember(int memberId) throws SQLException {
    	if (memberId <= 0) {
    	    throw new IllegalArgumentException("Invalid member ID");
    	}
        String sql = """
                DELETE FROM members
                WHERE member_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
        	
            statement.setInt(1, memberId);
            statement.executeUpdate();
        }
    }
}
