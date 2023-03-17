## 스프링 데이터 JPA 동작 방식

스프링 데이터 JPA에서 인터페이스 타입의 인스턴스는 누가 만들어 주나??

- Spring AOP를 기반으로 동작하며, `RepositoryFactorySupport`에서 프록시를 생성



## 프록시 패턴

![img](https://blog.kakaocdn.net/dn/c6BFcq/btryhOGOOrr/O64DkGvuJxg7kkJCgpAvLk/img.png)

**프록시 패턴**

- 프록시와 리얼 서브젝트가 공유하는 인터페이스가 있고, 클라이언트는 해당 인터페이스 타입으로 프록시를 사용한다.
- 클라이언트는 프록시를 거쳐서 리얼 서브젝트를 사용하기 때문에 프록시는 리얼 서브젝트에 대한 접근을 관리하거나 부가 기능을 제공하거나, 리턴값을 변경할 수 있다.
- 리얼 서브젝트는 자신이 해야 할 일만 하면서 (**SRP**), 프록시를 사용해서 부가적인 기능(접근 제한, 로깅, 트랜잭션 등)을 제공할 때 프록시 패턴을 주로 사용함



프록시 패턴의 문제점

- 번거롭다!! 프록시를 프록시로 감싸는 경우가 생길 수도.. 위임하는 코드도....



요런 코드를 런타임에 동적으로 생성해주는 기능이 자바 리플렉션 API에 있다!! > 이것이 다이나믹 프록시다



## 다이나믹 프록시

**다이나믹 프록시**란? 런타임에 특정 인터페이스들을 구현하는 클래스 또는 인스턴스를 만드는 기술. 위의 방식대로 매번 프록시 클래스를 생성할 필요가 없어진다.



프록시 인스턴스 만들기: `Object Proxy.newProxyInstance(ClassLoader, Interfaces, InvocationHandler)`



근데 번거롭다. `InvocationHandler` 는 유연하지 않다. 리얼 서브젝트의 모든 메서드에 대해 적용된다. 스프링 AOP를 쓰자~

또한 인터페이스에 대해서만 프록시 생성 가능하다. 클래스의 프록시는 어떻게 만들지??



## 클래스의 프록시가 필요하다면?

서브 클래스를 만들 수 있는 라이브러리를 사용하여 프록시를 만들 수 있다.



**CGlib**

- 스프링, 하이버네이트가 사용

- 버전 호환성이 좋지 않아서 서로 다른 라이브러리 내부에 내장된 형태로 제공되기도 한다.

  ``` java
  MethodInterceptor handler = new MethodInterceptor() {
  		BookService bookService = new BookService();
  		@Override
  		public Object intercept(Object o, Method method, Object[] objects, MethodProxy
  		methodProxy) throws Throwable {
  			return method.invoke(bookService, objects);
  		}
  	};
  	BookService bookService = (BookService) Enhancer.create(BookService.class, handler);
  ```

  



**ByteBuddy**

- 바이트 코드 조작 뿐만 아니라, 다이나믹 프록시를 생성할 때도 사용 가능

  ``` java
  Class<? extends BookService> proxyClass = new ByteBuddy().subclass(BookService.class)
    // rent 메서드에만 적용
    .method(named("rent")).intercept(InvocationHanderAdapter.of(new InvocationHandler() {
    BookeService bookService = new BookService();
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // 전처리
      return method.invoke(bookService, args);
      // 후처리
      
      return invoke;
    }
  }))
    .make().load(BookService.class.getClassLoader()).getLoaded();
  
  BookService bookService = proxyClass.getConstructor(null).newInstance();
  ```



**서브 클래스를 만드는 방법의 단점**

- 상속을 사용하지 못하는 경우 프록시를 만들 수 없다.
  - `private` or `final`
- 인터페이스가 있을 때는 인터페이스의 프록시를 만들어서 사용할 것!
