package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Builds substring predicates that ignore case and accents through the database function
 * search_fold.
 */
public final class SearchPredicate {

  private SearchPredicate() {}

  /**
   * Builds a predicate matching a property that contains the query, with the database folding case
   * and accents of both sides. The wildcards of the query and its escape character match
   * themselves.
   *
   * @param path property to search
   * @param rawQuery text to find, as typed by the user
   * @return the substring predicate
   */
  public static BooleanExpression contains(StringPath path, String rawQuery) {
    return Expressions.booleanTemplate(
        "cast(search_fold({0}) as string) like concat('%', cast(search_fold({1}) as string), '%') escape '!'",
        path, rawQuery.replaceAll("[!%_]", "!$0"));
  }
}
