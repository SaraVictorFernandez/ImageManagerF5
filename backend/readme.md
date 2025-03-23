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
- Description: Upload an image file with name and description
- Request: 
  - Content-Type: multipart/form-data
  - Parameters:
    - `image` (file) - The image file to upload
    - `title` (string, optional) - Title for the image
    - `description` (string, optional) - Description of the image
- Response: 
  ```json
  {
    "id": 1,
    "filename": "abc123.jpg",
    "originalFilename": "vacation.jpg",
    "contentType": "image/jpeg",
    "fileSize": 1048576,
    "width": 1920,
    "height": 1080,
    "title": "My Vacation Photo",
    "description": "A beautiful sunset at the beach",
    "uploadDate": "2024-03-23T21:45:30",
    "lastModifiedDate": "2024-03-23T21:45:30",
    "url": "http://localhost:8080/uploads/abc123.jpg"
  }
  ```
- Error Responses:
  - 400 Bad Request: Invalid file type or empty file
  - 500 Internal Server Error: Upload failed

```http
GET /api/images
```
- Description: Get a list of all uploaded images metadata
- Response: Array of image DTOs
  ```json
  [
    {
      "id": 1,
      "filename": "abc123.jpg",
      "originalFilename": "vacation.jpg",
      "contentType": "image/jpeg",
      "fileSize": 1048576,
      "width": 1920,
      "height": 1080,
      "title": "My Vacation Photo",
      "description": "A beautiful sunset at the beach",
      "uploadDate": "2024-03-23T21:45:30",
      "lastModifiedDate": "2024-03-23T21:45:30",
      "url": "http://localhost:8080/uploads/abc123.jpg"
    },
    // ... more images
  ]
  ```

```http
GET /api/images/{id}
```
- Description: Get a specific image's metadata ID
- Parameters:
  - `id` (path variable): The ID of the image
- Response: Image DTO (same format as above)
- Error Responses:
  - 404 Not Found: Image not found

```http
PATCH /api/images/{id}
```
- Description: Update an image's metadata and/or file
- Parameters:
  - `id` (path variable): The ID of the image to update
  - `image` (file, optional) - New image file to replace the existing one
  - `title` (string, optional) - New title
  - `description` (string, optional) - New description
- Response: Updated image DTO
- Error Responses:
  - 404 Not Found: Image not found
  - 400 Bad Request: Invalid file type
  - 500 Internal Server Error: Update failed

```http
DELETE /api/images/{id}
```
- Description: Delete an image by its ID
- Parameters:
  - `id` (path variable): The ID of the image to delete
- Response: 204 No Content
- Error Responses:
  - 404 Not Found: Image not found
  - 500 Internal Server Error: Deletion failed

### Accessing Images

Images can be accessed directly through their URLs. When you upload an image, you'll receive the image's metadata including the URL in the response DTO. The URL will be in the format:
`http://localhost:8080/uploads/{filename}`

### Supported Image Types
- JPEG/JPG
- PNG
- GIF

### Image Metadata
Each image stored in the system includes:
- Unique identifier
- Original and stored filenames
- Content type
- File size
- Image dimensions (width and height)
- Title and description (optional)
- Upload and last modified dates
- Direct URL for access