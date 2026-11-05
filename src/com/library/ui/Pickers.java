package com.library.ui;

import java.sql.SQLException;
import java.util.ArrayList;

import com.library.enums.CopyStatus;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Member;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;

/**
 * Reusable "search, list, choose" flows shared by several menus.
 * Each method returns null when nothing was found.
 */
public class Pickers {

    private final ConsoleInput in;
    private final MemberRepository memberRepository = new MemberRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final BookCopyRepository copyRepository = new BookCopyRepository();

    public Pickers(ConsoleInput in) {
        this.in = in;
    }

    // Search members by name, email or phone, then let the user pick one
    public Member selectMember() throws SQLException {

        String keyword = in.readNonEmpty(
                "Search member by name, email, or phone: ");

        ArrayList<Member> members = memberRepository.searchMembers(keyword);

        if (members.isEmpty()) {
            System.out.println("No members found.");
            return null;
        }

        System.out.println("\n===== MATCHING MEMBERS =====");

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);

            System.out.println((i + 1) + ". " + member.getFullName()
                    + " | " + member.getEmail()
                    + " | " + member.getPhoneNumber());
        }

        int index = in.readIndex(
                "\nSelect a member (enter result number): ", members.size());

        Member selected = members.get(index);
        System.out.println("Selected member: " + selected.getFullName());

        return selected;
    }

    // Search books by title or author, then let the user pick one
    public Book selectBook() throws SQLException {

        String keyword = in.readNonEmpty("Search book by title or author: ");

        ArrayList<Book> books = bookRepository.searchBooks(keyword);

        if (books.isEmpty()) {
            System.out.println("No books found.");
            return null;
        }

        System.out.println("\n===== MATCHING BOOKS =====");

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);

            System.out.println((i + 1) + ". " + book.getTitle()
                    + " | " + book.getAuthor()
                    + " | " + book.getPublicationYear());
        }

        int index = in.readIndex(
                "\nSelect a book (enter result number): ", books.size());

        Book selected = books.get(index);
        System.out.println("Selected book: " + selected.getTitle());

        return selected;
    }

    // List the copies of a book (optionally only one status) and pick one.
    // Pass null as onlyStatus to list every copy.
    public BookCopy selectCopy(Book book, CopyStatus onlyStatus)
            throws SQLException {

        ArrayList<BookCopy> copies = new ArrayList<>();

        for (BookCopy copy : copyRepository.findCopiesByBookId(book.getBookId())) {
            if (onlyStatus == null || copy.getStatus() == onlyStatus) {
                copies.add(copy);
            }
        }

        if (copies.isEmpty()) {
            System.out.println(onlyStatus == null
                    ? "This book has no copies."
                    : "No copies with status " + onlyStatus + " for this book.");
            return null;
        }

        System.out.println("\n===== COPIES OF \"" + book.getTitle() + "\" =====");

        for (int i = 0; i < copies.size(); i++) {
            BookCopy copy = copies.get(i);

            System.out.println((i + 1) + ". Copy ID " + copy.getCopyId()
                    + " | " + copy.getStatus());
        }

        int index = in.readIndex(
                "\nSelect a copy (enter result number): ", copies.size());

        return copies.get(index);
    }
}
