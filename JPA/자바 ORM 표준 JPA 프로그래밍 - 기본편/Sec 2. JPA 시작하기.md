## Sec 2. JPA 시작하기

### Hello JPA - 애플리케이션 개발

엔티티 클래스에 @Table() 어노테이션 - 디비 이름 매핑.

```java
package hellojpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Table(name="USER") // USER 테이블에 쿼리 날아감
public class Member {

    @Id
    private Long id;
    private String name;
}

```



멤버변수에 @Column() - 칼럼 이름 매핑

```java
@Column(name = "username")	// username 칼럼으로 쿼리 날아감
```



- CRUD

  entityManager 는 자바의 컬렉션과 같은 느낌..

```java
						// INSERT
            Member member = new Member();
            member.setId(2L);
            member.setName("HelloB");

            em.persist(member);

            Member findMember = em.find(Member.class, 1L);
            // SELECT
            System.out.println("findMember.id = " + findMember.getId());
            System.out.println("findMember.name = " + findMember.getName());

            // UPDATE
            findMember.setName("HelloJPA");

						// DELETE
						em.remove(findMember);
```

업데이트를 보면 setter 만 호출해도 쿼리가 날아간다. JPA를 통해서 엔티티를 가져오면 이 엔티티는 JPA가 관리하게 된다. 그리고 트랜잭션이 커밋되는 순간 엔티티들의 변경 사항을 체크하고, 수정된 것이 있다면 업데이트 쿼리를 날리고 커밋하게 된다.



#### 주의!!

- EntityManagerFactory는 하나만 생성해서 애플리케이션 전체에서 공유해야 한다.
- EntityManager는 쓰레드간에 공유되면 안된다. 사용하고 버려야 한다.
- **JPA의 모든 데이터 변경은 트랜잭션 안에서 실행되어야 한다.**



#### JPQL 이란? 

- 가장 단순한 조회 방법
  - EntityManager.find()
  - 객체 그래프 탐색(a.getB().getC())
- 그럼, 나이가 18살 이상인 회원을 모두 검색하고 싶다면?!



em.createQuery() 란 메소드도 있긴 있다..!

```java
List<Member> result = em.createQuery("select m from Member as m", Member.class).getResultList();
```

JPA 입장에서는 테이블 대상으로 절대 코드를 짜지 않는다. 무조건 엔티티 기준!

Pagenation - `.setFirstResult().setLastResult()`



**JPQL은 객체를 대상으로 하는 객체 지향 쿼리 이다!**

- JPA 를 사용하면 엔티티 객체를 중심으로 개발한다.
- 문제는 검색 쿼리..
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
- 따라서 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능하다..
- -> 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL 문이 필요하다!



- JPA 는 SQL 을 추상화한 JPQL 이라는 객체 지향 쿼리 언어를 제공한다.
- SQL과 문법이 유사하고, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- **JPQL**은 엔티티 객체를 대상으로 쿼리
- **SQL** 은 데이터베이스 테이블을 대상으로 쿼리