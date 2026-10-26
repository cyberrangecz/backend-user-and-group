package cz.cyberrange.platform.userandgroup.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.QUser;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.util.LinkedMultiValueMap;

@DataJpaTest
class UserRepositoryTest {

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TestEntityManager entityManager;
  @Autowired private UserRepository userRepository;

  private Role adminRole, guestRole, userRole;
  private IDMGroup group1, group2, group3;
  private User user1, user2, user3, user4;

  @BeforeEach
  public void setup() {
    Microservice microservice = testDataFactory.getCrczpTrainingMicroservice();
    this.entityManager.persistAndFlush(microservice);

    adminRole = testDataFactory.getUAGAdminRole();
    adminRole.setMicroservice(microservice);
    this.entityManager.persistAndFlush(adminRole);

    guestRole = testDataFactory.getUAGTraineeRole();
    guestRole.setMicroservice(microservice);
    this.entityManager.persistAndFlush(guestRole);

    userRole = testDataFactory.getUAGPowerUserRole();
    userRole.setMicroservice(microservice);
    this.entityManager.persistAndFlush(userRole);

    group1 = testDataFactory.getUAGDefaultGroup();
    group1.setRoles(Set.of(guestRole));
    group2 = testDataFactory.getTrainingAdminGroup();
    group2.setRoles(Set.of(adminRole));
    group3 = testDataFactory.getUAGPowerUserGroup();
    group3.setRoles(Set.of(userRole));

    user1 = testDataFactory.getUser1();
    user2 = testDataFactory.getUser2();
    user3 = testDataFactory.getUser3();
    user4 = testDataFactory.getUser4();

    entityManager.persistAndFlush(user1);
    entityManager.persistAndFlush(user2);
    entityManager.persistAndFlush(user3);
    entityManager.persistAndFlush(user4);
  }

  @Test
  public void getLogin() throws Exception {
    String expectedLogin = "user1";
    User u =
        this.entityManager.persistAndFlush(
            new User(expectedLogin, "https://oidc.provider.cz/oidc/"));
    Optional<String> optionalLogin = this.userRepository.getSub(u.getId());
    String sub = optionalLogin.orElseThrow(() -> new Exception("User's sub should be found"));
    assertEquals(expectedLogin, sub);
  }

  @Test
  public void getLoginUserNotFound() {
    assertFalse(this.userRepository.getSub(100000000L).isPresent());
  }

  @Test
  public void getRolesOfUser() {
    entityManager.persistAndFlush(adminRole);
    entityManager.persistAndFlush(guestRole);

    group1.addUser(user1);
    group1.setRoles(Stream.of(adminRole).collect(Collectors.toSet()));
    entityManager.persistAndFlush(group1);

    Set<Role> userRoles = userRepository.getRolesOfUser(user1.getId());
    assertEquals(1, userRoles.size());
    assertTrue(userRoles.contains(adminRole));
    assertFalse(userRoles.contains(guestRole));
  }

  @Test
  public void getUserByLoginWithGroups() {
    group1.addUser(user1);
    group2.addUser(user1);
    entityManager.persistAndFlush(group1);
    entityManager.persistAndFlush(group2);
    entityManager.persistAndFlush(group3);

    Optional<User> userWithGroups =
        userRepository.getUserBySubWithGroups(user1.getSub(), user1.getIss());
    assertTrue(userWithGroups.isPresent());
    assertTrue(userWithGroups.get().getGroups().contains(group1));
    assertTrue(userWithGroups.get().getGroups().contains(group2));
    assertFalse(userWithGroups.get().getGroups().contains(group3));
  }

  @Test
  public void usersNotInGivenGroup() {
    group1.addUser(user1);
    group1.addUser(user3);
    entityManager.persistAndFlush(group1);

    List<User> usersNotInGroup =
        userRepository
            .usersNotInGivenGroup(group1.getId(), null, PageRequest.of(0, 10))
            .getContent();
    assertFalse(usersNotInGroup.containsAll(Set.of(user1, user3)));
    assertTrue(usersNotInGroup.containsAll(Set.of(user2, user4)));
  }

