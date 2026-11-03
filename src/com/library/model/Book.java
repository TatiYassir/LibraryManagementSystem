package com.library.model;

public class Book {
	// Attributes :
	private String title, author;
	private final int bookId;
	private int  publicationYear;
	
	// Constructor :
	public Book(String title, String author, int bookId, int publicationYear) {
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
