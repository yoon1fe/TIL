 [레퍼런스(https://github.com/qkraudghgh/coding-interview/blob/master/Interview/question/previous_interview.md)](https://github.com/qkraudghgh/coding-interview/blob/master/Interview/question/previous_interview.md)



### Sorting Algorithm에서 Stable 하다는 것은 무엇을 의미하나요?

>정렬할 엘리먼트들 중에서 중복된 값이 있을 때, 소팅 전과 후의 이 중복된 값들의 순서가 바뀌지 않음을 보장하는 것을 stable하다고 한다.





### Sorting Algorithm이 가짓수가 많은데 그 이유가 무엇일 것 같나요?

> 정렬해야 할 데이터의 종류나 개수에 따라 효율적일 수 있는 알고리즘이 다르기 때문이라고 생각한다. 예를 들어. 데이터의 개수가 많지 않을 경우에는 구현이 비교적 단순한 선택 정렬이나 버블 정렬을 사용해도 될 것이다. 또한, 이처럼 우선 시간복잡도를 따져 보고 적절한 소팅 알고리즘을 정할 수도 있을 것이고, 또는 메모리사용량을 고려해보고 in-place 한 알고리즘을 사용해야 할지, 아니면 merge sort와 같은 in-place 하지 않은 알고리즘을 사용해도 될지 결정할 수 있을 것이다.



### Quick sort에 대해서 설명해 줄 수 있나요?

> 일반적으로 성능이 가장 좋은 정렬 알고리즘이다. 최악의 경우 O(n^2)이지만, 베스트 케이스나 평균 케이스에서는 O(nlogn)의 시간복잡도를 가진다. C++의 stl 에 있는 sort() 함수도 quick sort 기반인 것으로 알고 있다. 알고리즘은 우선 랜덤하게 피벗을 정하고, 그 피벗보다 작은 값은 왼쪽, 큰 값은 오른쪽으로 옮기도 분할 정복 방식을 적용하여 각각 왼쪽, 오른쪽 에서 다시 피벗을 정하고 두 부분으로 나누는 식으로 정렬해나간다.