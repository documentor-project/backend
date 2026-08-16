# JPA Persistence Context와 Transaction

## Persistence Context의 역할

Persistence Context는 엔티티를 영속 상태로 관리하는 공간이다. EntityManager는 Persistence Context에 접근하는 창구 역할을 한다. 트랜잭션 안에서 조회한 엔티티는 Persistence Context에 저장되며, 같은 식별자로 다시 조회하면 일반적으로 1차 캐시의 엔티티를 반환한다.

```java
@Transactional
public void changeNickname(Long memberId, String nickname) {
    Member member = memberRepository.findById(memberId).orElseThrow();
    member.changeNickname(nickname);
}
```

위 코드에서는 `save()`를 다시 호출하지 않아도 트랜잭션이 끝날 때 변경 감지(dirty checking)가 일어나 수정 SQL이 실행될 수 있다. 단, 조회한 객체가 영속 상태여야 하며 트랜잭션 안에서 변경되어야 한다.

## Entity State

JPA 엔티티는 대표적으로 비영속, 영속, 준영속, 삭제 상태를 가진다.

| 상태 | 설명 | 예시 |
|---|---|---|
| 비영속(transient) | Persistence Context와 관계없는 새 객체 | `new Member()` |
| 영속(managed) | Persistence Context가 변경을 추적하는 객체 | `entityManager.persist(member)` 또는 조회 결과 |
| 준영속(detached) | 이전에는 영속이었으나 더 이상 추적되지 않는 객체 | `entityManager.detach(member)` 또는 Context 종료 후 객체 |
| 삭제(removed) | 삭제가 예약된 객체 | `entityManager.remove(member)` |

준영속 엔티티의 필드를 바꿔도 Persistence Context는 변경을 추적하지 않는다. API 요청으로 받은 객체를 곧바로 `merge()`하면 전달되지 않은 필드가 `null`로 덮일 위험이 있다. 업데이트는 식별자로 영속 엔티티를 조회한 뒤, 변경을 허용한 필드만 수정하는 방식이 더 안전하다.

## Dirty Checking과 Flush

dirty checking은 트랜잭션 커밋 시점에 Persistence Context가 관리 중인 엔티티의 초기 상태와 현재 상태를 비교해 변경된 값을 찾는 기능이다. 변경이 감지되면 JPA는 필요한 `UPDATE` SQL을 쓰기 지연 저장소에 등록하고, flush 시점에 데이터베이스로 전송한다.

flush는 Persistence Context의 변경 내용을 데이터베이스에 동기화하는 작업이다. flush가 곧 트랜잭션 commit을 의미하지는 않는다. commit은 트랜잭션을 확정하는 작업이고, flush는 SQL을 데이터베이스에 보내는 동기화 과정이다. flush 이후에도 트랜잭션이 rollback되면 이미 전송된 SQL의 결과는 rollback될 수 있다.

JPQL이나 native query를 실행하기 전에 현재 변경 내용을 반영해야 정확한 결과를 얻을 수 있어 AUTO flush mode에서는 flush가 발생할 수 있다. 그러나 모든 SQL이 즉시 실행된다고 가정하면 성능과 트랜잭션 동작을 잘못 이해하게 된다.

## Transaction Boundary

트랜잭션 경계는 일관성이 함께 보장되어야 하는 작업의 범위를 뜻한다. 주문 생성, 재고 차감, 결제 승인 결과 저장이 하나의 비즈니스 작업이라면 일부만 성공한 상태를 남기지 않도록 같은 트랜잭션 또는 보상 전략을 고려해야 한다.

Spring의 `@Transactional`은 프록시 기반으로 동작한다. 외부 Bean이 프록시를 통해 메서드를 호출할 때 트랜잭션이 적용된다. 같은 클래스 안에서 `this.someTransactionalMethod()`처럼 호출하는 self-invocation은 프록시를 거치지 않으므로 의도한 트랜잭션 속성이 적용되지 않을 수 있다.

```java
@Service
public class MemberService {
    @Transactional
    public void register(MemberCommand command) {
        saveMember(command);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMember(MemberCommand command) {
        // register() 내부 호출이라면 REQUIRES_NEW가 적용되지 않을 수 있다.
    }
}
```

트랜잭션 전파는 기존 트랜잭션이 있을 때 새 메서드가 어떤 방식으로 참여할지 정한다. 기본값인 `REQUIRED`는 기존 트랜잭션이 있으면 참여하고 없으면 새로 만든다. `REQUIRES_NEW`는 기존 트랜잭션을 잠시 중단하고 새 트랜잭션을 만든다. `REQUIRES_NEW`를 남용하면 부분 커밋으로 인해 업무 일관성이 깨질 수 있으므로, 감사 로그처럼 독립적으로 남겨야 하는 작업에 제한적으로 사용한다.

## 읽기 전용 Transaction

`@Transactional(readOnly = true)`는 읽기 작업의 의도를 표현하고, JPA 구현체나 데이터베이스 설정에 따라 최적화 힌트를 제공할 수 있다. 그러나 이를 보안 장치처럼 생각해서는 안 된다. 데이터베이스와 드라이버 설정에 따라 쓰기 SQL이 완전히 차단되는 것은 아니며, 중요한 쓰기 방지는 권한과 애플리케이션 로직으로 보장해야 한다.

조회 중심 서비스에는 `readOnly = true`를 기본으로 두고, 변경 메서드에 일반 `@Transactional`을 명시하면 읽기와 쓰기 의도가 코드에 드러난다.

## N+1 Query와 Fetch Join

연관 관계가 LAZY 로딩일 때 목록을 조회한 뒤 각 엔티티의 연관 객체에 접근하면 추가 SQL이 반복될 수 있다. 예를 들어 주문 100건을 조회한 뒤 각 주문의 회원 정보를 참조하면 주문 조회 1회와 회원 조회 N회가 발생하는 N+1 문제가 생길 수 있다.

필요한 연관 데이터를 한 번에 읽어야 하는 조회에는 fetch join, EntityGraph, DTO projection을 상황에 맞게 선택한다. fetch join은 연관 엔티티를 함께 조회해 SQL 횟수를 줄일 수 있지만, 컬렉션 fetch join과 페이징을 함께 사용할 때 결과가 왜곡되거나 메모리에서 페이징될 수 있어 주의해야 한다.

## 낙관적 잠금과 비관적 잠금

여러 트랜잭션이 같은 데이터를 변경할 수 있는 경우 동시성 제어가 필요하다. 낙관적 잠금은 엔티티의 `@Version` 값을 이용해 업데이트 시점에 충돌을 감지한다. 충돌이 드문 경우에 적합하며, 충돌이 발생하면 재시도 정책이 필요하다.

비관적 잠금은 조회 시점에 데이터베이스 잠금을 얻어 다른 트랜잭션의 변경을 제한한다. 충돌이 잦고 짧은 시간 안에 순서를 보장해야 하는 경우에 검토할 수 있지만, 잠금 대기와 데드락 위험이 있으므로 잠금 범위와 시간을 작게 유지해야 한다.

```java
@Version
private Long version;
```

재고 차감처럼 동일 자원에 경쟁이 발생하는 기능은 단순히 `synchronized`만 적용하기보다, 애플리케이션 인스턴스가 여러 개인 환경까지 고려해 데이터베이스 잠금 또는 원자적 업데이트 전략을 선택해야 한다.