  @Test
  public void usersInGivenGroups() {
    group1.setUsers(Set.of(user1, user2));
    entityManager.persistAndFlush(group1);
    group2.setUsers(Set.of(user2, user4));
    entityManager.persistAndFlush(group2);

    List<User> usersInGroups =
        userRepository
            .usersInGivenGroups(Set.of(group1.getId(), group2.getId()), null, PageRequest.of(0, 10))
            .getContent();
    assertEquals(3, usersInGroups.size());
    assertTrue(usersInGroups.containsAll(Set.of(user1, user2, user4)));
    assertFalse(usersInGroups.contains(user3));
  }

  @Test
  public void findAllByRoleId() {
    this.entityManager.persistAndFlush(adminRole);
    this.entityManager.persistAndFlush(guestRole);
    group1.setUsers(Set.of(user2, user3));
    group1.setRoles(Set.of(adminRole, guestRole));
    this.entityManager.persistAndFlush(group1);
    group2.setUsers(Set.of(user1, user3));
    group2.setRoles(Set.of(guestRole));
    this.entityManager.persistAndFlush(group2);

    Page<User> usersWithGuestRole =
        userRepository.findAllByRoleId(guestRole.getId(), null, PageRequest.of(0, 10));
    assertEquals(3, usersWithGuestRole.getContent().size());
    assertTrue(usersWithGuestRole.getContent().containsAll(Set.of(user1, user2, user3)));
    assertFalse(usersWithGuestRole.getContent().contains(user4));

    Page<User> usersWithAdminRoleRole =
        userRepository.findAllByRoleId(adminRole.getId(), null, PageRequest.of(0, 10));
    assertEquals(2, usersWithAdminRoleRole.getContent().size());
    assertTrue(usersWithAdminRoleRole.getContent().containsAll(Set.of(user2, user3)));
    assertFalse(usersWithAdminRoleRole.getContent().containsAll(Set.of(user1, user4)));
  }

  @Test
  public void findAllByRoleType() {
    this.entityManager.persistAndFlush(adminRole);
    this.entityManager.persistAndFlush(guestRole);
    group1.setUsers(Set.of(user2, user3));
    group1.setRoles(Set.of(adminRole, guestRole));
    this.entityManager.persistAndFlush(group1);
    group2.setUsers(Set.of(user1, user3));
    group2.setRoles(Set.of(guestRole));
    this.entityManager.persistAndFlush(group2);

    Page<User> usersWithGuestRole =
        userRepository.findAllByRoleType(guestRole.getRoleType(), null, PageRequest.of(0, 10));
    assertEquals(3, usersWithGuestRole.getContent().size());
    assertTrue(usersWithGuestRole.getContent().containsAll(Set.of(user1, user2, user3)));
    assertFalse(usersWithGuestRole.getContent().contains(user4));

    Page<User> usersWithAdminRoleRole =
        userRepository.findAllByRoleType(adminRole.getRoleType(), null, PageRequest.of(0, 10));
    assertEquals(2, usersWithAdminRoleRole.getContent().size());
    assertTrue(usersWithAdminRoleRole.getContent().containsAll(Set.of(user2, user3)));
    assertFalse(usersWithAdminRoleRole.getContent().containsAll(Set.of(user1, user4)));
  }

  @Test
  public void findAllByRoleAndNotWithIds() {
    this.entityManager.persistAndFlush(adminRole);
    this.entityManager.persistAndFlush(guestRole);
    group1.setUsers(Set.of(user2, user3));
    group1.setRoles(Set.of(adminRole, guestRole));
    this.entityManager.persistAndFlush(group1);
    group2.setUsers(Set.of(user1, user3));
    group2.setRoles(Set.of(guestRole));
    this.entityManager.persistAndFlush(group2);

    Page<User> usersWithGuestRole =
        userRepository.findAllByRoleAndNotWithIds(
            null,
            PageRequest.of(0, 10),
            guestRole.getRoleType(),
            Set.of(user3.getId(), user4.getId()));
    assertEquals(2, usersWithGuestRole.getContent().size());
    assertTrue(usersWithGuestRole.getContent().containsAll(Set.of(user1, user2)));
    assertFalse(usersWithGuestRole.getContent().contains(user4));

    Page<User> usersWithAdminRoleRole =
        userRepository.findAllByRoleAndNotWithIds(
            null,
            PageRequest.of(0, 10),
            adminRole.getRoleType(),
            Set.of(user3.getId(), user2.getId()));
    assertEquals(0, usersWithAdminRoleRole.getContent().size());
    assertFalse(
        usersWithAdminRoleRole.getContent().containsAll(Set.of(user1, user4, user2, user3)));
  }

