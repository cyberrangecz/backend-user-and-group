/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz
 *
 *  Date      : 31.5.2016
 *
 *  (c) Copyright 2016 MASARYK UNIVERSITY
 *  All rights reserved.
 *
 *  This software is freely available for non-commercial use under license
 *  specified in following license agreement in LICENSE file. Please review the terms
 *  of the license agreement before using this software. If you are interested in
 *  using this software commercially orin ways not allowed in aforementioned
 *  license, feel free to contact Technology transfer office of the Masaryk university
 *  in order to negotiate ad-hoc license agreement.
 */
package cz.muni.ics.kypo.userandgroup.service.interfaces;

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDMGroupService {
    /**
     * Gets IDM group with given id from database.
     *
     * @param id of the IDM group to be loaded
     * @return IDM group with given id
     * @throws IdentityManagementException if group was not found
     */
    IDMGroup get(Long id) throws IdentityManagementException;

    /**
     * Creates given IDM group in database.
     *
     * @param group group to be created
     */
    IDMGroup create(IDMGroup group);

    /**
     * Updates given IDM group in database.
     *
     * @param group group to be updated
     */
    IDMGroup update(IDMGroup group);

    /**
     * Delete given IDM group from database and return status of deletion.
     * Return values: SUCCESS - succesfully deleted
     *                EXTERNAL_VALID - group is from external source and is not marked as DELETED
     *
     * @param group group to be deleted
     * @return status of deletion
     */
    GroupDeletionStatus delete(IDMGroup group);

    /**
     * Returns all IDM groups from database.
     *
     * @return IDM groups in database
     */
    List<IDMGroup> getAllIDMGroups();

    /**
     * Gets IDM group with given name from database.
     *
     * @param name of the IDM group to be loaded
     * @return IDM group with given name
     * @throws IdentityManagementException if group was not found
     */
    IDMGroup getIDMGroupByName(String name) throws IdentityManagementException;

    /**
     * Gets IDM groups with given name from database using like.
     *
     * @param name of the IDM group to be loaded (with '%' symbol)
     * @return IDM groups with given name
     * @throws IdentityManagementException
     */
    List<IDMGroup> getIDMGroupsByName(String name) throws IdentityManagementException;

    /**
     * Returns IDM group with assigned users from database
     *
     * @param id of the IDM group to be loaded
     * @return IDM group with assigned users in database
     * @throws IdentityManagementException if group was not found
     */
    IDMGroup getIDMGroupWithUsers(Long id) throws IdentityManagementException;

    /**
     * Returns IDM group with assigned users from database
     *
     * @param name of the IDM group to be loaded
     * @return IDM group with assigned users in database
     * @throws IdentityManagementException if group was not found
     */
    IDMGroup getIDMGroupWithUsers(String name) throws IdentityManagementException;

    /**
     * Returns true if group is internal otherwise false
     *
     * @param id of group
     * @return true if group is internal otherwise false
     * @throws IdentityManagementException if group was not found
     */
    boolean isGroupInternal(Long id) throws IdentityManagementException;

    /**
     * Deletes groups with given ids and returns statuses about their deletion.
     * Return statuses: SUCCESS - succesfully deleted
     *      EXTERNAL_VALID - group is from external source and is not marked as DELETED
     *      ERROR - group could not be deleted, try it later
     *      NOT_FOUND - group could not be found
     *
     * @param idsOfGroups ids of groups to be deleted
     * @return statuses about deletion of groups
     */
    Map<IDMGroup, GroupDeletionStatus> deleteGroups(List<Long> idsOfGroups);

    /**
     * Returns all roles of group with given id
     *
     * @param id of group.
     * @return all roles of group with given id
     */
    Set<Role> getRolesOfGroup(Long id);
}
