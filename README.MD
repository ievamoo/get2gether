# Get2gether Backend

Get2gether is a social event planning platform that helps users organize and manage group events efficiently. This repository contains the backend implementation of the Get2gether application.

## Architecture Overview

### Core Components

1. **Event Management System**
    - Event creation, modification, and deletion
    - Attendance tracking
    - Event notifications
    - Group-based event organization

2. **Group Management**
    - Group creation and management
    - Member management
    - Group-based permissions
    - Group notifications

3. **Invitation System**
    - Event invitations
    - Group invitations
    - Invitation status tracking
    - Real-time notifications

4. **User Management**
    - User authentication and authorization
    - Profile management
    - Availability tracking
    - User preferences

### Technical Stack

- **Framework**: Spring Boot
- **Database**: H2 (Production and Test)
- **Security**: Spring Security with JWT
- **Real-time Communication**: WebSocket (STOMP)
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven

## Project Structure

```
get2gether/
├── src/
│   ├── main/
│   │   ├── java/get2gether/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── enums/           # Enumeration classes
│   │   │   ├── event/           # Event-related classes
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── mapper/          # Object mappers
│   │   │   ├── model/           # Entity classes
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       └── application.properties  # Application configuration
│   └── test/                    # Test classes and resources
└── pom.xml                      # Maven configuration
```

## Key Features

### Event Management
- Create, update, and delete events
- Track event attendance
- Manage event details (date, time, location, etc.)
- Group-based event organization
- Real-time event notifications

### Group Management
- Create and manage groups
- Add/remove group members
- Set group permissions
- Group-based event organization
- Real-time group notifications

### Invitation System
- Send and manage invitations
- Track invitation status
- Real-time invitation notifications
- Group and event invitations

### User Features
- User registration and authentication
- Profile management
- Availability tracking
- Notification preferences
- User settings

## Setup and Installation

### Prerequisites
- Java 17 or higher
- Maven
- IDE (recommended: IntelliJ IDEA)

### Database Setup
The application uses H2 database for both production and test environments:
- Production: H2 in-memory database
- Test: H2 in-memory database with test data

### Application Setup
1. Clone the repository
2. Configure application.yml with your settings
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

### Authentication Endpoints
- POST `/auth/register` - Register new user
- POST `/auth/login` - User login

### User Endpoints
- GET `/user` - Get current user profile
- PUT `/user` - Update current user profile
- DELETE `/user` - Delete current user account
- GET `/user/availability` - Get user's available days
- PUT `/user/availability` - Update user's available days
- GET `/user/all` - Get all users (admin only)

### Event Endpoints
- GET `/events` - Get all events
- POST `/events` - Create new event
- GET `/events/{id}` - Get event by ID
- PATCH `/events/{eventId}` - Update event
- DELETE `/events/{eventId}` - Delete event
- PATCH `/events/{eventId}/status` - Toggle event attendance status

### Group Endpoints
- GET `/groups/{groupId}` - Get group by ID
- GET `/groups/{groupId}/events` - Get all events in a group
- POST `/groups` - Create new group
- PUT `/groups/{groupId}` - Update group name & color
- DELETE `/groups/{groupId}` - Delete group
- DELETE `/groups/{groupId}/members/{memberToDelete}` - Remove specific member from group
- DELETE `/groups/{groupId}/members` - Leave group

### Invitation Endpoints
- GET `/invites` - Get all invitations
- POST `/invites` - Create new group invitation
- PATCH `/invites/{inviteId}` - Respond to invitation (accept/decline)

## WebSocket Endpoints

### WebSocket Configuration
- WebSocket endpoint: `/ws`
- Message broker prefixes:
    - `/queue` - Point-to-point messaging
    - `/topic` - Publish-subscribe messaging
    - `/app` - Application-specific destinations
    - `/user` - User-specific destinations

### Chat Endpoints
- Send message: `/app/group/{groupId}/chat`
- Receive messages: `/topic/group/{groupId}/chat`

### Event Notifications
- `/topic/events` - Event updates
- `/topic/event-deleted` - Event deletion notifications
- `/topic/event-attendance` - Event attendance updates

### Group Notifications
- `/topic/group/{groupId}` - Group updates
- `/topic/group-deleted` - Group deletion notifications
- `/topic/group-members` - Group member updates

### Invitation Notifications
- `/topic/invites` - Invitation updates
- `/topic/invite-status` - Invitation status updates

## Testing

### Unit Tests
Run unit tests:
```bash
mvn test
```

### Integration Tests
Run integration tests:
```bash
mvn verify
```

## Security

The application uses Spring Security with JWT authentication. Key security features:
- JWT-based authentication
- Role-based access control
- Secure password hashing
- CORS configuration
- Rate limiting