  @Test
  public void getUserByIdWithGroups() {
    group1.addUser(user1);
    group2.addUser(user1);
    entityManager.persistAndFlush(group1);
    entityManager.persistAndFlush(group2);
    entityManager.persistAndFlush(group3);

    Optional<User> userWithGroups = userRepository.getUserByIdWithGroups(user1.getId());
    assertTrue(userWithGroups.isPresent());
    assertEquals(2, userWithGroups.get().getGroups().size());
    assertTrue(userWithGroups.get().getGroups().contains(group1));
    assertTrue(userWithGroups.get().getGroups().contains(group2));
    assertFalse(userWithGroups.get().getGroups().contains(group3));
  }

  @Test
  public void findBySubAndIss() {
    String expectedSub = "user1";
    this.entityManager.persist(new User(expectedSub, "https://oidc.provider.cz/oidc/"));
    Optional<User> optionalUser =
        this.userRepository.findBySubAndIss(expectedSub, "https://oidc.provider.cz/oidc/");
    assertTrue(optionalUser.isPresent());
    assertEquals(expectedSub, optionalUser.get().getSub());
    assertEquals("https://oidc.provider.cz/oidc/", optionalUser.get().getIss());
  }

  @Test
  public void findBySubAndDifferentIss() {
    String expectedSub = "user1";
    this.entityManager.persist(new User(expectedSub, "https://oidc.provider.cz/oidc/"));
    Optional<User> optionalUser =
        this.userRepository.findBySubAndIss(
            expectedSub, "https://oidc.provider-different.cz/oidc/");
    assertFalse(optionalUser.isPresent());
  }

  @Test
  public void pagedQueriesNeverRepeatNorSkipUsersWhenSortKeyTies() {
    List<User> allUsers = persistUsersWithTiedNamesInDefaultGroup();
    Comparator<User> nameThenId =
        Comparator.comparing(User::getFullName).thenComparing(User::getId);
    List<Long> expectedOrder = allUsers.stream().sorted(nameThenId).map(User::getId).toList();
    Sort sortByName = Sort.by("fullName");

    Map<String, Function<Pageable, Page<User>>> pagedQueries =
        Map.of(
            "findAll",
            pageable -> userRepository.findAll(new BooleanBuilder(), pageable),
            "usersInGivenGroups",
            pageable -> userRepository.usersInGivenGroups(Set.of(group1.getId()), null, pageable),
            "usersNotInGivenGroup",
            pageable -> userRepository.usersNotInGivenGroup(group2.getId(), null, pageable),
            "findAllByRoleId",
            pageable -> userRepository.findAllByRoleId(guestRole.getId(), null, pageable),
            "findAllByRoleType",
            pageable -> userRepository.findAllByRoleType(guestRole.getRoleType(), null, pageable),
            "findAllByRoleAndNotWithIds",
            pageable ->
                userRepository.findAllByRoleAndNotWithIds(
                    null, pageable, guestRole.getRoleType(), Set.of(0L)));

    pagedQueries.forEach(
        (queryName, pagedQuery) -> {
          List<Long> pagedIds = new ArrayList<>();
          Page<User> page;
          int pageNumber = 0;
          do {
            page = pagedQuery.apply(PageRequest.of(pageNumber++, 3, sortByName));
            page.getContent().forEach(user -> pagedIds.add(user.getId()));
          } while (page.hasNext());

          assertEquals(expectedOrder, pagedIds, queryName);
        });
  }

