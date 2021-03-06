/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.saml.profile.query.handler;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.saml.profile.query.internal.SAMLQueryServiceComponent;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation class of SAMLAttributeFinder
 */
public class UserStoreAttributeFinder implements SAMLAttributeFinder {
    public void init() {

    }

    /**
     * @param userName
     * @return
     */
    public Map<String, String> getAttributes(String userName) {
        return null;
    }


    /**
     * @param userName
     * @param attributes
     * @return Map of attribute name value pairs
     */
    public Map<String, String> getAttributes(String userName, String[] attributes) {

        //establish realmservice to access user store
        try {
            UserStoreManager userStoreManager = SAMLQueryServiceComponent.getRealmservice().
                    getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId()).
                    getUserStoreManager();
            //if not define filtering of user attributes
            if (attributes == null || attributes.length == 0) {
                /*
                attributes = SAMLQueryServiceComponent.getRealmservice().
                        getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId()).
                        getClaimManager().getAllClaimUris();
                        */
                List<String> list = new ArrayList<String>();
                ClaimMapping[] claimMappings = SAMLQueryServiceComponent.getRealmservice().getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId()).getClaimManager().getAllClaimMappings("http://wso2.org/claims");
                for(ClaimMapping claimMapping : claimMappings){
                    if(claimMapping.getClaim() != null && claimMapping.getClaim().getClaimUri() != null) {
                        list.add(claimMapping.getClaim().getClaimUri());
                    }
                }
                attributes =  list.toArray(new String[list.size()]);
            }

            return userStoreManager.getUserClaimValues(userName, attributes, null);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }

        return null;
    }
}
