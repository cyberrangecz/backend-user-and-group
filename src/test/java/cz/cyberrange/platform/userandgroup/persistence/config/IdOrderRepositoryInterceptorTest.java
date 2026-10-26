package cz.cyberrange.platform.userandgroup.persistence.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class IdOrderRepositoryInterceptorTest {

  static Stream<Arguments> requestedPagesAndExpectedSorts() {
    return Stream.of(
        Arguments.of(PageRequest.of(0, 20), Sort.by(Sort.Order.asc("id"))),
        Arguments.of(
            PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "fullName")),
            Sort.by(Sort.Order.desc("fullName"), Sort.Order.asc("id"))),
        Arguments.of(
            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id")),
            Sort.by(Sort.Order.desc("id"))),
        Arguments.of(Pageable.unpaged(), Sort.by(Sort.Order.asc("id"))),
        Arguments.of(
            Pageable.unpaged(Sort.by(Sort.Direction.DESC, "id")), Sort.by(Sort.Order.desc("id"))));
  }

  @ParameterizedTest
  @MethodSource("requestedPagesAndExpectedSorts")
  void withIdOrderCompletesTheSortAndKeepsThePaging(Pageable requested, Sort expectedSort) {
    Pageable completed = IdOrderRepositoryInterceptor.withIdOrder(requested);

    assertEquals(expectedSort, completed.getSort());
    assertEquals(requested.isPaged(), completed.isPaged());
    if (requested.isPaged()) {
      assertEquals(requested.getPageNumber(), completed.getPageNumber());
      assertEquals(requested.getPageSize(), completed.getPageSize());
    }
  }
}