  @Test
  public void pagedQueryWithoutSortReturnsUsersInIdOrder() {
    List<User> allUsers = persistUsersWithTiedNamesInDefaultGroup();
    List<Long> expectedOrder = allUsers.stream().map(User::getId).sorted().toList();

    List<Long> pagedIds =
        userRepository
            .findAll(new BooleanBuilder(), PageRequest.of(0, allUsers.size()))
            .map(User::getId)
            .getContent();

    assertEquals(expectedOrder, pagedIds);
  }

  @Test
  public void requestedSortOnIdIsKeptAsRequested() {
    List<User> allUsers = persistUsersWithTiedNamesInDefaultGroup();
    List<Long> expectedOrder =
        allUsers.stream().map(User::getId).sorted(Comparator.reverseOrder()).toList();

    List<Long> pagedIds =
        userRepository
            .findAll(
                new BooleanBuilder(),
                PageRequest.of(0, allUsers.size(), Sort.by(Sort.Direction.DESC, "id")))
            .map(User::getId)
            .getContent();

    assertEquals(expectedOrder, pagedIds);
  }

  @Test
  public void queryWithoutPageableReturnsUsersInIdOrder() {
    List<User> allUsers = persistUsersWithTiedNamesInDefaultGroup();
    List<Long> expectedOrder = allUsers.stream().map(User::getId).sorted().toList();

    List<Long> returnedIds =
        userRepository
            .usersInGivenGroups(Set.of(group1.getId()), null, null)
            .map(User::getId)
            .getContent();

    assertEquals(expectedOrder, returnedIds);
  }

  @Test
  public void findByIdInOrderByIdReturnsUsersInIdOrder() {
    List<Long> descendingIds =
        Stream.of(user1, user2, user3, user4)
            .map(User::getId)
            .sorted(Comparator.reverseOrder())
            .toList();

    List<Long> returnedIds =
        userRepository.findByIdInOrderById(descendingIds).stream().map(User::getId).toList();

    assertEquals(descendingIds.reversed(), returnedIds);
  }

  @Test
  public void nameFilterMatchesSingleWordOnGivenName() {
    persistUsersForNameFilter();

    assertEquals(List.of("zqxjames"), subsMatchingNameFilter("Zqxjames"));
  }

  @Test
  public void nameFilterMatchesSingleWordOnFamilyName() {
    persistUsersForNameFilter();

    assertEquals(Set.of("zqxjames", "zqxanna"), Set.copyOf(subsMatchingNameFilter("Zqxgenerated")));
  }

  @Test
  public void nameFilterWithFullNameMatchesExactlyOneUser() {
    persistUsersForNameFilter();

    assertEquals(List.of("zqxanna"), subsMatchingNameFilter("Zqxanna Zqxgenerated"));
  }

  @Test
  public void nameFilterRequiresEveryWordToMatchEitherName() {
    persistUsersForNameFilter();

    assertTrue(subsMatchingNameFilter("Zqxjames Zqxanna").isEmpty());
    assertEquals(List.of("zqxanna"), subsMatchingNameFilter("  zqxgenerated   zqxANNA "));
  }

  @Test
  public void nameFilterIgnoresCase() {
    persistUsersForNameFilter();

    assertEquals(List.of("zqxjames"), subsMatchingNameFilter("ZQXJAMES"));
    assertEquals(List.of("zqxjames"), subsMatchingNameFilter("zqxjames zqxGENERATED"));
  }

  @Test
  public void nameFilterWithBlankTextDoesNotRestrictUsers() {
    persistUsersForNameFilter();
    long allUsersCount = userRepository.count();

    assertEquals(allUsersCount, subsMatchingNameFilter("").size());
    assertEquals(allUsersCount, subsMatchingNameFilter("   ").size());
  }

  @Test
  public void givenNameStaysFilterableAlongsideNameFilter() {
    persistUsersForNameFilter();

    assertEquals(List.of("zqxanna"), subsMatching(predicateFromParameters("givenName", "zqxanna")));
  }

  @Test
  public void familyNameStaysFilterableAlongsideNameFilter() {
    persistUsersForNameFilter();

    assertEquals(
        List.of("zqxother"), subsMatching(predicateFromParameters("familyName", "zqxfamily")));
  }

