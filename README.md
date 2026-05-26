# DocuMentor Backend

DocuMentor는 기술 문서를 읽고도 스스로 질문을 만들기 어려운 개발자 직군 취업 준비생을 위한 기술 문서 기반 면접/실무형 질문 생성 및 반복 학습 루틴 서비스입니다.

사용자가 PDF, Markdown, 텍스트 형태의 기술 문서를 업로드하면 RAG 기반으로 문서 내용을 분석하고, 개념형, 비교형, 실무 적용형, 트러블슈팅형 질문과 꼬리질문을 생성합니다. 생성된 질문은 문서 출처를 함께 제공해 근거를 확인할 수 있으며, 질문 리스트 공유와 이메일 복습 발송을 통해 개인 학습과 스터디 루틴을 지원합니다.

## Tech Stack

- Java 21
- Spring Boot 3.5.13
- Spring Web
- Spring Data JPA
- Bean Validation
- MySQL
- H2
- Gradle

## Package Convention

```text
com.documentor.backend
├── presentation
├── service
├── domain
└── infra
```

- `presentation`: controller, request/response DTO
- `service`: application use case, transaction flow
- `domain`: entity, domain model, business rule
- `infra`: persistence, external integration, technical detail

## MVP Scope

- 회원가입 / 로그인
- 문서 업로드
- 문서 목록 / 상세 조회
- 문서 기반 질문 생성
- 꼬리질문 생성
- 질문별 문서 출처 제공
- 질문 리스트 저장 / 조회
- 질문 리스트 공유 링크 생성 / 조회
- 이메일 복습 설정
- 이메일 발송 이력 저장

## Documents

- [API Specification](docs/API_SPEC.md)
