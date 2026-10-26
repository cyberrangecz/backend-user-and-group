package cz.cyberrange.platform.userandgroup.persistence.repository;

import static cz.cyberrange.platform.userandgroup.persistence.repository.SearchPredicate.contains;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.persistence.entity.QUser;
import java.util.Arrays;

/** Builds the user filter that matches free text against the given name and the family name. */
public final class UserNameFilter {

  private UserNameFilter() {}

  /**
   * Narrows a user predicate to the users whose names match the given text. The text is split into
   * words on whitespace, and each word must be contained, ignoring case and accents, in the given
   * name or in the family name. Blank or missing text leaves the predicate unrestricted.
   *
   * @param predicate filter to narrow; may be null or empty
   * @param nameText free text of whitespace-separated words; may be null
   * @return a new predicate matching users that satisfy both the given predicate and the text
   */
  public static Predicate narrow(Predicate predicate, String nameText) {
    BooleanBuilder narrowed = new BooleanBuilder(predicate);
    if (nameText != null) {
      Arrays.stream(nameText.trim().split("\\s+"))
          .filter(word -> !word.isEmpty())
          .forEach(
              word ->
                  narrowed.and(
                      contains(QUser.user.givenName, word)
                          .or(contains(QUser.user.familyName, word))));
    }
    return narrowed;
  }
}
