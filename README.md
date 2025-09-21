# Resume File Parser (RFP)

A Spring Boot application for parsing and managing resumes in PDF and DOCX formats. Extracts contact information,
skills, work experience, and education details from uploaded resumes.

## Technology Stack

- **Backend**:
    - Java 17
    - Spring Boot 3.x
    - Spring Data MongoDB
    - MongoDB (with GridFS for file storage)
    - Apache PDFBox (for PDF parsing)
    - Apache POI (for DOCX parsing)

- **Frontend**:
    - Thymeleaf templates
    - Bootstrap 5

- **Build Tool**: Maven

## Features

- Upload resumes in PDF or DOCX format
- Automatic parsing of resume content
- Search resumes by name, email, or skills
- Download original resume files
- View parsed resume details

## How to Run the Application

### Prerequisites

- Java 17 JDK installed
- MongoDB server running locally or accessible
- Maven installed (for building)

### Running the Application

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd rfp

Configure MongoDB:
Ensure MongoDB is running (default configuration expects MongoDB at localhost:27017)
Or update application.properties with your MongoDB connection details
Build and run:

mvn clean install
mvn spring-boot:run

Access the application:
Open http://localhost:8080 in your browser

## API Endpoints

### Web UI Endpoints

| Endpoint                  | Method | Description                   |
|---------------------------|--------|-------------------------------|
| `/resumes/upload`         | GET    | Show resume upload form       |
| `/resumes/upload`         | POST   | Process uploaded resume file  |
| `/resumes/search`         | GET    | Show search form              |
| `/resumes/search/results` | GET    | Show search results           |
| `/resumes/preview/{id}`   | GET    | Preview a specific resume     |
| `/resumes/download/{id}`  | GET    | Download original resume file |

### REST API Endpoints

| Endpoint                        | Method | Description                       |
|---------------------------------|--------|-----------------------------------|
| `/api/resumes`                  | POST   | Upload and parse a resume         |
| Request Body:                   |        | `multipart/form-data` with file   |
| Response:                       |        | `201 Created` with resume details |
| `/api/resumes/{id}`             | GET    | Get resume details by ID          |
| Response:                       |        | `200 OK` with resume JSON         |
| `/api/resumes/search?q={query}` | GET    | Search resumes                    |
| Query Params:                   |        | `q`: search term                  |
| Response:                       |        | `200 OK` with array of matches    |
| `/api/resumes/{id}/file`        | GET    | Download original resume file     |
| Response:                       |        | `200 OK` with file attachment     |

### Request/Response Examples

**Upload Resume (POST `/api/resumes`)**

```json
// Request (form-data)
file: <resume.pdf>

// Response (201 Created)
{
"id": "65a1b2c3d4e5f6g7h8i9j0",
"name": "John Doe",
"email": "john.doe@example.com",
"skills": ["Java", "Spring Boot", "MongoDB"],
"fileSize": "245KB",
"fileType": "application/pdf"
}


// Response (200 OK)
{
"id": "65a1b2c3d4e5f6g7h8i9j0",
"name": "John Doe",
"email": "john.doe@example.com",
"phone": "+1 (555) 123-4567",
"skills": ["Java", "Spring Boot", "MongoDB"],
"experience": [
{
"company": "Tech Corp",
"position": "Software Engineer",
"duration": "2020-Present"
}
],
"education": [
{
"institution": "State University",
"degree": "B.S. Computer Science",
"year": 2020
}
]
}

Error Responses
| Status Code | Description |
|-------------|------------------------------|
| 400 | Invalid file format |
| 404 | Resume not found |
| 500 | Internal server error |
```

# MongoDB configuration

spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=rfp

# Server port

server.port=8080

# File upload settings

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

This README provides:

1. Clear project description
2. Complete technology stack information
3. Detailed setup and running instructions
4. Comprehensive API endpoint documentation
5. Configuration details
6. Future enhancement ideas

You can customize it further by adding:

- Screenshots of the UI
- Contribution guidelines
- License information
- Known issues
- Deployment instructions for production
