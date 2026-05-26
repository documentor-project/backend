# API Specification

## Common Error Response

```json
{
  "code": "DOCUMENT_NOT_FOUND",
  "message": "문서를 찾을 수 없습니다.",
  "details": null
}
```

## Auth

### Sign Up

- Method: `POST`
- Path: `/api/auth/signup`
- Auth: Not required

Request:

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "승연"
}
```

Response:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "승연",
  "createdAt": "2026-05-26T09:00:00"
}
```

Validation:

- `email`: required, email format
- `password`: required, 8~64 characters
- `nickname`: required, 2~20 characters

### Login

- Method: `POST`
- Path: `/api/auth/login`
- Auth: Not required

Request:

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Refresh Token

- Method: `POST`
- Path: `/api/auth/refresh`
- Auth: Not required

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

Response:

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "new-jwt-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## User

### Get My Profile

- Method: `GET`
- Path: `/api/users/me`
- Auth: Required

Response:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "승연",
  "createdAt": "2026-05-26T09:00:00"
}
```

### Update My Profile

- Method: `PATCH`
- Path: `/api/users/me`
- Auth: Required

Request:

```json
{
  "nickname": "새닉네임"
}
```

Response:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "새닉네임",
  "updatedAt": "2026-05-26T10:00:00"
}
```

## Document

### Upload Document

- Method: `POST`
- Path: `/api/documents`
- Auth: Required
- Content-Type: `multipart/form-data`

Request:

```text
file: MultipartFile
title: "Spring Transaction 공식 문서"
```

Response:

```json
{
  "documentId": 10,
  "title": "Spring Transaction 공식 문서",
  "fileName": "spring-transaction.pdf",
  "fileType": "PDF",
  "status": "UPLOADED",
  "createdAt": "2026-05-26T09:00:00"
}
```

Validation:

- `file`: required
- allowed file types: `pdf`, `md`, `txt`
- `title`: required, 1~100 characters

### Get My Documents

- Method: `GET`
- Path: `/api/documents`
- Auth: Required

Query Parameters:

```text
page=0
size=20
```

Response:

