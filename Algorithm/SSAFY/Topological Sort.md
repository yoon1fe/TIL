## Topological Sort(위상 정렬)

### 위상 정렬이란?

**위상 정렬**(topological sorting)은 유향 그래프의 꼭짓점들(vertex)을 변의 방향을 거스르지 않도록 나열하는 것을 의미한다. 위상정렬을 가장 잘 설명해 줄 수 있는 예로 대학의 선수과목(prerequisite) 구조를 예로 들 수 있다. 만약 특정 수강과목에 선수과목이 있다면 그 선수 과목부터 수강해야 하므로, 특정 과목들을 수강해야 할 때 위상 정렬을 통해 올바른 수강 순서를 찾아낼 수 있다. 이와 같이 선후 관계가 정의된 그래프 구조 상에서 선후 관계에 따라 정렬하기 위해 위상 정렬을 이용할 수 있다. 정렬의 순서는 유향 그래프의 구조에 따라 여러 개의 종류가 나올 수 있다. 위상 정렬이 성립하기 위해서는 반드시 그래프의 순환이 존재하지 않아야 한다. 즉, 그래프가 비순환 유향 그래프(directed acyclic graph)여야 한다.

*출처: [위키백과](https://ko.wikipedia.org/wiki/%EC%9C%84%EC%83%81%EC%A0%95%EB%A0%AC)*





**위상 정렬**이란 방향이 있는 그래프에 존재하는 각 정점들의 선행 순서를 위배하지 않으면서 모든 정점들을 나열하는 것을 말합니다.

위상 정렬은 하나의 유향 그래프에서 여러 위상 정렬이 가능합니다. 그리고 싸이클이 없는(DAG, Directed Acyclic Graph) 그래프에서만 위상 정렬을 수행할 수 있습니다.



최근 IT 기업들의 코딩 테스트에서 가끔씩 나온답니다. 어떤 일을 할 때 다른 일이 선행되어야 하는 조건이 붙은 문제에서 위상 정렬을 한 뒤 쉽게 답을 도출해낼 수 있겠죠.



위상 정렬을 수행하는 방식은 크게 두 가지가 존재합니다. 하나는 DFS를 이용하는 방법, 다른 하나는 Queue를 이용하는 방법입니다.



### DFS 이용하는 방법

DFS를 이용해서 위상 정렬을 뒤에서부터 만들어 나가는 방법입니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcqzrsK%2FbtqJxq2dJuG%2FKKCZ4qrFr9KfKYeUtfxkR1%2Fimg.png)

![img](https://blog.kakaocdn.net/dn/lUlNg/btqJs5dvCau/KhIeAcF5Kmbliw0BkEo8qk/img.png)

![img](https://blog.kakaocdn.net/dn/In1N4/btqJrxhg1NL/j6SVEKrSoq09ABLSgKuN20/img.png)





DFS를 모두 수행하면 이런 그래프가 만들어 지는 것이죠.

![img](https://blog.kakaocdn.net/dn/bblUXu/btqJrwP9Kaa/eB7kkLdOliRAISMHNRcYsK/img.png)



### Queue를 사용하는 방법

각 정점의 진입 차수(진입하는 노드의 수)를 구해놓고 시작합니다.

Queue를 사용해서 위상 정렬을 하는 로직은 다음과 같습니다.

1. 진입 차수가 0인 정점을 Queue에 넣습니다.
2. Queue에서 원소를 꺼내 연결된 모든 간선을 삭제합니다.
3. 간선 삭제 이후에 진입 차수가 0이 된 정점을 Queue에 넣습니다.
4. Queue가 빌 때까지 2~3번을 반복합니다. 모든 원소를 방문하기 전에 Queue가 빈다면 싸이클이 존재한다는 것이고, 모든 원소를 방문했다면 Queue에서 꺼낸 순서가 위상 정렬의 결과가 됩니다.



![img](https://blog.kakaocdn.net/dn/mGFIV/btqJzskLL4v/cOfTrvz27XOda84riJiGv0/img.png)

![img](https://blog.kakaocdn.net/dn/Jg2NP/btqJxad7cXr/Dlk7j1KwsSNm9YevFEWKU0/img.png)

![img](https://blog.kakaocdn.net/dn/GqvnT/btqJtZRIdE7/KT0feYA9ogmmjxc3VEfrcK/img.png)

![img](https://blog.kakaocdn.net/dn/boDHJb/btqJvT4H7kF/6mMTPwMD81uYmbuSKjhMj1/img.png)





위상 정렬의 시간 복잡도는 **O(V + E)**가 됩니다. 



### 추천 문제

[B2252 줄 세우기](https://www.acmicpc.net/problem/2252)

[B1005 ACM Craft](https://www.acmicpc.net/problem/1005)

[B2529 부등호](https://www.acmicpc.net/problem/2529)

[B1766 문제집](https://www.acmicpc.net/problem/1766)

[2020 카카오 인턴십 동굴탐험](https://programmers.co.kr/learn/courses/30/lessons/67260)





감사합니다!