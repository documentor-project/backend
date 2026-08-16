# DocuMentor RAG 고도화 개발 계획

## 1. 목표와 범위

DocuMentor의 문서 기반 기술면접 질문 생성 기능을 품질 측정 가능하고 운영 가능한 RAG 파이프라인으로 고도화한다.

이번 계획은 다음 범위를 포함한다.

- 문서 구조를 보존하는 청킹과 메타데이터 확장
- 검색 품질을 측정하고 개선하는 평가 체계
- 영속 벡터 저장소 도입과 문서-벡터 정합성 관리
- 질문 생성 작업(Job)의 영속화와 재시도 처리
- 통합 테스트, 실패 사례, 개선 전후 결과 문서화

답변 피드백, 복습 기능, 새로운 문서 형식 지원은 이번 고도화 범위에서 제외한다.

## 2. 현재 구조와 개선 대상

| 영역 | 현재 구현 | 개선 목표 |
|---|---|---|
| 벡터 저장 | `SimpleVectorStore` 인메모리 저장 | pgvector 또는 Qdrant 기반 영속 저장 |
| 청킹 | 기본 `TokenTextSplitter` | 파일 형식과 문서 구조를 반영한 분할 |
| 메타데이터 | 문서 ID, 청크 순서, 파일 형식, 페이지 | 사용자 ID, 섹션 제목, 원본 위치까지 확장 |
| 검색 | 사용자 조건을 문자열로 결합해 top-K 검색 | 검색용 질의 구성, 중복 제거, 선택적 reranking |
| 질문 생성 상태 | 메모리의 `ConcurrentHashMap` | DB 기반 Job 상태, 실패 사유, 재시도 이력 |
| 품질 검증 | 단위 테스트 일부 | Golden dataset, 검색·생성 평가, 통합 테스트 |

## 3. 목표 아키텍처

```text
문서 업로드
  -> 문서 파싱
  -> 형식별 구조 인식 청킹
  -> 청크 메타데이터 부여
  -> 임베딩
  -> 영속 벡터 저장소

질문 생성 요청
  -> Generation Job 생성(PENDING)
  -> 검색용 질의 구성
  -> 벡터 검색 + 문서/사용자 필터
  -> 중복 제거 + 선택적 reranking
  -> 근거 청크 기반 LLM 질문 생성
  -> QuestionSet 저장
  -> Job 상태 완료 또는 실패 기록
```

## 4. 기술 결정

### 4.1 벡터 저장소

1차 선택은 **pgvector**로 한다.

- 관계형 데이터와 운영 데이터를 함께 다루는 현재 Spring Boot 서비스 구조에 잘 맞는다.
- 컨테이너로 로컬 개발 환경을 구성하기 쉽다.
- 문서 ID와 사용자 ID 기반 필터링, 인덱스 관리, 백업 전략을 한 데이터베이스 생태계에서 운영할 수 있다.

Qdrant는 대규모 벡터 검색 성능이나 벡터 검색 기능을 더 깊이 다뤄야 할 때 대안으로 검토한다. 이번 프로젝트에서는 저장소 교체 자체보다 데이터 정합성, 검색 품질, 운영 검증을 완성하는 데 우선순위를 둔다.

### 4.2 평가 방식

평가는 LLM 판정에만 의존하지 않는다. 사람이 정의한 정답 근거 청크를 기준으로 검색 품질을 먼저 측정하고, 생성 결과는 근거성·중복·질문 조건 충족 여부를 함께 검토한다.

### 4.3 구현 원칙

- 개선 전 baseline을 먼저 저장한다.
- 검색 로직의 모든 변경은 동일한 평가셋으로 전후 비교한다.
- 비동기 처리 결과와 실패 원인은 재시작 후에도 남아야 한다.
- 기존 API의 요청·응답 계약은 가능한 한 유지한다.
- 외부 API 오류와 데이터 처리 오류를 구분한다.

## 5. 단계별 개발 계획

### Phase 0. 준비 및 baseline 측정

**목적:** 개선 효과를 증명할 기준을 만든다.

작업:

- 평가 문서 5~10개를 선정한다. PDF, Markdown, TXT와 코드 블록이 있는 문서를 포함한다.
- 문서별 평가 질의 5개 내외를 작성한다.
- 각 질의에 대해 기대 근거 문서, 페이지, 섹션, 청크 범위를 기록한다.
- 현재 검색 결과와 질문 생성 결과를 JSON 또는 CSV로 저장한다.
- `Recall@3`, `Recall@5`, 출처 일치율, 중복 질문 비율, 생성 실패율을 계산한다.

권장 디렉터리:

```text
backend/src/test/resources/rag-evaluation/
  documents/
  golden-dataset.json
  baseline-results.json
docs/rag-evaluation/
  baseline-report.md
```

완료 기준:

- 최소 25개 이상의 평가 질의가 존재한다.
- 모든 평가 질의에 기대 근거가 기록되어 있다.
- 현재 구현의 지표와 대표 실패 사례 3건이 문서화되어 있다.

### Phase 1. 청킹과 메타데이터 개선

**목적:** 검색 단위가 문서 의미 단위와 최대한 일치하도록 만든다.

작업:

- `SpringAiDocumentVectorIndexer`에서 파일 형식별 splitter를 선택하도록 분리한다.
- Markdown은 heading을 기준으로 섹션을 나누고, 길이가 긴 섹션만 토큰 단위로 재분할한다.
- PDF는 페이지 정보를 유지한 문단 단위 분할을 우선 적용한다.
- 코드 블록은 한 청크 안에서 보존하고, 과도하게 긴 코드 블록만 별도 정책으로 분할한다.
- `userId`, `sectionTitle`, `sourceOffset` 또는 원본 위치 정보를 청크 메타데이터에 추가한다.
- 기존 `documentId`, `chunkIndex`, `fileType`, `page` 메타데이터를 유지한다.

예상 수정 대상:

- `backend/src/main/java/com/documentor/backend/infra/ai/SpringAiDocumentVectorIndexer.java`
- `backend/src/main/java/com/documentor/backend/infra/ai/DocumentReaderFactory.java` 또는 신규 splitter 전략 클래스
- 문서·청크 관련 단위 테스트

완료 기준:

- 형식별 청킹 결과가 테스트로 검증된다.
- Markdown 제목과 코드 블록이 의도치 않게 분리되지 않는다.
- 동일 평가셋에서 baseline보다 `Recall@5` 또는 출처 일치율이 개선된다.

### Phase 2. 검색 질의 및 결과 선별 개선

**목적:** 질문 생성 목적에 맞는 근거 청크를 정확히 선택한다.

작업:

- `SpringAiDocumentContextRetriever`의 검색 질의를 사용자의 질문 조건이 반영된 자연어 요청으로 재구성한다.
- `topK`와 similarity threshold를 설정값으로 분리해 실험 가능하게 한다.
- 동일 문서의 인접·중복 청크가 과도하게 포함되지 않도록 deduplication 규칙을 적용한다.
- 후보 검색 결과와 최종 선택 결과의 점수·메타데이터를 기록한다.
- 필요 시 후보 N개를 검색한 뒤 cross-encoder 또는 LLM 기반 reranker를 붙인다. 이 단계는 baseline 대비 효과가 확인될 때만 유지한다.

예상 수정 대상:

- `backend/src/main/java/com/documentor/backend/infra/ai/SpringAiDocumentContextRetriever.java`
- `backend/src/main/java/com/documentor/backend/infra/ai/QuestionGenerationAiConfig.java`
- 검색 설정 properties와 테스트 코드

완료 기준:

- 문서 ID와 사용자 ID 필터가 모든 검색 경로에 적용된다.
- top-K, threshold 조합별 평가 결과가 남아 있다.
- 선택한 검색 전략의 근거와 제외한 전략의 이유가 문서화되어 있다.

### Phase 3. pgvector 도입과 인덱스 수명주기 관리

**목적:** 재기동에도 유지되고 문서 데이터와 정합성이 맞는 벡터 인덱스를 만든다.

작업:

- Docker Compose에 PostgreSQL + pgvector 서비스를 추가한다.
- Spring AI의 pgvector 연동 설정과 의존성을 추가한다.
- `VectorStoreConfig`를 환경별 설정으로 분리한다. 로컬 개발·테스트·운영 환경에서 연결 정보를 설정할 수 있어야 한다.
- 문서 업로드 완료 시 벡터를 저장하고, 문서 삭제 시 해당 `documentId`의 벡터를 삭제한다.
- 관리자 또는 소유자용 문서 재색인 기능을 구현한다.
- 초기 도입 시 기존 문서 인덱스를 채우는 일회성 backfill 또는 재색인 명령을 준비한다.

예상 수정 대상:

- `backend/build.gradle`
- `backend/docker-compose.yml`
- `backend/src/main/java/com/documentor/backend/infra/ai/VectorStoreConfig.java`
- 문서 삭제 서비스와 재색인 API/서비스
- application profile 설정 파일

완료 기준:

- 애플리케이션 재시작 뒤 기존 문서로 질문 생성이 가능하다.
- 문서 삭제 뒤 해당 문서 청크가 검색되지 않는다.
- 재색인 후 기존 인덱스와 중복 벡터가 생기지 않는다.

### Phase 4. 질문 생성 Job 영속화 및 실패 복구

**목적:** 질문 생성 과정을 비동기 작업으로 안전하게 관리한다.

작업:

- `question_generation_jobs` 엔티티와 마이그레이션을 추가한다.
- Job 필드: ID, 사용자 ID, 문서 ID, 요청 옵션, 상태, 실패 코드, 실패 메시지, 재시도 횟수, 생성·시작·종료 시각, QuestionSet ID.
- 상태 전이를 `PENDING -> RETRIEVING -> GENERATING -> COMPLETED | FAILED`로 정의한다.
- `ConcurrentHashMap`과 `AtomicLong` 기반 상태 관리를 DB 조회로 교체한다.
- 실패 원인을 파싱, 임베딩, 검색 결과 부족, LLM 호출, 저장 오류로 분류한다.
- `FAILED` 상태 Job을 재시도할 수 있는 API를 추가한다.
- 프론트엔드에서 실패 상태와 재시도 동선을 제공한다.

예상 수정 대상:

- `backend/src/main/java/com/documentor/backend/service/question/QuestionGenerationService.java`
- Question generation controller, DTO, repository, domain entity
- `fe/frontend/src/pages/QuestionSettings/index.tsx`
- API client와 상태 표시 UI

완료 기준:

- 서버 재시작 뒤에도 Job 상태와 실패 사유를 조회할 수 있다.
- 실패한 Job을 재시도해 정상 완료까지 상태 전이가 확인된다.
- 중복 요청과 재시도 요청이 동일한 QuestionSet을 중복 생성하지 않도록 보호된다.

### Phase 5. 통합 검증과 결과 문서화

**목적:** 개선이 실제로 동작하고, 포트폴리오에서 설명 가능한 근거를 남긴다.

작업:

- 테스트용 문서를 업로드하고 인덱싱한 뒤 질문을 생성하는 통합 테스트를 작성한다.
- 서버 재기동, 문서 삭제, 재색인, 생성 실패·재시도 시나리오를 검증한다.
- Golden dataset으로 개선 전후 지표를 다시 계산한다.
- 대표 실패 사례를 `현상 -> 원인 -> 조치 -> 결과` 형식으로 3건 이상 작성한다.
- README에 아키텍처, 주요 기술 결정, 지표, 한계를 정리한다.

완료 기준:

- 핵심 RAG 흐름의 통합 테스트가 통과한다.
- 개선 전후 비교표와 측정 조건이 문서화되어 있다.
- 포트폴리오에 사용할 수 있는 수치와 실패 사례가 확보된다.

## 6. 테스트 전략

| 레벨 | 대상 | 핵심 검증 |
|---|---|---|
| 단위 테스트 | splitter, 질의 생성, 중복 제거, Job 상태 전이 | 경계 조건과 규칙이 정확히 적용되는지 |
| 통합 테스트 | 벡터 저장-검색, 문서 삭제, 재색인 | 데이터 정합성과 필터가 보장되는지 |
| 회귀 평가 | Golden dataset | 개선 후 기존 검색 품질이 하락하지 않는지 |
| 수동 검증 | 실제 PDF/MD/TXT 업로드 UI | 출처 표시, 실패 메시지, 재시도 흐름이 자연스러운지 |

