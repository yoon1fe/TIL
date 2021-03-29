### Single Source Shortest Path

**Single Source Shortest Path**란 **하나의 출발점(Single Source)에서 모든 노드까지 도달하는데 걸리는 비용(시간)을 계산하여 최단 경로를 구하는 것**입니다.

 

우선 최단 경로의 정의는 **간선의 가중치가 있는 그래프에서 두 노드 사이의 경로들 중에 간선의 가중치의 합이 최소인 경로**라고 할 수 있습니다.

 

다익스트라 알고리즘은 음수 가중치가 없는 그래프에서 사용할 수 있습니다.

음수 가중치가 있을 때는 **벨만-포드 알고리즘**을 사용할 수 있습니다. 요것은 다음에 정리를 해보겠습니다.

 

참고로 모든 노드들에 대한 최단 경로(All Pairs Shortest Path)로는 플로이드-와샬 알고리즘이란게 쓰입니다. 얘는 삼중 for문으로 간단하게 구현할 수 있는데, 얘도 담에 정리해보겠슴다..

 

 

다익스트라 알고리즘은 그리디한 접근으로 Minimum Spanning Tree를 구하는 프림 알고리즘과 비슷합니다.

 

 

#### Dijkstra's Algorithm

우선 기본적으로 필요한 변수는 두 개가 있습니다. (V는 노드의 개수)

- 출발점으로부터의 최단 거리를 저장할 배열 **dist[V]**
- 노드 방문 여부를 표시하는 **visited[V]**

순서는 다음과 같습니다.

1. dist에는 나올 수 있는 가장 큰 값으로 초기화 해줍니다. `final int INF = Integer.MAX_VALUE;`

2. 시작 노드(s)의 dist 값을 0으로 바꾸어줍니다. 이는 s->s 까지의 거리를 의미하므로 0입니다. `dist[s] = 0;`

   그리고 시작 노드의 방문 여부도 업데이트 해줍니다. `visited[s] = true;`

3. 시작 노드와 연결되어 있는 노드들의 dist 값을 업데이트해줍니다.

4. 그 후 방문하지 않은 노드 중 dist 값이 최소인 노드를 찾아줍니다.

5. 찾은 노드의 방문 여부를 업데이트하고, 이 노드와 인접한 노드들 중 방문하지 않은 노드의 dist값들을 업데이트 해줍니다. 이 때, 기존의 **dist값 보다 왔던 노드에서 가는 거리가 더 짧은 경우**에만 업데이트해줍니다.

- 모든 노드를 방문할 때까지 4~5를 반복합니다.

 

저도 무슨 말인지 잘 모르겠으니깐 직접 해보겠습니다.

여기서 i번 노드에서 j번 노드로 가는 엣지의 가중치를 `w[i][j]`로 표현하겠습니다.



