package cz.cyberrange.platform.userandgroup.service;

import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.UserRepository;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/** Provides information about the user authenticated for the current request. */
@Service
@TransactionalRO
public class SecurityService {

  private final UserRepository userRepository;
  private final IDMGroupRepository groupRepository;

  @Autowired
  public SecurityService(UserRepository userRepository, IDMGroupRepository groupRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
  }

  /**
   * Returns the user authenticated for the current request, matched by the sub and issuer carried
   * in its JWT.
   *
   * @return the authenticated user
   * @throws EntityNotFoundException when no user has that sub and issuer
   */
  public User getLoggedInUser() {
    String sub = getSubOfLoggedInUser();
    String iss = getIssOfLoggedInUser();
    Optional<User> optionalUser = userRepository.findBySubAndIss(sub, iss);
    return optionalUser.orElseThrow(
        () ->
            new EntityNotFoundException(
                new EntityErrorDetail(
                    User.class,
                    "sub",
                    sub.getClass(),
                    sub,
                    "Logged in user with sub " + sub + " could not be found in database.")));
  }

  private String getSubOfLoggedInUser() {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

  private String getIssOfLoggedInUser() {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    return authentication.getToken().getIssuer().toString();
  }

  /**
   * Checks whether the given sub matches the sub of the user authenticated for the current request.
   *
   * @param sub sub to compare
   * @return true when it matches, false otherwise
   */
  public boolean hasLoggedInUserSameLogin(String sub) {
    return sub.equals(getSubOfLoggedInUser());
  }

  /**
   * Checks whether the given id matches the id of the user authenticated for the current request.
   *
   * @param userId id to compare
   * @return true when it matches, false otherwise
   * @throws EntityNotFoundException when no user has the current sub and issuer
   */
  public boolean hasLoggedInUserSameId(Long userId) {
    User loggedInUser = getLoggedInUser();
    return userId.equals(loggedInUser.getId());
  }

  /**
   * Checks whether the user authenticated for the current request belongs to the group with the
   * given id.
   *
   * @param groupId id of the group to check
   * @return true when the user belongs to that group, false otherwise
   * @throws EntityNotFoundException when no user has the current sub and issuer
   * @throws javax.persistence.EntityNotFoundException when no group has that id
   */
  public boolean isLoggedInUserInGroup(Long groupId) {
    User loggedInUser = getLoggedInUser();
    IDMGroup group = groupRepository.getById(groupId);
    return group.getUsers().contains(loggedInUser);
  }

  /**
   * Checks whether the user authenticated for the current request belongs to the group with the
   * given name.
   *
   * @param groupName name of the group to check
   * @return true when the user belongs to that group, false otherwise
   * @throws EntityNotFoundException when no user has the current sub and issuer
   * @throws EntityNotFoundException when no group has that name
   */
  public boolean isLoggedInUserInGroup(String groupName) {
    User loggedInUser = getLoggedInUser();
    Optional<IDMGroup> optionalGroup = groupRepository.findByName(groupName);
    IDMGroup group =
        optionalGroup.orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        IDMGroup.class,
                        "name",
                        groupName.getClass(),
                        groupName,
                        "Group with name " + groupName + " could not be found.")));
    return group.getUsers().contains(loggedInUser);
  }

  /**
   * Checks whether the current request's JWT carries an authority equal to the given role type's
   * name.
   *
   * @param roleType role type to check
   * @return true when the authority is present, false otherwise
   */
  public boolean hasRole(RoleType roleType) {
    JwtAuthenticationToken authenticationToken =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    for (GrantedAuthority grantedAuthority : authenticationToken.getAuthorities()) {
      if (grantedAuthority.getAuthority().equals(roleType.name())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the current request's JWT carries the administrator or power user authority.
   *
   * @return true when either authority is present, false otherwise
   */
  public boolean canRetrieveAnyInformation() {
    return hasRole(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)
        || hasRole(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
  }
}
