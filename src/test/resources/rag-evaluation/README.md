# RAG Evaluation Resources

이 디렉터리는 DocuMentor RAG 파이프라인의 baseline과 개선 결과를 비교하기 위한 테스트 데이터다.

## 구성

- `documents/spring-di-bean-lifecycle.md`: heading, 표, 코드 블록을 포함한 Markdown 문서
- `documents/jpa-persistence-context.md`: 유사 개념 비교와 트랜잭션 사례를 포함한 Markdown 문서
- `documents/java-concurrency-completablefuture.txt`: Markdown 문법에 의존하지 않는 일반 텍스트 문서
- `golden-dataset.json`: 검색 질의 25개와 기대 근거 섹션·문장

## 사용 방법

1. `documents/`의 문서를 각각 DocuMentor에 업로드한다.
2. 문서가 `READY` 상태가 된 뒤 `golden-dataset.json`의 질의로 질문 생성을 요청한다.
3. 검색된 top-K 청크에 `expectedEvidence.anchorText` 또는 해당 `sectionTitle`의 내용이 있는지 확인한다.
4. 결과를 baseline 또는 개선 결과 파일로 저장한다.

## 측정 규칙

- `Recall@K`: 기대 근거를 포함한 청크가 상위 K개 검색 결과에 있으면 해당 질의를 성공으로 본다.
- 출처 일치율: 생성된 질문의 출처가 `expectedEvidence`에 지정한 섹션 또는 근거 문장과 일치한 비율이다.
- 중복 질문 비율: 하나의 생성 요청 결과에서 의미가 같은 질문이 2개 이상인 비율이다.
- 생성 실패율: 검색 근거 부족, LLM 오류, 저장 오류 등으로 질문 생성에 실패한 요청의 비율이다.

## 주의 사항

문서 ID는 업로드 결과의 실제 `documentId`와 다를 수 있다. 평가 실행 결과에는 업로드된 문서 ID와 `golden-dataset.json`의 논리 문서 ID를 함께 기록한다. 청킹 정책이 바뀌면 청크 ID는 달라질 수 있으므로, 평가는 청크 ID가 아닌 섹션 제목과 근거 문장으로 판정한다.
