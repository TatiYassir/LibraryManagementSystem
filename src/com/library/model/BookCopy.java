package com.library.model;
import com.library.enums.CopyStatus;

public class BookCopy {
	//Attributes :
	private final int copyId;
	private final Book book;
	private CopyStatus status;
	
	//Constructor : 
	public BookCopy(int copyId , Book book , CopyStatus status) {
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
		this.status = status;
	}
}