```json
{
  "content": [
    {
      "documentId": 10,
      "title": "Spring Transaction 공식 문서",
      "fileType": "PDF",
      "status": "READY",
      "createdAt": "2026-05-26T09:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Get Document Detail

- Method: `GET`
- Path: `/api/documents/{documentId}`
- Auth: Required

Response:

```json
{
  "documentId": 10,
  "title": "Spring Transaction 공식 문서",
  "fileName": "spring-transaction.pdf",
  "fileType": "PDF",
  "status": "READY",
  "chunkCount": 42,
  "createdAt": "2026-05-26T09:00:00"
}
```

### Delete Document

- Method: `DELETE`
- Path: `/api/documents/{documentId}`
- Auth: Required

Response:

```text
204 No Content
```

## Question Generation

### Create Question Generation

- Method: `POST`
- Path: `/api/documents/{documentId}/question-generations`
- Auth: Required

Request:

```json
{
  "questionCount": 10,
  "difficulty": "BASIC",
  "field": "SPRING",
  "includeFollowUp": true,
  "questionTypes": [
    "CONCEPT",
    "COMPARISON",
    "PRACTICAL",
    "TROUBLESHOOTING"
  ]
}
```

Response:

```json
{
  "generationId": 100,
  "documentId": 10,
  "status": "PENDING",
  "requestedQuestionCount": 10,
  "createdAt": "2026-05-26T09:00:00"
}
```

Validation:

- `questionCount`: 1~30
- `difficulty`: `BEGINNER`, `BASIC`, `ADVANCED`
- `field`: `CS`, `SPRING`, `JPA`, `REDIS`, `DATABASE`, `NETWORK`, `OS`, `OTHER`
- `questionTypes`: at least one item
- document status must be `READY`

### Get Question Generation Status

- Method: `GET`
- Path: `/api/question-generations/{generationId}`
- Auth: Required

Response:

```json
{
  "generationId": 100,
  "documentId": 10,
  "status": "COMPLETED",
  "progress": 100,
  "createdQuestionCount": 8,
  "skippedQuestionCount": 2,
  "skipReason": "문서 근거가 부족한 항목은 생성하지 않았습니다.",
  "questionSetId": 50,
  "createdAt": "2026-05-26T09:00:00",
  "completedAt": "2026-05-26T09:01:30"
}
```

## Question Set

### Get Question Sets

- Method: `GET`
- Path: `/api/question-sets`
- Auth: Required

Query Parameters:

```text
documentId=10
page=0
size=20
```

Response:

```json
{
  "content": [
    {
      "questionSetId": 50,
      "title": "Spring Transaction 질문 세트",
      "documentId": 10,
      "documentTitle": "Spring Transaction 공식 문서",
      "questionCount": 8,
      "difficulty": "BASIC",
      "createdAt": "2026-05-26T09:01:30"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Get Question Set Detail

- Method: `GET`
- Path: `/api/question-sets/{questionSetId}`
- Auth: Required

Response:

```json
{
  "questionSetId": 50,
  "title": "Spring Transaction 질문 세트",
  "documentId": 10,
  "documentTitle": "Spring Transaction 공식 문서",
  "difficulty": "BASIC",
  "questions": [
    {
      "questionId": 1000,
      "type": "CONCEPT",
      "difficulty": "BASIC",
      "content": "Spring의 선언적 트랜잭션 관리 방식은 무엇이며 어떤 장점이 있나요?",
      "source": {
        "documentId": 10,
        "page": 3,
        "chunkIndex": 12,
        "snippet": "Declarative transaction management allows..."
      },
      "followUps": [
        {
          "followUpQuestionId": 2000,
          "content": "프록시 기반 AOP에서 self-invocation이 트랜잭션에 미치는 영향은 무엇인가요?"
        }
      ],
      "createdAt": "2026-05-26T09:01:30"
    }
  ],
  "createdAt": "2026-05-26T09:01:30"
}
```

### Update Question Set Title

- Method: `PATCH`
- Path: `/api/question-sets/{questionSetId}`
- Auth: Required

Request:

```json
{
  "title": "Spring 트랜잭션 면접 질문"
}
```

Response:

```json
{
  "questionSetId": 50,
  "title": "Spring 트랜잭션 면접 질문",
  "updatedAt": "2026-05-26T10:00:00"
}
```

### Delete Question Set

- Method: `DELETE`
- Path: `/api/question-sets/{questionSetId}`
- Auth: Required

Response:

```text
204 No Content
```

## Question

### Get Question Detail

- Method: `GET`
- Path: `/api/questions/{questionId}`
- Auth: Required

Response:

```json
{
  "questionId": 1000,
  "questionSetId": 50,
  "type": "CONCEPT",
  "difficulty": "BASIC",
  "content": "Spring의 선언적 트랜잭션 관리 방식은 무엇이며 어떤 장점이 있나요?",
  "source": {
    "documentId": 10,
    "documentTitle": "Spring Transaction 공식 문서",
    "page": 3,
    "chunkIndex": 12,
    "snippet": "Declarative transaction management allows..."
  },
  "followUps": [
    {
      "followUpQuestionId": 2000,
      "content": "프록시 기반 AOP에서 self-invocation이 트랜잭션에 미치는 영향은 무엇인가요?"
    }
  ],
  "bookmarked": false,
  "answered": false
}
```

### Update Question Bookmark

- Method: `PATCH`
- Path: `/api/questions/{questionId}/bookmark`
- Auth: Required

Request:

```json
{
  "bookmarked": true
}
```

Response:

```json
{
  "questionId": 1000,
  "bookmarked": true
}
```

### Update Question Answer Status

- Method: `PATCH`
- Path: `/api/questions/{questionId}/answer-status`
- Auth: Required

Request:

```json
{
  "answered": true
}
```

Response:

```json
{
  "questionId": 1000,
  "answered": true,
  "answeredAt": "2026-05-26T10:00:00"
}
```

## Share Link

### Create Share Link

- Method: `POST`
- Path: `/api/question-sets/{questionSetId}/share-links`
- Auth: Required

Request:

```json
{
  "expiresAt": "2026-06-26T23:59:59"
}
```

Response:

```json
{
  "shareId": 300,
  "shareToken": "abc123xyz",
  "shareUrl": "https://documentor.site/share/abc123xyz",
  "expiresAt": "2026-06-26T23:59:59",
  "createdAt": "2026-05-26T10:00:00"
}
```

### Get Shared Question Set

- Method: `GET`
- Path: `/api/shared-question-sets/{shareToken}`
- Auth: Not required

Response:

```json
{
  "questionSetId": 50,
  "title": "Spring 트랜잭션 면접 질문",
  "documentTitle": "Spring Transaction 공식 문서",
  "difficulty": "BASIC",
  "questions": [
    {
      "questionId": 1000,
      "type": "CONCEPT",
      "content": "Spring의 선언적 트랜잭션 관리 방식은 무엇이며 어떤 장점이 있나요?",
      "source": {
        "page": 3,
        "snippet": "Declarative transaction management allows..."
      },
      "followUps": [
        {
          "content": "프록시 기반 AOP에서 self-invocation이 트랜잭션에 미치는 영향은 무엇인가요?"
        }
      ]
    }
  ]
}
```

### Delete Share Link

- Method: `DELETE`
- Path: `/api/share-links/{shareId}`
- Auth: Required

Response:

```text
204 No Content
```

## Notification Setting

### Get My Notification Setting

- Method: `GET`
- Path: `/api/notification-settings/me`
- Auth: Required

Response:

```json
{
  "enabled": true,
  "email": "user@example.com",
  "sendTime": "09:00",
  "questionCount": 3,
  "questionSetId": 50,
  "questionSetTitle": "Spring 트랜잭션 면접 질문",
  "updatedAt": "2026-05-26T10:00:00"
}
```

### Save My Notification Setting

- Method: `PUT`
- Path: `/api/notification-settings/me`
- Auth: Required

Request:

```json
{
  "enabled": true,
  "email": "user@example.com",
  "sendTime": "09:00",
  "questionCount": 3,
  "questionSetId": 50
}
```

Response:

```json
{
  "enabled": true,
  "email": "user@example.com",
  "sendTime": "09:00",
  "questionCount": 3,
  "questionSetId": 50,
  "updatedAt": "2026-05-26T10:00:00"
}
```

Validation:

- `email`: required when `enabled` is `true`
- `sendTime`: `HH:mm`
- `questionCount`: 1~10
- `questionSetId`: must be owned by current user

## Review Delivery

### Get Review Deliveries

- Method: `GET`
- Path: `/api/review-deliveries`
- Auth: Required

Query Parameters:

```text
page=0
size=20
```

Response:

```json
{
  "content": [
    {
      "deliveryId": 700,
      "questionSetId": 50,
      "questionSetTitle": "Spring 트랜잭션 면접 질문",
      "email": "user@example.com",
      "questionCount": 3,
      "status": "SENT",
      "sentAt": "2026-05-26T09:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## Enums

### DocumentStatus

```text
UPLOADED
PARSING
EMBEDDING
READY
FAILED
```

### QuestionField

```text
CS
SPRING
JPA
REDIS
DATABASE
NETWORK
OS
OTHER
```

### QuestionDifficulty

```text
BEGINNER
BASIC
ADVANCED
```

### QuestionType

```text
CONCEPT
COMPARISON
PRACTICAL
TROUBLESHOOTING
```

### GenerationStatus

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

### DeliveryStatus

```text
PENDING
SENT
FAILED
```

## MVP Implementation Priority

1. Auth
2. Document upload and parsing status
3. Question generation request and status
4. Question set and question read APIs
5. Share link APIs
6. Notification setting APIs
7. Review delivery history
