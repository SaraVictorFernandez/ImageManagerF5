# F5 Tech Test API

A Spring Boot REST API service that handles image upload functionality, with plans to expand features and add a React frontend.

## Project Status

🚧 **Currently in Development** 🚧

- ✅ Backend initialization with Spring Boot
- ✅ Basic image upload functionality
- 🔄 Additional API endpoints (in progress)
- 📝 React frontend (planned)

## Prerequisites

- Java 17 or higher
- Gradle 7.0+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)
- Git

## Getting Started

1. Clone the repository:
```bash
git clone [repository-url-tbd]
```

2. Navigate to the project directory:
```bash
cd [project-name-tbd]
```

3. Build the project:
```bash
./gradlew build
```

4. Run the application:
```bash
./gradlew bootRun
```

The server will start on `http://localhost:8080`

## Testing

To run the tests:
```bash
./gradlew test
```

## API Endpoints

### Hello World Endpoints

```http
GET /hello
```
- Description: Returns a simple "Hello, World!" message
- Response: 
  ```json
  "Hello, World!"
  ```

```http
GET /hello/{name}
```
- Description: Returns a personalized hello message
- Parameters:
  - `name` (path variable): Name to include in the greeting
- Response: 
  ```json
  "Hello, {name}!"
  ```

### Image Management Endpoints

```http
POST /api/images
```
- Description: Upload an image file
- Request: 
  - Content-Type: multipart/form-data
  - Parameter: `image` (file) - The image file to upload
- Response: 
  ```json
  {
    "url": "/uploads/{filename}"
  }
  ```
- Error Responses:
  - 400 Bad Request: Invalid file type or empty file
  - 500 Internal Server Error: Upload failed

```http
GET /api/images
```
- Description: Get a list of all uploaded images
- Response: 
  ```json
  {
    "urls": ["/uploads/file1.jpg", "/uploads/file2.png", ...]
  }
  ```

```http
DELETE /api/images
```
- Description: Delete an image by its filename
- Parameters:
  - `fileName` (path variable): The filename of the image to delete (e.g., "abc123.jpg")
- Response: 204 No Content
- Error Responses:
  - 404 Not Found: Image not found
  - 500 Internal Server Error: Deletion failed


### Supported Image Types
- JPEG
- PNG
- GIF