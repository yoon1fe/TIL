## 책 통계

### 프로덕션 코드

**요구사항**

- 책 통계 화면
  - 현재 대여 중인 책이 몇 권인지 보여준다.
  - 분야별로 도서관에 등록되어 있는 책이 각각 몇 권인지 보여준다.



```kotlin
class BookStatResponse(
    val type: BookType,
    var count: Int,
) {
    fun plusOne() {
        count++
    }
}
```



```kotlin
@Service
class BookService(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
  	...
  
    @Transactional(readOnly = true)
    fun countLoanedBook(): Int {
        return userLoanHistoryRepository.findAllByStatus(UserLoanStatus.LOADNED).size
    }

    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        val results = mutableListOf<BookStatResponse>()
        val books = bookRepository.findAll()
        for (book in books) {
            val targetDto = results.firstOrNull { dto -> book.type == dto.type }
            if (targetDto == null) {
                results.add(BookStatResponse(book.type, 1))
            } else {
                targetDto.plusOne()
            }
        }

        return results
    }
}
```

- `getBookStatistics()` 간결하게

  ``` kotlin
       @Transactional(readOnly = true)
      fun getBookStatistics(): List<BookStatResponse> {
          val results = mutableListOf<BookStatResponse>()
          val books = bookRepository.findAll()
          for (book in books) {
              results.firstOrNull { dto -> book.type == dto.type }?.plusOne()
                  ?: results.add(BookStatResponse(book.type, 1))
          }
  
          return results
      }
  ```



### 테스트 코드 & 리팩토링

**테스트 코드**

```kotlin
    @Test
    @DisplayName("책 대여 권수를 정상 조회한다")
    fun countLoanBookTest() {
        // given
        val savedUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "A"),
            UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
            UserLoanHistory.fixture(savedUser, "C", UserLoanStatus.RETURNED),
        ))

        // when
        val result = bookService.countLoanedBook()

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 대여권수를 정상 조회한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
            Book("A", BookType.COMPUTER),
            Book("B", BookType.COMPUTER),
            Book("C", BookType.SCIENCE),
        ))

        // when
        val results = bookService.getBookStatistics()

        // then
        assertThat(results).hasSize(2)
//        val computerDto = results.first { dto -> dto.type == BookType.COMPUTER }
//        assertThat(computerDto.count).isEqualTo(2)
        assertCount(results, BookType.COMPUTER, 2)

//        val scienceDto = results.first { dto -> dto.type == BookType.SCIENCE }
//        assertThat(scienceDto.count).isEqualTo(1)
        assertCount(results, BookType.SCIENCE, 1)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Int) {
        assertThat(results.first { dto -> dto.type == type}.count).isEqualTo(count)
    }
```



**리팩토링**

콜체인이 너무 길면 가독성이나 유지보수가 어렵다.

- AS-IS

  ``` kotlin
       @Transactional(readOnly = true)
      fun getBookStatistics(): List<BookStatResponse> {
          val results = mutableListOf<BookStatResponse>()
          val books = bookRepository.findAll()
          for (book in books) {
              results.firstOrNull { dto -> book.type == dto.type }?.plusOne()
                  ?: results.add(BookStatResponse(book.type, 1))
          }
  
          return results
      }
  ```

- TO-BE

  ``` kotlin
  @Transactional(readOnly = true)
      fun getBookStatistics(): List<BookStatResponse> {
          return bookRepository.findAll()     // List<Book>
              .groupBy { book -> book.type }  // Map>
              .map { (type, books) -> BookStatResponse(type, books.size) }
  }
  ```



## 애플리케이션 대신 DB로 기능 구현

```kotlin
@Transactional(readOnly = true)
fun countLoanedBook(): Int {
    return userLoanHistoryRepository.countByStatus(UserLoanStatus.LOADNED).toInt()
    // return userLoanHistoryRepository.findAllByStatus(UserLoanStatus.LOADNED).size

}
```

- 서버 코드를 보고 메모리를 생각할 수 있어야 한다. `findAll` 로 가져오면 테이블의 모든 데이터가 메모리에 올라오게 된다...



```kotlin
@Query("select new com.group.libraryapp.dto.book.response.BookStatResponse(b.type, COUNT(b.id)) from Book b group by b.type")
fun getStats(): List<BookStatResponse>
```

- JPQL로 특정 DTO로 바로 변환