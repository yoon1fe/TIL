## URI & URL & URN

URI와 URL. 상당히 헷갈리는 단어다. 나도 이때까진 같은 의미로 사용해왔었다.

보통 어디 웹페이지의 주소라 함은 URL이라고 많이 칭했었는데, 요번에 URI 와 URL (+ URN)에 대해서 정리해봐야겠다.



#### [URI]

먼저 **통합 자원 식별자(Uniform Resource Identifier, URI)** 란 인터넷에 있는 자원을 나타내는 유일한 주소이다. URI의 존재는 인터넷에서 요구되는 기본조건으로서, 인터넷 프로토콜에 항상 붙어 다닌다. 

URI는 다음과 같은 형태를 나타낸다.

` scheme:[//[user[:password]@]host[:port]][/path][?query][#fragment]`



이러한 URI의 하위 개념으로 **URL**과 **URN**이 있다.



#### [URL]

**통합 자원 지시자(Uniform Resource Locator, URL)**는 URI의 가장 흔한 형태이다. URL은 특정 서버의 한 리소스에 대한 구체적인 **위치(Location)**를 서술한다. URL은 리소스가 정확히 어디에 있고 어떻게 접근할 수 있는지를 분명하게 알려준다.

다음은 URL의 문법이다.

`scheme:[//[user:password@]host[:port]][/]path[?query][#fragment]`

URI랑 똑같다.



아래는 모두 URL의 예시이다.

- `http://naver.com` - 네이버 사이트의 URL
- `http://img.naver.net/static/www/dl_qr_naver.png` - 네이버 앱 QR 코드의 이미지에 대한 URL
- `http://news.naver.com/main/main.nhn?mode=LSD&mid=shm&sid1=104` - 네이버 뉴스에서 분류 중 "세계" 주제의 기사에 대한 URL

여러 블로그를 찾아봤는데 쿼리 쿼리 스트링이 포함되면 URL이 아니라고 하는 곳이 많았다. 하지만 [위키](https://ko.wikipedia.org/wiki/URL)나 [MDN](https://developer.mozilla.org/ko/docs/Learn/Common_questions/What_is_a_URL)을 찾아보니 쿼리 스트링도 포함되나보다.. 허허





#### [URN]

**통합 자원 이름(Uniform Resource Name, URN)**은 `urn:scheme`을 사용하는 URI를 위한 이름이다. 무슨 말인지 잘 모르겠다. URN은 콘텐츠를 이루는 한 리소스에 대해, 그 리소스의 위치에 영향을 받지 않는 유일무이한 이름 역할을 한다. 위치에 독립적이기 때문에 URL과 달리 리소스를 여기저기로 옮기더라도 문제없이 그 자원에 접근할 수 있는 것이다.

이러한 URN은 URL의 한계로 인해 착수되었지만 아직 채택되지 않아 접할 기회가 많이 없었을 것이다. 여기서 말하는 URL의 한계란, URL은 **위치**를 나타내는 주소이지, 실제 이름이 아니란 것이다.

예를 들어, `https://yoon1fe.tistory.com/134`에 있는 글을 카테고리를 나눈다고 `https://yoon1fe.tistory.com/CS/134`로 URL을 바꾸었다. 이렇게 되면 더이상 `https://yoon1fe.tistory.com/134` 주소로는 원래 글을 찾을 수 없다. 

즉, URL은 **특정 시점에 어떤 것이 위치한 곳을 알려준다**는 단점이 있다.

이러한 단점을 해결하기 위해서 그 자원을 가리키는 실제 자원의 이름을 사용해 자원에 접근하는 방법이 나온 것이다. 이렇게 하면 자원의 위치가 바뀌더라도 해당 자원을 찾을 수 있게 된다.



#### [결론]

**요약하자면, URL과 URL은 URI의 종류이다.**

**모든 URL은 URI이고, 또한 모든 URN은 URI이다.**

**그리고 URL과 URN은 다르다. 즉, 모든 URI는 URL이라고 말할 수 없다.**



![URI, URL, URN](https://t1.daumcdn.net/cfile/tistory/2416C94158D62B9E11)







단순하게 말하면, URI는 규약이고, URL은 규약에 대한 형태라고 생각하면 편할 것이다. URI(개) URL(퍼그) URN(프렌치 불독) 정도로 이해하자 허허









##### Reference

https://ko.wikipedia.org/wiki/%ED%86%B5%ED%95%A9_%EC%9E%90%EC%9B%90_%EC%8B%9D%EB%B3%84%EC%9E%90

https://mygumi.tistory.com/139

https://developer.mozilla.org/ko/docs/Learn/Common_questions/What_is_a_URL

https://ko.wikipedia.org/wiki/URL

https://lambdaexp.tistory.com/39

https://goodgid.github.io/URL-URI-URN/