![img](https://blog.kakaocdn.net/dn/but0K7/btqHGUSayaD/oIrnryULwpHIqJtkTh97Y1/img.png)



1에서 출발해서 모든 노드까지의 최단 경로를 구해봅시다.

 

먼저 1번 노드의 방문 여부를 갱신하고, 인접한 2, 3번 노드의 dist값을 업데이트해 줍시다.

 



![img](https://blog.kakaocdn.net/dn/cRKsQP/btqHNwCZn7N/FnXkJLLZcKhgwrVE4JXY71/img.png)



이제 방문하지 않은 노드들 중에서 dist 값이 가장 작은 친구를 골라줍시다. 2번 노드가 되겠네요.

 



![img](https://blog.kakaocdn.net/dn/b6Y9Wd/btqHJqDjcDd/1nWrkyi0lD0iuNaLuik8c1/img.png)



2번 노드에서 인접한 친구들을 찾아봅시다. 3번 노드랑 4번 노드가 있습니다.

 

3번 노드인 경우, dist[3]은 3으로, 2(dist[2]) + 4(`w[2][3]`)보다 큽니다. 이 말인즉슨, 1번 노드에서 3번 노드로 바로 가는게(3) 2번 노드를 경유하고 3번 노드로 가는 것(2+4)보다 짧다는 의미입니다. 따라서 갱신 하지 않습니다.

4번 노드인 경우, dist[4]가 INF입니다. dist[2] + `w[2][4]`가 당근 작기 때문에 7로 업데이트해줍니다.

 

그 다음 다시 dist 중에서 가장 작은 3번 노드를 골라줍시다.



![img](https://blog.kakaocdn.net/dn/DE4cr/btqHMwpwGC9/UCppr4I9vjIfumIQWDkKsk/img.png)



3번 노드와 인접한 노드는 4번 노드입니다. dist[4]는 7이고, 이는 dist[3] + `w[3][4]` 보다 작기 때문에 갱신하지 맙시다.

 

 

다음으로는 4번 노드를 선택하는데, 더이상 인접한 곳이 없기 때문에 스킵하고 마지막 5번 노드로 갑시다.



![img](https://blog.kakaocdn.net/dn/okAD3/btqHGVjhdZx/4De7zRb5Te8c8NfKy1C0j0/img.png)



 

5번 노드에서는 1번 노드로 갈 수 있는데 dist[1] < `w[5][1]` 이기 때문에 갱신하지 않습니다.

 

자 이제 모든 노드를 방문했습니다.

dist 배열에 저장된 값은 1번 노드에서부터 각 노드까지의 최단 거리가 된 것입니다.

 

 

이제 구현한 코드를 한번 보시져.

```
public class DijkstraTest {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int V = Integer.parseInt(br.readLine());
        int start = 0 ;
        int end = V-1;
        final int INF = Integer.MAX_VALUE;

        int[][] matrix = new int[V][V];
        int[] distance = new int[V];        // 출발지에서 자신까지 오는 최단거리
        boolean[] visited = new boolean[V];

        for (int i = 0; i < V; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine(), " ");
            for(int j = 0; j < V; j++) {
                matrix[i][j] = Integer.parseInt(st.nextToken());
            }
        }
        Arrays.fill(distance, INF);
        distance[start] = 0;

        int min = 0, current = 0;
        for(int i = 0; i < V; i++) {

            min = INF;
            // 1단계 : 방문하지 않은 정점들 중 출발지에서 자신까지 오는 비용이 최단인 정점을 경유지로 선택
            for(int j = 0; j < V; j++) {
                if(!visited[j] && min > distance[j]) {
                    min = distance[j];
                    current = j;
                }
            }

            visited[current] = true;
            if(current == end) break;

            // 2단계 : 선택된 current 정점을 경유지로 해서 아직 방문하지 않은 다른 정점으로의 최단 거리 비용 계산
            for(int j = 0; j < V; j++) {
                // min == distance[current]
                if(!visited[j] && matrix[current][j] != 0 && distance[j] > min + matrix[current][j]) {
                    distance[j] = min + matrix[current][j];
                }
            }
        }
        System.out.println(distance[end]);
    }
}
```

인접 행렬로 표현한 그래프에서 SSSP를 구하는 코드입니다.

인접 행렬로 그래프를 표현한 경우 N^2 만큼 봐줘야 하기 때문에 비효율적입니다.

그리고, dist 중 가장 작은 값을 구하는 부분을 우선순위 큐를 이용해서 더욱 효율적으로 작성할 수도 있습니다.

```
Map<Integer, List<Node>> graph = new HashMap<>();

public static void Dijkstra(int s) {
    PriorityQueue<Node> q = new PriorityQueue<>();

    boolean[] v = new boolean[V + 1];
    q.offer(new Node(s, 0));
    dist[s] = 0;

    while(!q.isEmpty()) {
        Node cur = q.poll();
        int curPos = cur.to;

        if(v[curPos]) continue;
        v[curPos] = true;

        for(Node node : graph.get(curPos)) {
            if(dist[node.to] > dist[curPos] + node.weight) {
                dist[node.to] = dist[curPos] + node.weight;
                q.add(new Node(node.to, dist[node.to]));
            }
        }
    }
}
```

해시맵으로 그래프를 표현했습니다.

반복문을 획기적으로 줄일 수 있답니다.