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

package org.wso2.carbon.identity.saml.profile.query.processor;

import org.opensaml.saml2.core.*;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.saml.profile.query.ResponseBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SAMLAttributeQueryProcessor extends SAMLSubjectQueryProcessor {

    @Override
    public Response process(RequestAbstractType request) {
        AttributeQuery query = (AttributeQuery) request;
        String issuer = getIssuer(query.getIssuer());
        String userName = getUserName(query.getSubject());
        Object issuerConfig = getIssuerConfig(issuer);
        List<Attribute> requestedattributes = query.getAttributes();
        String claimAttributes[] = getAttributesAsArray(requestedattributes);
        //pass filtered attribute list below
        Map<String, String> attributes = getUserAttributes(userName,claimAttributes, issuerConfig);
        Assertion assertion = build(userName, issuerConfig, attributes);
        Assertion[] assertions = {assertion};
        Response response = null;

        try {
            response = ResponseBuilder.build(assertions, (SAMLSSOServiceProviderDO) issuerConfig, userName);
            log.info("SAMLAttributeQueryProcessor : response generated");
        } catch (IdentityException e) {
            log.error("error occurred ",e);
        }

        return response;
    }

    /**
     *
     * @param claimattributes
     * @return
     */
    private String[] getAttributesAsArray(List<Attribute> claimattributes){
        List<String> list = new ArrayList<String>();
        String[] claimArray = null;



        if(claimattributes.size() > 0){

            for(Attribute attribute : claimattributes){
                if(attribute.getFriendlyName() != null) {
                    list.add(attribute.getFriendlyName());

                }
            }
            claimArray =  list.toArray(new String[list.size()]);
            return  claimArray;
        }

     return  claimArray;
    }



}
