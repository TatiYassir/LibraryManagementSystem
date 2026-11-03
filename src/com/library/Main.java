package com.library;

import java.time.LocalDate;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.BookCopy;
import com.library.model.Borrow;
import com.library.enums.CopyStatus;

public class Main {
	public static void main (String [] args) {
		Book b1 = new Book("Clean Code","hendi",1,1965);
		Member m1 = new Member("TATI Yassir","yassir@gamil.com","0612034876",75);
		BookCopy bc = new BookCopy(123, b1, CopyStatus.AVAILABLE);

		Borrow bw = new Borrow(
		    147,
		    m1,
		    bc,
		    LocalDate.now().plusDays(14)
		);
		
	
	
	
	
	
	
	
	
	}
}
