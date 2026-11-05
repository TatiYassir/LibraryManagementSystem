package com.library.model;

public class Member {
	// Attributes :
	private String fullName;
	private String email,phoneNumber;
	private final int memberId;
	
	// Constructor :
	public Member(String fullName,String email,String phoneNumber, int memberId) {
		// Validate member information
		if (fullName == null || fullName.isBlank()) {
		    throw new IllegalArgumentException("Member name cannot be empty");
		}

		if (phoneNumber == null || phoneNumber.isBlank()) {
		    throw new IllegalArgumentException("Phone number cannot be empty");
		}

		if (memberId <= 0) {
		    throw new IllegalArgumentException("Invalid member ID");
		}
		this.fullName = fullName;
		if (!isValidEmail(email)) {
	        throw new IllegalArgumentException("Invalid email address");
	    }
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.memberId = memberId;
	}
	
	//getter :
	public String getFullName() {
		return fullName;
	}
	public String getEmail() {
		return email;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public int getMemberId() {
		return memberId;
	}
	
	//setter :
	public void setFullName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			throw new IllegalArgumentException("Member name cannot be empty");
		}
		this.fullName = fullName;
	}
	public void setPhoneNumber(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw new IllegalArgumentException("Phone number cannot be empty");
		}
		this.phoneNumber = phoneNumber;
	}
	public void setEmail(String email) {
		if (!isValidEmail(email)) {
	        throw new IllegalArgumentException("Invalid email address");
	    }
		this.email = email;
	}
	
	// Methods :
	private boolean isValidEmail(String email) {
	    if (email == null) {
	        return false;
	    }

	    return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	}
	@Override
	public String toString() {
		return "Member ID: " + memberId +
		       "\nName: " + fullName +
		       "\nE-mail: " + email +
		       "\nPhone: " + phoneNumber;
	}
	public void displayInfo() {
		
			System.out.println("The member info :");
			System.out.println("******************************");
			System.out.println("Full Name :"+fullName);
			System.out.println("Phone Number :"+phoneNumber);
			System.out.println("E-mail :"+email);
			System.out.println("Member Id :"+memberId);
	}
}
