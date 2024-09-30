## 기본 값 타입

**JPA의 데이터 타입 분류**

- 엔티티 타입
  - `@Entity`로 정의하는 객체
  - 데이터가 변해도 식별자로 지속해서 **추적 가능**
- 값 타입
  - Int, Integer, String 처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
  - 식별자가 없고 값만 있으므로 변경시 추적 불가



**값 타입 분류**

- **기본 값 타입**
  - 자반 기본 타입(int, double..)
  - 래퍼 클래스(Integer, Long..)
  - String
- **임베디드 타입**(복합 값 타입)
- **컬렉션 값 타입**(collection value type)



**기본 값 타입**

- 생명주기가 엔티티에 의존
- 값 타입은 공유하면 안된다!!
- 참고) 자바의 기본 타입은 절대 공유되지 않는다.



## 임베디드 타입

- 새로운 값 타입을 직접 정의할 수 있음
- 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 함.
- int, String 과 같은 값 타입이다. Entity가 아님!



**사용법**

- @Embeddable: 값 타입을 정의하는 곳에 표시
- @Embedded: 값 타입을 사용하는 곳에 표시
- 기본 생성자 필수!



**장점**

- 재사용 가능
- 높은 응집도



```java
@Entity
public class Member {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "USERNAME")
  private String username;

  // 기간
  private LocalDateTime startDate;
  private LocalDateTime endDate;

  // 주소
  private String city;
  private String street;
  private String zipcode;
}
```



```java
@Entity
public class Member {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "USERNAME")
  private String username;

  // 기간
  @Embedded
  private Period workPeriod;

  // 주소
  @Embedded
  private Address workAddress;
}

@Embeddable
public class Period {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

@Embeddable
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
```



**임베디드 타입과 테이블 매핑**

- 임베디드 타입은 엔티티의 값일 뿐이다.
- 임베디드 타입을 사용하기 전과 후에 매핑하는 테이블은 같다.
- 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능 
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음



**@AttributeOverride: 속성 재정의**

- 한 엔티티에서 같은 값 타입을 사용하면?
  - 컬럼 명이 중복됨.
- @AttributeOverrides, @AttributeOverride 를 사용해서 컬럼명 속성을 재정의



**임베디드 타입과 null**

- 임베디드 타입의 값이 Null 이면 매핑한 컬럼 값은 모두 null로 세팅됨.



## 값 타입과 불변 객체

값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다. 따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.



**값 타입 공유 참조**

- 임베디드 타입 같은 값 타입을 **여러 엔티티에서 공유하면 위험**하다.
- 부작용이 발생할 수 있음!
  - 하나 고치면 다른 엔티티의 값 타입도 수정될 수 있다.
  - 의도한거라면, 값 타입으로 표현하지 말고 엔티티로 표현해야 함.



**값 타입 복사**

- 값 타입의 실제 인스턴스 값을 공유하는 것은 위험!!
- 대신 값(인스턴스)를 복사해서 사용하자.



**객체 타입의 한계**

- 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
- 문제는 **임베디드 타입처럼 직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입**..
- 자바 기본 타입에 값을 대입하면 값을 복사한다.
- **객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.**
  - **== 객체의 공유 참조는 피할 수 없다.**



**불변 객체**

- 객체 타입을 수정할 수 없도록 만들면 위의 부작용을 원천 차단 가능
- 값 타입은 불변 객체로 설계해야 함!
  - 불변 객체(Immutable Object): 생성 시점 이후 절대 값을 변경할 수 없는 객체. 생성자로만 값을 설정하고, setter 안만들면 됨.
- 참고: Integer, String 은 자바가 제공하는 대표적인 불변 객체



## 값 타입의 비교

값 타입은 인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐야 함.

자바의 `==` 비교는 참조값 비교.



- 동일성(identity) 비교: 인스턴스의 참조 값을 비교, `==` 사용.
- 동등성(equivalence) 비교: 인스턴스의 값을 비교, `equals()` 사용
- 값 타입은 `a.equals(b)`를 사용해서 동등성 비교를 해야 함.
- 값 타입의 `equals()` 메서드를 적절하게 재정의해야 함. (주로 모든 필드 비교)



## 값 타입 컬렉션

- 값 타입 하나 이상 저장할 때 사용
- @ElementCollection, @CollectionTable 사용
- DB는 컬렉션을 같은 테이블에 저장할 수 없다.
- 컬렉션을 저장하기 위한 별도의 테이블이 필요함

``` java
@ElementCollection
@CollectionTable(name = "FAVORITE_FOOD", joinColumn = @JoinColumn(name = "MEMBER_ID"))
private Set<String> favoriteFoods = new HashSet<>();
```



**값 타입 컬렉션 사용**

- 값 타입 저장
  - 값 타입 컬렉션 스스로 생명 주기가 없기 때문에 별도의 테이블이 있음에도 필드로 들어있는 엔티티의 생명 주기를 따라간다.
- 값 타입 조회
  - 값 타입 컬렉션도 지연 로딩 전략 사용
- 값 타입 수정
  - 새로운 인스턴스로 통째로 갈아끼워야 함.

- 참고) 값 타입 컬렉션은 영속성 전에 + 고아 객체 제거 기능을 필수로 가진다.



**값 타입 컬렉션의 제약사항**

- 값 타입은 엔티티와 다르게 식별자 개념이 없다.
- 값은 변경하면 추적이 어렵다.
- **값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.**
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야 함: null 입력X, 중복 저장X
- **쓰지마라!!**



**값 타입 컬렉션 대안**

- 실무에서는 상황에 따라 값 타입 컬렉션 대신에 **일대다 관계**를 고려

  ``` java
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "MEMBER_ID")
  private Set<foodEntity> favoriteFoods = new HashSet<>();
  ```

  

- 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용

- 영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬 렉션 처럼 사용 

- EX) AddressEntity



**값 타입은 언제 사용하는가??**

- 정말~ 값 타입이라 판단될 때만 사용하자!
- 엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안됨
- 식별자가 필요하고, 지속해서 값을 추적, 변경해야 한다면 그것은 값 타입이 아닌 엔티티!



## 실전 예제 6 - 값 타입 매핑

```java
@Embeddable
public class Address {

    private String city;
    private String street;
    private String zipcode;

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }

    private void setCity(String city) {
        this.city = city;
    }

    private void setStreet(String street) {
        this.street = street;
    }

    private void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, zipcode);
    }
}
```