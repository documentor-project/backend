# Spring DI와 Bean Lifecycle

## DI가 필요한 이유

의존성 주입(Dependency Injection, DI)은 객체가 필요한 협력 객체를 직접 생성하지 않고 외부로부터 전달받는 방식이다. `OrderService`가 `PaymentClient` 구현체를 `new`로 생성하면 구현 교체와 테스트 대역 주입이 어렵다. 반대로 생성자를 통해 의존성을 주입하면 서비스는 인터페이스에만 의존하고, 실행 환경에 따라 구현체를 교체할 수 있다.

```java
public class OrderService {
    private final PaymentClient paymentClient;

    public OrderService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }
}
```

생성자 주입은 필수 의존성을 객체 생성 시점에 확정하고, 필드를 `final`로 만들 수 있다는 장점이 있다. 순환 의존성이 있으면 애플리케이션 시작 시점에 발견하기 쉽다. 선택적 의존성이나 프레임워크 제약이 있는 경우에는 세터 주입을 사용할 수 있지만, 일반적인 비즈니스 서비스의 필수 의존성에는 생성자 주입을 우선한다.

## IoC Container와 Bean

Spring IoC Container는 애플리케이션에서 사용할 객체를 생성하고, 의존 관계를 연결하며, 생명주기를 관리한다. Container가 관리하는 객체를 Bean이라고 한다. `@Component`, `@Service`, `@Repository`, `@Controller`가 붙은 클래스는 컴포넌트 스캔의 대상이 될 수 있다. `@Configuration` 클래스의 `@Bean` 메서드가 반환하는 객체도 Bean으로 등록할 수 있다.

Bean 등록 방식은 사용 목적에 따라 선택한다.

| 등록 방식 | 적합한 경우 |
|---|---|
| `@Component` 계열 | 애플리케이션이 직접 작성한 클래스 |
| `@Bean` | 외부 라이브러리 객체 또는 생성 과정이 복잡한 객체 |
| `@Import` | 설정 모듈을 명시적으로 조합할 때 |

같은 타입의 Bean이 둘 이상이면 Spring은 어떤 Bean을 주입해야 하는지 판단할 수 없다. 이때 기본 후보를 `@Primary`로 지정하거나, `@Qualifier`로 주입 대상의 이름을 명시한다. `@Primary`는 기본 선택 규칙이고, `@Qualifier`는 특정 구현체를 정확히 선택해야 할 때 사용한다.

## Bean Scope

Bean scope는 Container가 Bean 인스턴스를 생성하고 유지하는 범위를 뜻한다. Spring의 기본 scope는 singleton이다. singleton scope는 애플리케이션 Container당 하나의 Bean 인스턴스를 공유한다. 따라서 singleton Bean에 요청별 상태나 사용자별 상태를 필드로 보관하면 동시성 문제가 발생할 수 있다.

| Scope | 인스턴스 생성 기준 | 주의점 |
|---|---|---|
| singleton | ApplicationContext당 하나 | 상태를 공유하지 않도록 설계 |
| prototype | Bean 요청마다 하나 | Container가 소멸 콜백을 자동 호출하지 않음 |
| request | HTTP 요청마다 하나 | 웹 애플리케이션 환경 필요 |
| session | HTTP 세션마다 하나 | 세션 메모리 사용량 관리 필요 |

prototype Bean은 생성과 의존성 주입까지는 Container가 담당하지만, 클라이언트에게 반환한 후의 소멸 관리는 클라이언트 책임이다. singleton Bean이 prototype Bean을 생성자 주입으로 받으면 prototype이 singleton 생성 시점에 한 번만 주입된다. 매 요청마다 새로운 prototype Bean이 필요하면 `ObjectProvider` 또는 scoped proxy를 사용한다.

## Bean Lifecycle

일반적인 singleton Bean의 생명주기는 다음과 같다.

```text
Bean 인스턴스 생성
  -> 의존성 주입
  -> 초기화 콜백(@PostConstruct)
  -> 애플리케이션에서 사용
  -> Container 종료
  -> 소멸 콜백(@PreDestroy)
```

`@PostConstruct`는 의존성 주입이 끝난 뒤 실행된다. 외부 설정을 검증하거나, 의존 객체를 사용해야 하는 초기화 작업에 적합하다. `@PreDestroy`는 singleton Bean이 소멸되기 전에 실행되며, 열린 연결이나 스레드와 같은 자원을 정리할 때 사용한다.

```java
@Component
public class SearchIndexWarmup {
    @PostConstruct
    void validateConfiguration() {
        // 의존성 주입 이후 필요한 설정을 검증한다.
    }

    @PreDestroy
    void closeClient() {
        // Container 종료 전에 외부 클라이언트를 정리한다.
    }
}
```

`@PostConstruct`에서 오래 걸리는 네트워크 호출을 수행하면 애플리케이션 시작 시간이 길어지고, 실패 시 기동 자체가 실패할 수 있다. 반드시 기동 전에 검증해야 하는 작업과, 기동 뒤 비동기로 처리할 수 있는 작업을 구분해야 한다.

## 순환 의존성

`AService`가 `BService`를 생성자 주입받고 `BService`가 다시 `AService`를 생성자 주입받으면 순환 의존성이 발생한다. 생성자 주입에서는 애플리케이션 시작 시점에 이 문제가 드러난다. 이를 `@Lazy`로 숨기기보다 두 서비스가 동시에 맡고 있는 책임을 분리하는 것이 우선이다.

예를 들어 주문 생성과 결제 승인 로직이 서로를 호출한다면, 두 작업을 조정하는 별도 Coordinator를 두거나 이벤트 기반으로 흐름을 분리할 수 있다. 순환 의존성은 기술적 주입 방식의 문제가 아니라 도메인 책임이 지나치게 결합되었다는 신호일 수 있다.

## 테스트 관점

생성자 주입을 사용하면 Spring Context를 띄우지 않아도 단위 테스트에서 가짜 구현체를 주입할 수 있다. 반면 `@Autowired` 필드 주입은 테스트 코드가 리플렉션에 의존하거나 Context를 필요로 하게 만들어 테스트의 의도를 흐릴 수 있다.

```java
@Test
void createsOrderWithFakePaymentClient() {
    PaymentClient fakeClient = request -> PaymentResult.approved();
    OrderService service = new OrderService(fakeClient);

    OrderResult result = service.createOrder(new OrderRequest());

    assertThat(result.isApproved()).isTrue();
}
```
