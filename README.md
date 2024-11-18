# Lost and Found Application

This is a Lost and Found application built with Java, Spring Boot, and Maven. It allows users to upload PDF files containing lost items, authenticate users, and manage user roles.

## Technologies Used

- Java
- Spring Boot
- Maven
- H2 Database
- JavaScript
- SQL

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher
- Docker

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/your-username/lost-and-found.git
    cd lost-and-found
    ```

2. Build the project:
    ```sh
    mvn clean install
    ```

3. Run the application:
    ```sh
    mvn spring-boot:run
    ```

### Configuration

The application uses an in-memory H2 database for testing purposes. Configuration settings can be found in `src/test/resources/application.yml`.

### Running Tests

To run the tests, use the following command:
```sh
mvn test
```

### Running with Docker
Build the Docker image:
```docker compose -f compose.yaml build```.
Run the Docker container:  
```docker compose -f compose.yaml up```

This will start the application in a Docker container and map port 5432 of the container to port 5432 on your host machine.  
### Usage
#### Authentication

Users can authenticate by providing their username and password. Upon successful authentication, a JWT token is generated and stored in the local storage. 
The following users are pre-inserted in the database for testing purposes:
- Username: `user1`, Password: `password1`, Role: `ROLE_USER`
- Username: `admin1`, Password: `password2`, Role: `ROLE_ADMIN`

#### Uploading PDF Files
Users can upload PDF files containing lost items. The application extracts the content of the PDF and processes it.

#### API Endpoints
* `GET /authenticate`: Authenticates a user and returns a JWT token.
* `POST /register`: Registers a new user.
* `GET /lost-items`: Retrieves a list of lost items.
* `POST /lost-items`: Uploads a PDF file containing lost items.
* `GET /claims`: Retrieves a list of claims on lost items.
* `POST /claims`: Creates a new claim on a lost item.
* `GET /users`: Retrieves a list of users.
* `GET /roles`: Retrieves a list of roles.


### Project Structure
* src/main/java/com/taa/lostandfound: Contains the main application code.
* src/main/resources/static/js: Contains JavaScript files for the frontend.
* src/test/java/com/taa/lostandfound: Contains test cases for the application.
* src/test/resources: Contains test resources and configuration files.
