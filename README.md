# MentorConnect - Backend without DTO

This version intentionally has **no dto folder**.

Architecture:
Postman -> Controller -> Service -> Repository -> Entity -> MySQL

## VS Code terminal

```powershell
.\\mvnw.cmd clean install
.\\mvnw.cmd spring-boot:run
```

Before running, edit `src/main/resources/application.properties` and replace `YOUR_MYSQL_PASSWORD` with your MySQL password.

## Main Postman APIs

POST `/api/tags` body `{ "name": "Java" }`

POST `/api/alumni` body `{ "name":"Arun", "email":"arun@gmail.com", "maxMentees":2, "interestTags":[{"id":1}] }`

POST `/api/students` body `{ "name":"Ravi", "email":"ravi@gmail.com", "department":"CSE", "interestTags":[{"id":1}] }`

GET `/api/mentorships/suggestions/student/1`

POST `/api/mentorships?studentId=1&alumniId=1`

POST `/api/sessions?mentorshipPairId=1` body `{ "topic":"Spring Boot", "sessionDate":"2026-09-29T10:00:00", "durationMinutes":60 }`

GET `/api/mentorships/report`