  @Test
  public void textFilterMatchesWildcardCharactersLiterally() {
    persistUsersForNameFilter();
    persistNamedUser("zqxwild", "Zqx50%_!", "Zqxwildfamily");
    persistNamedUser("zqxplain", "Zqx50abc", "Zqxwildfamily");
    entityManager.flush();
    entityManager.clear();

    assertEquals(List.of("zqxwild"), subsMatching(predicateFromParameters("givenName", "50%_!")));
    assertEquals(List.of("zqxwild"), subsMatchingNameFilter("50%_!"));
  }

  @Test
  public void textFilterMatchesMixedCaseInEitherDirection() {
    persistUsersForNameFilter();

    assertEquals(
        List.of("zqxjames"), subsMatching(predicateFromParameters("givenName", "zQxJaMeS")));
    assertEquals(
        List.of("zqxother"), subsMatching(predicateFromParameters("familyName", "ZQXFAMILY")));
  }

  @Test
  public void nameGivenNameAndFamilyNameFiltersCombine() {
    persistUsersForNameFilter();
    LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("givenName", "zqxjam");
    parameters.add("familyName", "zqxgen");

    Predicate predicate =
        UserNameFilter.narrow(predicateFromParameters(parameters), "zqxgenerated");

    assertEquals(List.of("zqxjames"), subsMatching(predicate));
  }

  private void persistUsersForNameFilter() {
    persistNamedUser("zqxjames", "Zqxjames", "Zqxgenerated");
    persistNamedUser("zqxanna", "Zqxanna", "Zqxgenerated");
    persistNamedUser("zqxother", "Zqxother", "Zqxfamily");
    entityManager.flush();
    entityManager.clear();
  }

  private void persistNamedUser(String sub, String givenName, String familyName) {
    User user = new User(sub, "https://oidc.provider.cz/oidc/");
    user.setGivenName(givenName);
    user.setFamilyName(familyName);
    user.setFullName(givenName + " " + familyName);
    entityManager.persist(user);
  }

  private List<String> subsMatchingNameFilter(String nameText) {
    return subsMatching(UserNameFilter.narrow(new BooleanBuilder(), nameText)).stream()
        .filter(sub -> sub.startsWith("zqx") || nameText.isBlank())
        .toList();
  }

  private List<String> subsMatching(Predicate predicate) {
    return userRepository.findAll(predicate, Pageable.unpaged()).stream()
        .map(User::getSub)
        .toList();
  }

  private Predicate predicateFromParameters(String parameterName, String parameterValue) {
    LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add(parameterName, parameterValue);
    return predicateFromParameters(parameters);
  }

  private Predicate predicateFromParameters(LinkedMultiValueMap<String, String> parameters) {
    QuerydslBindings bindings = new QuerydslBindings();
    userRepository.customize(bindings, QUser.user);
    return new QuerydslPredicateBuilder(
            DefaultConversionService.getSharedInstance(), SimpleEntityPathResolver.INSTANCE)
        .getPredicate(TypeInformation.of(User.class), parameters, bindings);
  }

  /**
   * Persists the 4 prepared users and 4 more under alternating full names, all in the default group
   * holding the trainee role, then rewrites the first user's row so the table's physical order no
   * longer equals the id order.
   */
  private List<User> persistUsersWithTiedNamesInDefaultGroup() {
    List<User> allUsers = new ArrayList<>(List.of(user1, user2, user3, user4));
    for (int index = 5; index <= 8; index++) {
      allUsers.add(
          entityManager.persistAndFlush(
              new User("user" + index, "https://oidc.provider.cz/oidc/")));
    }
    for (int index = 0; index < allUsers.size(); index++) {
      User user = allUsers.get(index);
      user.setFullName(index % 2 == 0 ? "Alpha" : "Beta");
      group1.addUser(user);
    }
    entityManager.persistAndFlush(group1);
    entityManager.persistAndFlush(group2);
    user1.setMail("rewritten@oidc.provider.cz");
    entityManager.flush();
    entityManager.clear();
    return allUsers;
  }
}
