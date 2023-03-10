안녕하세용.

이번 글에서는 `Integer.valueOf()` 캐싱에 대해 간단히 알아보겠습니다. 며칠전 사내 기술교육에서 아이스 브레이킹으로 강사님이 언급해주신 내용인데 흥미로워서 글로 정리해봅니다.. ㅎ



아래 코드를 봅시다.

```java
System.out.println(Integer.valueOf("127") == Integer.valueOf("127"));
System.out.println(Integer.valueOf("128") == Integer.valueOf("128"));
System.out.println(Integer.parseInt("128") == Integer.valueOf("128"));
```



위 코드를 실행하면 다음과 같은 결과가 출력됩니다.

```java
true
false
true
```



127을 비교한 결과는 우리 예상대로 `true`가 나오는데, **128**을 비교했을 때는 `false`가 나옵니다. `parseInt()`를 썼을 때는 또 `true`가 나오구요. 오.. 왜 이럴까요??



먼저 `Integer.valueOf()`와 `parseInt()` 메서드에 대해서 알아봅시다.

```java
public static Integer valueOf(String s) throws NumberFormatException {
    return Integer.valueOf(parseInt(s, 10));
}
```



``` java
public static int parseInt(String s, int radix) throws NumberFormatException{ 
    ... 
}
```



`valueOf()` 메서드는 `Integer` 클래스의 객체를 반환합니다. `praseInt()`는 `int primitive type`을 반환하구요. `valueOf()` 내부적으로는 `parseInt()` 메서드를 사용해서 나온 값을 다시 한 번 `Integer.valueOf(int)` 메서드를 통해 `Integer` wrapper 클래스로 감싸서 반환해주고 있습니다. 그럼 `Integer.valueOf(int)` 요 메서드가 관건이겠네요.



```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}

private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }
```



바로 여기에 128이란 숫자의 비밀이 숨어 있습니다. 바로 **-128 ~127** 사이의 숫자는 Integer 타입의 인스턴스를 미리 캐싱해두는 것입니다. 상한값은 설정으로 조절할 수 있구요(`-XX:AutoBoxCacheMax=<size>`).



`Integer.valueOf("127")` 가 반환해주는 `Integer` 인스턴스는 캐싱된 인스턴스이고, `Integer.valueOf("128")`의 반환값은 `new` 를 통해 새로 생성된 인스턴스인 것이죠. 따라서 `Integer.valueOf("128") == Integer.valueOf("128")` 에서 비교되는 둘은 서로 다른 인스턴스이기 때문에 `false`를 반환하는 것입니다.



물론 `Integer` wrapper 클래스는 `equals`로 비교하는 것이 바람직하지만, 혹여나 128 이상의 숫자를 `==`으로 비교했을 때 원하는 값이 나오지 않더라도 당황하지 말기로 해요~











**Reference**

https://stackoverflow.com/questions/20877086/why-do-comparisons-with-integer-valueofstring-give-different-results-for-12

https://stackoverflow.com/questions/508665/different-between-parseint-and-valueof-in-java

https://meetup.nhncloud.com/posts/185