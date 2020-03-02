package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * The interface for IDMGroup service layer.
 */
public interface IDMGroupService {
    /**
     * Gets IDMGroup with the given ID.
     *
     * @param id The ID of the IDMGroup to be loaded.
     * @return the {@link IDMGroup} with the given ID.
     * @throws EntityNotFoundException if a group was not found.
     */
    IDMGroup getGroupById(Long id) throws EntityNotFoundException;

    /**
     * Gets list of IDMGroups based on the given groupIds
     *
     * @param groupIds the list of IDs.
     * @return the list of {@link IDMGroup} with the given IDs.
     */
    List<IDMGroup> getGroupsByIds(List<Long> groupIds);

    /**
     * Creates new IDMGroup.
     *
     * @param group                     IDMGroup to be created.
     * @param groupIdsOfImportedMembers all {@link cz.muni.ics.kypo.userandgroup.model.User}s from groups with given IDs will be imported to new group.
     * @return created {@link IDMGroup}.
     * @throws EntityNotFoundException if some of the groups with given IDs could not be found.
     */
    IDMGroup createIDMGroup(IDMGroup group, List<Long> groupIdsOfImportedMembers) throws EntityNotFoundException;

    /**
     * Update given IDMGroup.
     *
     * @param group IDMGroup to be updated.
     * @return the {@link IDMGroup} with  updated fields.
     * @throws EntityConflictException if the group with the given ID is main and its name is trying to be changed.
     */
    IDMGroup updateIDMGroup(IDMGroup group) throws EntityConflictException;

    /**
     * Delete given IDMGroup from the database and return status of the deletion.
     *
     * @param group the group to be deleted.
     */
    void deleteIDMGroup(IDMGroup group);

    /**
     * Returns all IDMGroups from the database.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return list of all {@link IDMGroup}s from database wrapped up in {@link Page}.
     */
    Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable);

    /**
     * Gets the IDMGroup with the given name.
     *
     * @param name the name of the IDMGroup to be loaded.
     * @return the {@link IDMGroup} with the given name
     * @throws EntityNotFoundException if the group was not found.
     */
    IDMGroup getIDMGroupByName(String name);

    /**
     * Gets the IDMGroup based on the given name including group roles.
     *
     * @param name the name of the IDMGroup to be loaded.
     * @return the {@link IDMGroup} with the given name
     * @throws EntityNotFoundException if the group was not found.
     */
    IDMGroup getIDMGroupWithRolesByName(String name) throws EntityNotFoundException;

    /**
     * Returns all roles of IDMGroup with given ID.
     *
     * @param id ID of the IDMGroup.
     * @return all roles of {@link IDMGroup} with the given ID.
     * @throws EntityNotFoundException if the group was not found.
     */
    Set<Role> getRolesOfGroup(Long id) throws EntityNotFoundException;

    /**
     * Assigns the role to the IDMGroup with the given ID. All {@link cz.muni.ics.kypo.userandgroup.model.User}s in the group
     * will obtain an assigned role.
     *
     * @param groupId the ID of the IDMGroup which will getGroupById the role with the given role ID.
     * @param roleId  the ID of the role to be assigned to the IDMGroup.
     * @return the {@link IDMGroup} with the assigned role with the given role ID.
     * @throws EntityNotFoundException if the IDMGroup or role with a given ID could not be found.
     */
    IDMGroup assignRole(Long groupId, Long roleId) throws EntityNotFoundException;

    /**
     * Removes role from IDMGroup with the given ID. All {@link cz.muni.ics.kypo.userandgroup.model.User}s in the group
     * will lose this role if they do not take on this role from another group.
     *
     * @param groupId the ID of the IDMGroup from which role with role ID is removed.
     * @param roleId  the ID of the role.
     * @return the {@link IDMGroup} with the removed role.
     * @throws EntityNotFoundException        if the IDMGroup or the role could not be found.
     * @throws EntityConflictException if the role <i>GUEST, USER, ADMINISTRATOR<i/> is removed from the IDMGroup with name  <i>DEFAULT-GROUP,
     *                                             USER-AND-GROUP_USER, USER-AND-GROUP_ADMINISTRATOR<i/>.
     */
    IDMGroup removeRoleFromGroup(Long groupId, Long roleId);

    /**
     *
     * Removes particular user from a group.
     *
     * @param groupToUpdate group to be updated
     * @param user          user to be removed from a group
     * @throws EntityConflictException if administrator wants to remove himself from a group.
     */
    void removeUserFromGroup(IDMGroup groupToUpdate, User user);

    /**
     * Add particular user to a group.
     *
     * @param groupToUpdate group where the user will be added.
     * @param userToBeAdded user that will be added to the particular group.
     * @return updated group
     */
    IDMGroup addUserToGroup(IDMGroup groupToUpdate, User userToBeAdded);

    /**
     * Gets IDMGroup for default roles from the database.
     *
     * @return {@link IDMGroup} for default roles - <i>DEFAULT-GROUP<i/>.
     * @throws EntityNotFoundException if the IDMGroup was not found.
     */
    IDMGroup getGroupForDefaultRoles();


    /**
     * Evict user from cache
     *
     * @param user user to be evicted from cache.
     */
    void evictUserFromCache(User user);
}
