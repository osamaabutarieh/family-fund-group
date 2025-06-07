# Family Fund Box Management System

A web-based application for managing family fund boxes, subscriptions, and financial advances.

## Features

- Member Management
  - Add, edit, and manage member information
  - Track member status (active/inactive)

- Subscription Management
  - Monthly subscription tracking
  - Payment recording and cancellation
  - Payment status tracking (paid/unpaid)

- Financial Advances
  - Issue financial advances to members
  - Track advance payments and remaining balances
  - Payment recording and status management

- Financial Management
  - Transaction tracking
  - Financial overview dashboard
  - Export functionality (Excel/PDF)

## Technology Stack

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- Thymeleaf with Layout Dialect
- MySQL Database
- AdminLTE 3 for UI
- Maven for dependency management

## Getting Started

### Prerequisites

- Java 17 or later
- MySQL 8.0 or later
- Maven 3.6 or later

### Database Configuration

The application uses MySQL. Update the database configuration in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/familyfunddb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application

1. Clone the repository
```bash
git clone [repository-url]
```

2. Navigate to the project directory
```bash
cd FamilyFundGroup
```

3. Run the application using Maven
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## Project Structure

```
src/main/
├── java/com/familyfund/
│   ├── config/         # Configuration classes
│   ├── controller/     # Web controllers
│   ├── model/          # Entity classes
│   ├── repository/     # Data repositories
│   ├── service/        # Business logic
│   └── FamilyFundApplication.java
└── resources/
    ├── templates/      # Thymeleaf templates
    ├── static/         # Static resources
    └── application.properties
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details 