## 7. 측정 지표 정의

| 지표 | 정의 | 목표 예시 |
|---|---|---|
| Recall@K | 기대 근거 청크가 상위 K개 검색 결과에 포함된 질의 비율 | baseline 대비 상승 |
| 출처 일치율 | 생성 질문의 출처가 기대 근거와 일치한 비율 | baseline 대비 상승 |
| 중복 질문 비율 | 동일 의미 질문이 2개 이상 생성된 비율 | baseline 대비 하락 |
| 생성 실패율 | 전체 생성 요청 중 실패한 비율 | baseline 대비 하락 |
| 재기동 후 검색 성공률 | 재기동 후 기존 문서 검색에 성공한 비율 | 100% |
| Job 복구율 | 실패 Job이 재시도 후 정상 완료된 비율 | 실패 유형별 측정 |

목표 수치는 baseline을 측정한 후 정한다. 임의의 목표 수치를 먼저 선언하지 않고, 평가셋 규모와 문서 난이도를 함께 기록한다.

## 8. 작업 순서와 마일스톤

| 마일스톤 | 선행 조건 | 결과물 |
|---|---|---|
| M1: Baseline 확보 | 없음 | 평가셋, 현재 성능 리포트 |
| M2: 청킹 개선 | M1 | 구조 보존 splitter, 전후 검색 결과 |
| M3: 검색 개선 | M1, M2 | 질의 전략, 파라미터 실험 결과 |
| M4: 벡터 영속화 | M1 | pgvector 환경, 삭제·재색인 처리 |
| M5: Job 영속화 | M4 권장 | Job 테이블, 재시도 API/UI |
| M6: 최종 검증 | M2~M5 | 통합 테스트, 최종 개선 리포트 |

M2와 M4는 병렬로 진행할 수 있다. 다만 성과를 설명하는 핵심은 M1이므로 평가셋 없이 청킹 또는 저장소를 먼저 바꾸지 않는다.

## 9. 리스크와 대응

| 리스크 | 영향 | 대응 |
|---|---|---|
| 평가셋이 작거나 편향됨 | 지표 신뢰도 저하 | 문서 형식·주제·난이도를 섞고 평가 질의 수를 공개 |
| LLM 출력의 비결정성 | 전후 비교가 흔들림 | 동일 모델·프롬프트·샘플 수를 유지하고 반복 측정 |
| pgvector 도입 범위가 커짐 | 일정 지연 | docker-compose와 개발 환경부터 최소 구성, 재색인 API는 후순위 |
| reranking 비용 증가 | 응답 시간·비용 증가 | baseline과 품질 향상폭을 비교해 효과가 있을 때만 적용 |
| 비동기 Job 중복 실행 | 중복 QuestionSet 생성 | 요청 식별자, 상태 검사, 트랜잭션 및 idempotency 정책 적용 |

## 10. 최종 산출물 체크리스트

- [ ] Golden dataset과 baseline 결과
- [ ] 문서 구조 기반 청킹 코드와 테스트
- [ ] 검색 질의·파라미터 실험 기록
- [ ] pgvector 기반 영속 벡터 저장소
- [ ] 문서 삭제·재색인 처리
- [ ] DB 기반 질문 생성 Job과 재시도 API
- [ ] 프론트엔드 실패·재시도 UI
- [ ] RAG 통합 테스트
- [ ] 개선 전후 지표 리포트
- [ ] 실패 사례 3건과 README 아키텍처 문서

## 11. 완료 선언 기준

다음 문장을 실제 수치와 함께 말할 수 있으면 개발 계획은 완료다.

> PDF·Markdown·TXT 문서를 구조적으로 청킹하고, Golden dataset으로 검색 품질을 측정해 개선 전후를 검증했습니다. 또한 pgvector와 DB 기반 Job 관리로 벡터 인덱스와 생성 작업 상태를 영속화해, 서버 재기동·문서 삭제·실패 재시도 상황에서도 안정적으로 동작하는 RAG 기반 면접 질문 생성 서비스를 구현했습니다.
