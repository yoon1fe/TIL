![img](https://lh5.googleusercontent.com/tqsGcOYk-owmbNi0AMT-vKbL6RxcFHypIeUT4AZ09iAQc5mfUdgUa5uwTzPQ8G5BkQH5eePy98gAadajWYIYp4v1Ust9bNw3QEvCOyyMkzvKtafoZaQ7prBMeZrP8i0gxwZlN5Cj)



Spring Data 는 여러 스프링 프로젝트를 묶어놓은 것을 지칭한다.

| Spring Data            | SQL & NoSQL 저장소 지원 프로젝트의 묶음.                     |
| ---------------------- | ------------------------------------------------------------ |
| **Spring Data Common** | 여러 저장소 지원 프로젝트의 공통 기능 제공.                  |
| **Spring Data REST**   | 저장소의 데이터를 하이퍼미디어 기반 HTTP 리소스로(REST API로) 제공하는 프로젝트. |
| **Spring Data JPA**    | 스프링 데이터 Common이 제공하는 기능에 **JPA 관련 기능 추가**. |

http://projects.spring.io/spring-data/





### Spring Data Common 1. 레포지토리

![img](https://lh6.googleusercontent.com/zeNaOb-uhvZjerbocijZgndm7B5kbaKEoXxBnWufjjiD463Wi-wlb5eiOXIa3HRdlEXbXLTfct5NqB9518DDZLyIuzxtPdpFXVwjYZS6W8pAdrrrOfLx3AmFJhis3Ifwq139u2R5)

Repository interface - 마커 인터페이스.

중간 단계 Repository 에는 @NoRepositoryBean 어노테이션이 붙어 있다. 실질적인 Repository가 아님을 명시.



테스트 코드를 작성할 때 Spring boot 에서 제공해주는 @DataJpaTest 어노테이션을 붙이면 리포지토리 빈들만 등록이 된다.



커스텀한 메서드를 추가할 수도 있다.

ex)

``` java
public interface PostRepository extends JpaRepository<Post, Long> {

  Page<Post> findByTitleContains(String title, Pageable pageable);
  
}

```



### Spring Data Common 2. 인터페이스 정의하기

Repository 인터페이스로 공개할 메소드를 직접 일일히 정의하고 싶다면

특정 레포지토리 당 -> @RepositoryDefinition(domainClass, idClass)

공통 인터페이스 정의 -> @NoRepositoryBean



### Spring Data Common 3. Null 처리

Spring Data 2.0 부터 자바 8의 Optional 지원한다.

`Optional<Post> findById(Long id);`

컬렉션은 Null 을 리턴하지 않고, 빈 컬렉션을 리턴한다.



Null 관련 어노테이션도 지원한다.

@NonNullApi, @NonNull, @Nullable

런타임 체크를 지원한다.





### Spring Data Common 4. 쿼리 만들기

### Spring Data Common 5. 쿼리 만들기 실습

### Spring Data Common 6. 비동기 퀘리 메서드

### Spring Data Common 7. 커스텀 레포지토리

### Spring Data Common 8. 기본 레포지토리 커스터마이징

### Spring Data Common 9. 도메인 이벤트

### Spring Data Common 10. QueryDSL 연동

### QueryDSL 연동 보강

### Spring Data Common 11. 웹 기능

### Spring Data Common 12. 웹 기능 - DomainClassConverter

### Spring Data Common 13. 웹 기능 - Pageable과 Sort

### Spring Data Common 14. 웹 기능 - HATEOAS

### Spring Data Common 15. 정리

