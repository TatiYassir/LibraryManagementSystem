package com.library.ui;

import java.sql.SQLException;

import com.library.model.Member;
import com.library.repository.MemberRepository;

public class MemberMenu extends Menu {

    private final MemberRepository memberRepository = new MemberRepository();
    private final Pickers pickers;

    public MemberMenu(ConsoleInput in, Pickers pickers) {
        super(in);
        this.pickers = pickers;
    }

    @Override
    protected String title() {
        return "MEMBER MENU";
    }

    @Override
    protected String[] options() {
        return new String[] {
            "Add a member", "Search members", "Show all members",
            "Update a member", "Delete a member"
        };
    }

    @Override
    protected void handle(int choice) throws SQLException {
        switch (choice) {
            case 1 -> addMember();
            case 2 -> searchMembers();
            case 3 -> showAllMembers();
            case 4 -> updateMember();
            case 5 -> deleteMember();
        }
    }

    private void addMember() throws SQLException {
        String name = in.readLine("Full name: ");
        String email = in.readLine("E-mail: ");
        String phone = in.readLine("Phone number: ");

        // The constructor validates everything (ID 1 is a placeholder)
        Member member = new Member(name, email, phone, 1);

        int id = memberRepository.addMember(member);
        System.out.println("Member added with ID " + id + ".");
    }

    private void searchMembers() throws SQLException {
        String keyword = in.readNonEmpty("Search keyword: ");
        printList("SEARCH RESULTS", memberRepository.searchMembers(keyword),
                "No members found.", "Total members found");
    }

    private void showAllMembers() throws SQLException {
        printList("ALL MEMBERS", memberRepository.findAllMembers(),
                "No members registered.", "Total members");
    }

    private void updateMember() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        System.out.println("Press Enter to keep the current value.");

        String name = in.readLine("Full name [" + member.getFullName() + "]: ");
        String email = in.readLine("E-mail [" + member.getEmail() + "]: ");
        String phone = in.readLine(
                "Phone [" + member.getPhoneNumber() + "]: ");

        if (!name.isEmpty()) member.setFullName(name);
        if (!email.isEmpty()) member.setEmail(email);
        if (!phone.isEmpty()) member.setPhoneNumber(phone);

        memberRepository.updateMember(member);
        System.out.println("Member updated.");
    }

    private void deleteMember() throws SQLException {
        Member member = pickers.selectMember();
        if (member == null) return;

        if (!in.confirm("Delete " + member.getFullName() + "? (y/n): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        memberRepository.deleteMember(member.getMemberId());
        System.out.println("Member deleted.");
    }
}
