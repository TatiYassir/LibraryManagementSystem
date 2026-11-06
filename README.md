# 📚 Library Management System

A **multi-layer Java application** integrated with **MariaDB** for managing library operations, including books, physical copies, members, borrowings, and reservations.

The system implements a **FIFO reservation queue**, automatic copy reassignment, borrowing limits, and reservation expiration management.

---

## ✨ Features

### 📖 1. Book & Copy Management
- Register, update, search, and delete books.
- Manage individual physical book copies.
- Track copy availability through five statuses:
  - `AVAILABLE`
  - `BORROWED`
  - `RESERVED`
  - `UNDER_MAINTENANCE`
  - `LOST`
- Automatically assign available copies to waiting reservations.
- Preserve physical copy records to maintain borrowing and reservation history.

### 👥 2. Member Management
- Register and manage library members.
- Validate member information, including email format and required fields.
- Track each member's borrowing and reservation history.

### 🔄 3. Borrowing System
- Borrow and return physical book copies.
- Enforce a maximum of **3 outstanding borrowings per member**.
- Track borrowing dates, expected return dates, and actual return dates.
- Automatically update overdue borrowing statuses.
- Synchronize borrowing records and physical copy statuses with MariaDB.

### 📋 4. Reservation System
- Implement a **First-In, First-Out (FIFO)** reservation queue.
- Automatically assign available copies to the oldest waiting reservation.
- Allow members to cancel reservations.
- Apply a **3-day pickup period** after a copy is assigned.
- Automatically expire overdue pickups.
- Reassign released copies to the next waiting reservation.

### 📊 5. Reports
- Display library statistics.
- Show physical copies grouped by status.
- Display overdue borrowings.
- Report expired reservations and copy reassignment results.

---

## 🏗️ Architecture

The application follows a layered architecture that separates user interaction, business logic, database operations, and data representation.

```text
+-------------------------------------------------------+
|                       UI LAYER                        |
|                                                       |
|       Main | Menus | ConsoleInput | Pickers           |
+---------------------------+---------------------------+
                            |
                            v
+-------------------------------------------------------+
|                    SERVICE LAYER                      |
|                                                       |
|          BorrowService | ReservationService           |
+---------------------------+---------------------------+
                            |
                            v
+-------------------------------------------------------+
|                  REPOSITORY LAYER                     |
|                                                       |
| BookRepository | BookCopyRepository | MemberRepository|
| BorrowRepository | ReservationRepository              |
+---------------------------+---------------------------+
                            |
                            v
+-------------------------------------------------------+
|                    DATABASE LAYER                     |
|                                                       |
|              MariaDB + JDBC Driver                    |
|                 library_management                    |
+-------------------------------------------------------+
```

### Project Structure

```text
LibraryManagementSystem/
│
├── DATABASE/
│   └── schema.sql
│
├── lib/
│   └── mariadb-java-client.jar
│
├── src/
│   ├── module-info.java
│   └── com/
│       └── library/
│           ├── Main.java
│           │
│           ├── enums/
│           │   ├── BorrowStatus.java
│           │   ├── CopyStatus.java
│           │   └── ReservationStatus.java
│           │
│           ├── model/
│           │   ├── Book.java
│           │   ├── BookCopy.java
│           │   ├── Borrow.java
│           │   ├── ExpiredReservationResult.java
│           │   ├── Member.java
│           │   └── Reservation.java
│           │
│           ├── repository/
│           │   ├── BookCopyRepository.java
│           │   ├── BookRepository.java
│           │   ├── BorrowRepository.java
│           │   ├── DatabaseConnection.java
│           │   ├── MemberRepository.java
│           │   └── ReservationRepository.java
│           │
│           ├── service/
│           │   ├── BorrowService.java
│           │   └── ReservationService.java
│           │
│           └── ui/
│               ├── Main.java
│               ├── Menu.java
│               ├── BookMenu.java
│               ├── MemberMenu.java
│               ├── CopyMenu.java
│               ├── BorrowingMenu.java
│               ├── ReservationMenu.java
│               ├── ReportsMenu.java
│               ├── ReservationReport.java
│               ├── Pickers.java
│               └── ConsoleInput.java
│
├── screenshots/
│
└── README.md
```

---

## 🛠️ Technologies

| Technology | Purpose |
|---|---|
| Java 17+ | Application development |
| MariaDB | Relational database |
| JDBC | Database connectivity |
| SQL | Data storage and queries |
| OOP | Application design and organization |

---

## ⚙️ Requirements

Before running the application, make sure you have:

- **JDK 17 or later**
- **MariaDB Server**
- **MariaDB JDBC Driver** (`org.mariadb.jdbc`)
- A Java IDE or command-line environment

---

## 🗄️ Database Design

The application uses a MariaDB database named `library_management`, containing five tables:

