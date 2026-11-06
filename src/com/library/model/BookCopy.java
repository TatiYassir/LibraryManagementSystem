package com.library.model;
import com.library.enums.CopyStatus;

public class BookCopy {
	//Attributes :
	private final int copyId;
	private final Book book;
	private CopyStatus status;
	
	//Constructor : 
	public BookCopy(int copyId, Book book, CopyStatus status) {

	    // A physical copy must belong to a book
	    if (book == null) {
	        throw new IllegalArgumentException("Book cannot be null");
	    }

	    // Every copy must have an initial status
	    if (status == null) {
	        throw new IllegalArgumentException("Copy status cannot be null");
	    }
	    if (copyId <= 0) {
	        throw new IllegalArgumentException("Invalid copy ID");
	    }
	    this.copyId = copyId;
	    this.book = book;
	    this.status = status;
	}
	
	//getter :
	public int getCopyId() {
		return copyId;
	}
	public Book getBook() {
		return book;
	}
	public CopyStatus getStatus() {
		return status;
	}
	
	//setter :
	public void setStatus(CopyStatus status) {
	    if (status == null) {
	        throw new IllegalArgumentException("Copy status cannot be null");
	    }

	    this.status = status;
	}

	@Override
	public String toString() {
		return "Copy ID: " + copyId +
		       "\nBook: " + book.getTitle() +
		       "\nStatus: " + status;
	}
}
