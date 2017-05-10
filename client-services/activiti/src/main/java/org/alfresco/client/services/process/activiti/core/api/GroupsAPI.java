package org.alfresco.client.services.process.activiti.core.api;

import static org.alfresco.client.services.process.activiti.common.constant.RequestConstant.*;
import static org.alfresco.client.services.process.activiti.core.ActivitiConstant.ACTIVITI_SERVICE_PATH;

import org.alfresco.client.services.process.activiti.common.model.representation.ResultList;
import org.alfresco.client.services.process.activiti.core.model.body.GroupRequest;
import org.alfresco.client.services.process.activiti.core.model.body.MembershipRequest;
import org.alfresco.client.services.process.activiti.core.model.representation.GroupResponse;
import org.alfresco.client.services.process.activiti.core.model.representation.MembershipResponse;

import retrofit2.Call;
import retrofit2.http.*;
import rx.Observable;

/**
 * Created by jpascal on 03/05/2017.
 */
public interface GroupsAPI
{

    /**
     * Create a group
     *
     * @param body (optional)
     * @return GroupResponse
     */
    @Headers({ "Content-type: application/json" })
    @POST(ACTIVITI_SERVICE_PATH + "/identity/groups")
    Call<GroupResponse> createGroupCall(@Body GroupRequest body);

    /**
     * Add a member to a group
     *
     * @param groupId The id of the group to add a member to. (required)
     * @param body (optional)
     * @return MembershipResponse
     */
    @Headers({ "Content-type: application/json" })
    @POST(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}/members")
    Call<MembershipResponse> createMembershipCall(@Path(GROUP_ID) String groupId, @Body MembershipRequest body);

    /**
     * Delete a group
     *
     * @param groupId The id of the group to delete. (required)
     */
    @DELETE(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}")
    Call<Void> deleteGroupCall(@Path(GROUP_ID) String groupId);

    /**
     * Delete a member from a group
     *
     * @param groupId The id of the group to remove a member from. (required)
     * @param userId The id of the user to remove. (required)
     */
    @DELETE(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}/members/{userId}")
    Call<Void> deleteMembershipCall(@Path(GROUP_ID) String groupId, @Path(USER_ID) String userId);

    /**
     * Get a single group
     *
     * @param groupId The id of the group to get. (required)
     * @return GroupResponse
     */
    @DELETE(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}")
    Call<GroupResponse> getGroupCall(@Path(GROUP_ID) String groupId);

    /**
     * Get a list of groups
     *
     * @param id Only return group with the given id (optional)
     * @param name Only return groups with the given name (optional)
     * @param type Only return groups with the given type (optional)
     * @param nameLike Only return groups with a name like the given value. Use
     *            % as wildcard-character. (optional)
     * @param member Only return groups which have a member with the given
     *            username. (optional)
     * @param potentialStarter Only return groups which members are potential
     *            starters for a process-definition with the given id.
     *            (optional)
     * @param sort Property to sort on, to be used together with the order.
     *            (optional)
     * @return DataResponse
     */
    Call<ResultList<GroupResponse>> getGroupsCall(@Query(ID) String id, @Query(NAME) String name,
            @Query(TYPE) String type, @Query(NAME_LIKE) String nameLike, @Query(MEMBER) String member,
            @Query(POTENTIAL_STARTER) String potentialStarter, @Query(SORT) String sort);

    /**
     * Update a group All request values are optional. For example, you can only
     * include the name attribute in the request body JSON-object, only updating
     * the name of the group, leaving all other fields unaffected. When an
     * attribute is explicitly included and is set to null, the group-value will
     * be updated to null.
     * 
     * @param groupId (required)
     * @param body (optional)
     * @return GroupResponse
     */
    @Headers({ "Content-type: application/json" })
    @POST(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}")
    Call<GroupResponse> updateGroupCall(@Path(GROUP_ID) String groupId, @Body GroupRequest body);

    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a group
     *
     * @param body (optional)
     * @return GroupResponse
     */
    @Headers({ "Content-type: application/json" })
    @POST(ACTIVITI_SERVICE_PATH + "/identity/groups")
    Observable<GroupResponse> createGroupObservable(@Body GroupRequest body);

    /**
     * Add a member to a group
     *
     * @param groupId The id of the group to add a member to. (required)
     * @param body (optional)
     * @return MembershipResponse
     */
    @Headers({ "Content-type: application/json" })
    @POST(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}/members")
    Observable<MembershipResponse> createMembershipObservable(@Path(GROUP_ID) String groupId,
            @Body MembershipRequest body);

    /**
     * Delete a group
     *
     * @param groupId The id of the group to delete. (required)
     */
    @DELETE(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}")
    Observable<Void> deleteGroupObservable(@Path(GROUP_ID) String groupId);

    /**
     * Delete a member from a group
     *
     * @param groupId The id of the group to remove a member from. (required)
     * @param userId The id of the user to remove. (required)
     */
    @DELETE(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}/members/{userId}")
    Observable<Void> deleteMembershipObservable(@Path(GROUP_ID) String groupId, @Path(USER_ID) String userId);

    /**
     * Get a single group
     *
     * @param groupId The id of the group to get. (required)
     * @return GroupResponse
     */
    @DELETE(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}")
    Observable<GroupResponse> getGroupObservable(@Path(GROUP_ID) String groupId);

    /**
     * Get a list of groups
     *
     * @param id Only return group with the given id (optional)
     * @param name Only return groups with the given name (optional)
     * @param type Only return groups with the given type (optional)
     * @param nameLike Only return groups with a name like the given value. Use
     *            % as wildcard-character. (optional)
     * @param member Only return groups which have a member with the given
     *            username. (optional)
     * @param potentialStarter Only return groups which members are potential
     *            starters for a process-definition with the given id.
     *            (optional)
     * @param sort Property to sort on, to be used together with the order.
     *            (optional)
     * @return DataResponse
     */
    Observable<ResultList<GroupResponse>> getGroupsObservable(@Query(ID) String id, @Query(NAME) String name,
            @Query(TYPE) String type, @Query(NAME_LIKE) String nameLike, @Query(MEMBER) String member,
            @Query(POTENTIAL_STARTER) String potentialStarter, @Query(SORT) String sort);

    /**
     * Update a group All request values are optional. For example, you can only
     * include the name attribute in the request body JSON-object, only updating
     * the name of the group, leaving all other fields unaffected. When an
     * attribute is explicitly included and is set to null, the group-value will
     * be updated to null.
     *
     * @param groupId (required)
     * @param body (optional)
     * @return GroupResponse
     */
    @Headers({ "Content-type: application/json" })
    @POST(ACTIVITI_SERVICE_PATH + "/identity/groups/{groupId}")
    Observable<GroupResponse> updateGroupObservable(@Path(GROUP_ID) String groupId, @Body GroupRequest body);

}
