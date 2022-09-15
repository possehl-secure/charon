/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.charon3.utils.usermanager;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.ConflictException;
import org.wso2.charon3.core.exceptions.NotFoundException;
import org.wso2.charon3.core.exceptions.NotImplementedException;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.objects.plainobjects.GroupsGetResponse;
import org.wso2.charon3.core.objects.plainobjects.UsersGetResponse;
import org.wso2.charon3.core.utils.CopyUtil;
import org.wso2.charon3.core.utils.codeutils.Node;
import org.wso2.charon3.core.utils.codeutils.SearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a sample dynamic user store.
 */
public class InMemoryUserManager implements UserManager {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserManager.class);
    //in memory user manager stores users
    ConcurrentHashMap<String, User> inMemoryUserList = new ConcurrentHashMap<String, User>();
    ConcurrentHashMap<String, Group> inMemoryGroupList = new ConcurrentHashMap<String, Group>();


    @Override
    public User createUser(User user, Map<String, Boolean> map)
            throws CharonException, ConflictException, BadRequestException {
        if (inMemoryUserList.get(user.getId()) != null) {
            throw new ConflictException("User with the id : " + user.getId() + "already exists");
        } else {
            inMemoryUserList.put(user.getId(), user);
            return (User) CopyUtil.deepCopy(user);
        }
    }

    @Override
    public User getUser(String id, Map<String, Boolean> map)
            throws CharonException, BadRequestException, NotFoundException {
       if (inMemoryUserList.get(id) != null) {
           return (User) CopyUtil.deepCopy(inMemoryUserList.get(id));
       } else {
           throw new NotFoundException("No user with the id : " + id);
       }
    }

    @Override
    public void deleteUser(String id)
            throws NotFoundException, CharonException, NotImplementedException, BadRequestException {
        if (inMemoryUserList.get(id) == null) {
            throw new NotFoundException("No user with the id : " + id);
        } else {
            inMemoryUserList.remove(id);
        }
    }

    @Override
    public UsersGetResponse listUsersWithGET(Node rootNode, int startIndex, int count, String sortBy,
                                         String sortOrder, String domainName, Map<String, Boolean> requiredAttributes)
            throws CharonException, NotImplementedException, BadRequestException {
        if (sortBy != null || sortOrder != null) {
            throw new NotImplementedException("Sorting is not supported");
        }  else if (startIndex != 1) {
            throw new NotImplementedException("Pagination is not supported");
        } else if (rootNode != null) {
            throw new NotImplementedException("Filtering is not supported");
        } else {
            return listUsers(requiredAttributes);
        }
    }

    private UsersGetResponse listUsers(Map<String, Boolean> requiredAttributes) throws CharonException {

        List<User> userList = new ArrayList<>();
        for (Map.Entry<String, User> entry : inMemoryUserList.entrySet()) {
            userList.add(entry.getValue());
        }
        return new UsersGetResponse(userList.size(), userList);
    }

    @Override
    public UsersGetResponse listUsersWithPost(SearchRequest searchRequest, Map<String, Boolean> requiredAttributes)
            throws CharonException, NotImplementedException, BadRequestException {

        return listUsersWithGET(searchRequest.getFilter(), searchRequest.getStartIndex(), searchRequest.getCount(),
                searchRequest.getSortBy(), searchRequest.getSortOder(), searchRequest.getDomainName(),
                requiredAttributes);
    }

    @Override
    public User updateUser(User user, Map<String, Boolean> map)
            throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
       if (user.getId() != null) {
           inMemoryUserList.replace(user.getId(), user);
           return (User) CopyUtil.deepCopy(user);
       } else {
           throw new NotFoundException("No user with the id : " + user.getId());
       }
    }

    public User updateUser(User user, Map<String, Boolean> requiredAttributes,
                           List<String> allSimpleMultiValuedAttributes)
            throws CharonException, BadRequestException, NotFoundException {

        if (StringUtils.isEmpty(user.getId())) {
            throw new NotFoundException("No user found. User id is empty.");
        }
        inMemoryUserList.replace(user.getId(), user);
        return (User) CopyUtil.deepCopy(user);
    }

    @Override
    public User getMe(String s, Map<String, Boolean> map)
            throws CharonException, BadRequestException, NotFoundException {
        return null;
    }

    @Override
    public User createMe(User user, Map<String, Boolean> map)
            throws CharonException, ConflictException, BadRequestException {
        return null;
    }

    @Override
    public void deleteMe(String s)
            throws NotFoundException, CharonException, NotImplementedException, BadRequestException {

    }

    @Override
    public User updateMe(User user, Map<String, Boolean> map)
            throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
        return null;
    }

    @Override
    public Group createGroup(Group group, Map<String, Boolean> map)
            throws CharonException, ConflictException, NotImplementedException, BadRequestException {
        inMemoryGroupList.put(group.getId(), group);
        return (Group) CopyUtil.deepCopy(group);
    }

    @Override
    public Group getGroup(String id, Map<String, Boolean> map)
            throws NotImplementedException, BadRequestException, CharonException, NotFoundException {
        if (inMemoryGroupList.get(id) != null) {
            return (Group) CopyUtil.deepCopy(inMemoryGroupList.get(id));
        } else {
            throw new NotFoundException("No user with the id : " + id);
        }
    }

    @Override
    public void deleteGroup(String id)
            throws NotFoundException, CharonException, NotImplementedException, BadRequestException {
        if (inMemoryGroupList.get(id) == null) {
            throw new NotFoundException("No user with the id : " + id);
        } else {
            inMemoryGroupList.remove(id);
        }
    }

    @Override
    public GroupsGetResponse listGroupsWithGET(Node rootNode, int startIndex, int count, String sortBy,
                             String sortOrder, String domainName, Map<String, Boolean> requiredAttributes)
            throws CharonException, NotImplementedException, BadRequestException {
        if (sortBy != null || sortOrder != null) {
            throw new NotImplementedException("Sorting is not supported");
        }  else if (startIndex != 1) {
            throw new NotImplementedException("Pagination is not supported");
        } else if (rootNode != null) {
            throw new NotImplementedException("Filtering is not supported");
        } else {
            return listGroups(requiredAttributes);
        }
    }

    private GroupsGetResponse listGroups(Map<String, Boolean> requiredAttributes) {
        List<Group> groupList = new ArrayList<>();
        for (Group group : inMemoryGroupList.values()) {
            groupList.add(group);
        }
        return new GroupsGetResponse(groupList.size(), groupList);
    }

    @Override
    public Group updateGroup(Group group, Group group1, Map<String, Boolean> map)
            throws NotImplementedException, BadRequestException, CharonException, NotFoundException {
        if (group.getId() != null) {
            inMemoryGroupList.replace(group.getId(), group);
            return (Group) CopyUtil.deepCopy(group);
        } else {
            throw new NotFoundException("No user with the id : " + group.getId());
        }
    }

    @Override
    public GroupsGetResponse listGroupsWithPost(SearchRequest searchRequest, Map<String, Boolean> requiredAttributes)
            throws NotImplementedException, BadRequestException, CharonException {

        return listGroupsWithGET(searchRequest.getFilter(), searchRequest.getStartIndex(),
                searchRequest.getCount(), searchRequest.getSortBy(), searchRequest.getSortOder(),
                searchRequest.getDomainName(), requiredAttributes);
    }
}
