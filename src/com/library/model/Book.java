package com.library.model;

import java.time.LocalDate;

public class Book {
	// Attributes :
	private String title, author;
	private final int bookId;
	private int  publicationYear;
	
	// Constructor :
	public Book(String title, String author, int bookId, int publicationYear) {

	    // Validate the book's descriptive information
	    if (title == null || title.isBlank()) {
	        throw new IllegalArgumentException("Book title cannot be empty");
	    }

	    if (author == null || author.isBlank()) {
	        throw new IllegalArgumentException("Book author cannot be empty");
	    }

	    // Validate identification and publication year
	    if (bookId <= 0) {
	        throw new IllegalArgumentException("Invalid book ID");
	    }

	    if (publicationYear <= 0 || publicationYear > LocalDate.now().getYear()) {
	        throw new IllegalArgumentException("Invalid publication year");
	    }

	    this.title = title;
	    this.author = author;
	    this.bookId = bookId;
	    this.publicationYear = publicationYear;
	}
	
	// Methods :
	public void displayInfo() {
		System.out.println("The book info :");
		System.out.println("*******************");
		System.out.println("Title : "+title+" Author : "+author+" Publication Year :"+publicationYear+" Book ID : "+bookId);
	}
	
	//getter
	public String getTitle() {
		return title;
	}
	public String getAuthor() {
		return author;
	}
	public int getBookId() {
		return bookId;
	}
	public int getPublicationYear() {
		return publicationYear;
	}
	
	//setter
	public void setTitle(String title) {
		this.title = title;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setPublicationYear(int publicationYear) {
		this.publicationYear = publicationYear;
	}
}
