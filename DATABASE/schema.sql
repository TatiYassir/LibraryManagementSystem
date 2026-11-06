
-- Schema for the Library Management System (MariaDB)

CREATE DATABASE IF NOT EXISTS library_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE library_management;


-- 1. Books
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
	author VARCHAR(255) NOT NULL,
	publication_year INT NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;


-- 2. Members
CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30) NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;


-- 3. Physical book copies
CREATE TABLE IF NOT EXISTS book_copies (
    copy_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    KEY (book_id),

    FOREIGN KEY (book_id)
        REFERENCES books(book_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;


-- 4. Borrowing records
CREATE TABLE IF NOT EXISTS borrows (
    borrow_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    copy_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    expected_return_date DATE NOT NULL,
    actual_return_date DATE DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    KEY (member_id),
    KEY (copy_id),

    FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    FOREIGN KEY (copy_id)
        REFERENCES book_copies(copy_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;


-- 5. Reservations
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    book_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    expiration_date DATE DEFAULT NULL,
    assigned_copy_id INT DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',

    KEY (member_id),
    KEY (book_id),
    KEY (assigned_copy_id),

    FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    FOREIGN KEY (book_id)
        REFERENCES books(book_id),

    FOREIGN KEY (assigned_copy_id)
        REFERENCES book_copies(copy_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;
