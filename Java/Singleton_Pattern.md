# Singleton Pattern

### 싱글톤 패턴이란

애플리케이션이 시작될 때 어떤 클래스가 **최초 한 번**만 메모리를 할당하고`(static)` 그 메모리에 하나의 인스턴스를 만들어 사용하는 디자인 패턴이다.

주로 공통된 객체를 여러 개 생성해서 사용하는 DBCP(DataBase Connection Pool)와 같은 상황에서 많이 사용된다.



```java
public class BookMgr {
    private static BookMgr instance;

    private BookMgr(){}

    public static BookMgr getInstance(){
        if(instance == null) instance = new BookMgr();
        return instance;
    }
}
```

생성자를 private으로 선언하고 클래스 내에서 하나의 인스턴스만을 생성한다. getInstance() 메소드를 통해 모든 클래스에서 동일한 인스턴스를 반환하도록 한다.



#### 싱글톤 패턴을 쓰는 이유

고정된 메모리 영역을 얻으면서 한 번의 new로 인스턴스를 사용하기 때문에 메모리 낭비를 방지할 수 있다. 또한, 싱글톤으로 만들어진 클래스의 인스턴스는 전역 인스턴스이기 때문에 다른 클래스의 인스턴스들이 데이터를 공유하기 쉽다.



#### 싱글톤 패턴의 문제점

싱글톤 인스턴스가 너무 많은 일을 하거나 많은 데이터를 공유시킬 경우 다른 클래스의 인스턴스들간에 결합도가 높아져 "개방-폐쇄 원칙"을 위배하게 된다. (객체 지향 설계 원칙에 어긋남)

따라서 수정이 어려워지고 테스트하기 어려워진다.

또한 멀티쓰레드 환경에서 동기화 처리를 하지 않으면 인스턴스가 두 개가 생성된다든지하는 경우가 발생할 수 있다.