| Table | Description |
|---|---|
| `books` | Stores book information |
| `book_copies` | Stores individual physical copies |
| `members` | Stores library member information |
| `borrows` | Stores borrowing transactions |
| `reservations` | Stores reservation records |

### Entity Relationships

- One book can have multiple physical copies.
- One member can have multiple borrowing records.
- One physical copy can appear in multiple borrowing records over time.
- One member can create multiple reservations.
- One book can have multiple reservations.
- A reservation can optionally have an assigned physical copy.

All tables use primary keys, and relationships are enforced using foreign keys.

### Database Schema

The SQL schema is available in:

`DATABASE/schema.sql`

---

## 🔐 Database Configuration

The application connects to MariaDB through:

`com.library.repository.DatabaseConnection`

Adjust the connection credentials if necessary:

```java
private static final String URL =
    "jdbc:mariadb://localhost:3306/library_management?sslMode=disable";

private static final String USER = "root";
private static final String PASSWORD = "";
```

Make sure MariaDB is running and the database schema has been imported before launching the application.

---

## ▶️ Compilation & Execution

### 1. Clone the repository

```powershell
git clone https://github.com/TatiYassir/LibraryManagementSystem.git
```

### 2. Navigate to the project directory

```powershell
cd LibraryManagementSystem
```

### 3. Create the database

Open MariaDB and execute the SQL schema:

```sql
CREATE DATABASE library_management;
USE library_management;
```

Then import `DATABASE/schema.sql` using your preferred MariaDB client.

### 4. Configure the JDBC driver

Make sure the MariaDB JDBC driver is available in the project's `lib` directory and correctly configured in the module path.

### 5. Compile the project

For a Windows PowerShell environment:

```powershell
$files = Get-ChildItem -Recurse src -Filter *.java |
    ForEach-Object { $_.FullName }

javac -d bin --module-path lib $files
```

### 6. Run the application

```powershell
java --module-path "bin;lib" -m LibraryManagementSystem/com.library.ui.Main
```

> **Note:** The module name, main class, and compilation commands must match the actual `module-info.java` and project configuration. The MariaDB JDBC driver must be accessible through the module path.

---

## 📸 Screenshots

The following screenshots demonstrate the application's main features and database design.

### 1. Main Menu
![Main Menu](Screenshots/01-main-menu.png)

### 2. All Books
![All Books](Screenshots/02-all-books.png)

### 3. Book Menu
![Book Menu](Screenshots/02-book-menu.png)

### 4. Physical Book Copies
![All Book Copies](Screenshots/03-all-book-copies.png)

### 5. Borrowing History
![Borrowing History](Screenshots/04-borrowing-history.png)

### 6. Successful Borrowing
![Successful Borrowing](Screenshots/05-successful-borrowing.png)

### 7. Borrowing History After New Borrowing
![Borrowing History After New Borrowing](Screenshots/06-borrowing-history-after-new-borrowing.png)

### 8. Successful Return
![Successful Return](Screenshots/06-successful-return.png)

### 9. Reservation History
![Reservation History](Screenshots/07-reservation-history.png)

### 10. Library Statistics
![Library Statistics](Screenshots/08-library-statistics.png)

### 11. Database Structure Diagram
![Database Structure Diagram](Screenshots/mermaid-diagram.png)
![Database Structure](screenshots/10-database-structure.png)

---

## 🔁 Domain Entity Lifecycles

### Book Copy Statuses

A physical book copy can have one of the following statuses:

```text
AVAILABLE
BORROWED
RESERVED
UNDER_MAINTENANCE
LOST
```

The status changes according to borrowing, reservation, return, and administrative operations.

### Reservation Status Lifecycle

```text
                 +------------------+
                 |     WAITING      |
                 +------------------+
                          |
                   Copy assigned
                          v
                 +------------------+
                 |  READY_FOR_PICKUP|
                 +------------------+
                    |           |
              Collection    Deadline passed
                    |           |
                    v           v
              +-----------+  +-----------+
              | FULFILLED |  |  EXPIRED  |
              +-----------+  +-----------+

WAITING / READY_FOR_PICKUP ---> CANCELLED
```

---

## 🧠 Core Business Rules

| Rule | Description |
|---|---|
| Borrowing limit | Maximum 3 outstanding borrowings per member |
| Reservation order | FIFO — oldest waiting reservation first |
| Pickup deadline | 3 days after a copy is assigned |
| Reservation collection | Only reservations ready for pickup can be collected |
| Copy allocation | Available copies are automatically assigned to waiting reservations |
| Reservation expiration | Expired reservations release their assigned copies for reassignment |
| Copy history | Physical copy records are retained rather than deleted |

---

## 🎯 Project Goals

This project was developed to apply and strengthen practical knowledge of:

- Object-Oriented Programming (OOP)
- Java application architecture
- Separation of concerns
- JDBC and relational database integration
- SQL queries and transactions
- Business-rule validation
- Collections and exception handling
- Entity relationships and status management

---

**Developed as an academic Java project.**
