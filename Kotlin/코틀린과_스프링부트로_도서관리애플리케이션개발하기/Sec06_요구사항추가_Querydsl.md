## Querydsl 도입

**JPQL의 단점**

- 쿼리가 문자열이기 때문에 버그를 찾기 어렵다.
- 문법이 SQL과 조금씩 다르다.
- 조건이 복잡한 동적 쿼리를 작성할 때 함수가 계속 늘어난다.
- 프로덕션 코드 변경에 취약하다.



Spring Data JPA와 Querydsl 을 함께 사용하며 서로를 보완해야 한다!

- Querydsl: 코드로 쿼리를 작성하게 해주는 도구



Qclass 로 코드로 쿼리 작성



**Querydsl 사용하기 - 첫 번째 방법**

Querydsl 적용된 구조

1. UserRepository가 JpaRepository, UserRepositoryCustom 구현

   ``` kotlin
   interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom {
   
       fun findByName(name: String): User?
   
       @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userLoanHistories")
       fun findAllWithHistories(): List<User>
   }
   ```

2. UserRepositoryCustomImpl 구현

3. Querydsl 설정

   ``` kotlin
   @Configuration
   class QuerydslConfig(
       private val em: EntityManager
   ) {
   
       @Bean
       fun querydsl(): JPAQueryFactory {
           return JPAQueryFactory(em)
       }
   }
   ```



```kotlin
class UserRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : UserRepositoryCustom {
    
    override fun findAllWithHistories(): List<User> {
        return queryFactory.select(user).distinct()
            .from(user)
            .leftJoin(userLoanHistory).on(userLoanHistory.user.id.eq(user.id)).fetchJoin()
            .fetch()
    }
}
```

- `select(user)`: select user 
- `distinct()`: select 결과에 DISTINCT를 추가한다. 
- `from(user)`: from user 
- `leftJoin(userLoanHistory)`: left join user_loan_history 
- `on(userLoanHistory.user.id.eq(user.id))`: on user_loan_history.user_id = user.id 
- `fetchJoin`: 앞의 join을 fetch join으로 처리한다. 
- `fetch()`: 쿼리를 실행하여 결과를 List 로 가져온다.



**이 방식의 장단점**

- 장점: 서비스단에서 `UserRepository` 하나만 사용하면 된다.
- 단점: 인터페이스와 클래스를 항상 같이 만들어주어야 하는 것이 번거롭고 부담스럽다.



**Querydsl 사용하기 - 두 번째 방법**

```kotlin
@Component
class BookQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    fun getStats(): List<BookStatResponse> {
        return queryFactory.select(Projections.constructor(
            BookStatResponse::class.java,
            book.type,
            book.id.count()
        ))
            .from(book)
            .groupBy(book.type)
            .fetch()
    }
}
```

- `Projections.constructor`: 주어진 DTO의 생성자를 호출하겠다~

- 장점: 클래스만 바로 만들면 되기 때문에 간결하다.
- 단점: 서비스단에서 필요에 따라 두 Repository를 모두 사용해주어야 한다.



## UserLoanHistoryRepository를 Querydsl로 리팩토링

Querydsl 을 사용하면 동적 쿼리를 간단히 만들 수 있다!!

- where 조건이 동적으로 생성

조회 쿼리의 필드수가(조건이) 늘어날수록 함수가 계속 생성되어야 한다..



```kotlin
@Component
class UserLoanHistoryQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun find(bookName: String, status: UserLoanStatus? = null): UserLoanHistory? {
        return queryFactory.select(userLoanHistory)
            .from(userLoanHistory)
            .where(
                userLoanHistory.bookName.eq(bookName),
                status?.let { userLoanHistory.status.eq(status) }
            )
            .limit(1)
            .fetchOne()
    }
}
```

다음 두 함수를 모두 커버 가능

``` kotlin
interface UserLoanHistoryRepository : JpaRepository<UserLoanHistory, Long> {

    fun findByBookName(bookName: String): UserLoanHistory?

    fun findByBookNameAndStatus(bookName: String, status: UserLoanStatus): UserLoanHistory?

}
```