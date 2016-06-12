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

import org.apache.ws.security.saml.SAMLIssuer;
import org.opensaml.SAMLAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectQuery;
import org.pdfbox.pdmodel.graphics.predictor.Sub;
import org.wso2.carbon.identity.saml.profile.query.handler.SAMLAttributeFinder;
import org.wso2.carbon.identity.saml.profile.query.handler.UserStoreAttributeFinder;

import java.util.*;

/**
 * Created by Gayan on 6/12/2016.
 */
public class SAMLSubjectQueryProcessor implements SAMLQueryProcessor {

    public SAMLAssertion[] process(RequestAbstractType request) {

        SubjectQuery query = (SubjectQuery) request;

        String issuer = getIssuer(query.getIssuer());

        String userName = getUserName(query.getSubject());

        Object issuerConfig = getIssuerConfig(issuer);

        Map<String, String> attributes = getUserAttributes(userName, null);

        build(userName, issuerConfig , attributes);

        return new SAMLAssertion[0];
    }

    protected  Object getIssuerConfig(String issuer) {

        return new Object();
    }


    protected Map<String,String> getUserAttributes(String userName, Set<String> attributes){

        List<SAMLAttributeFinder> finders = getAttributeFinders();

        for(SAMLAttributeFinder finder : finders) {
            Map<String,String> attributeMap = finder.getAttributes(userName, attributes);
            if(attributeMap != null && attributeMap.size() > 0) {
                return attributeMap;
            }
        }

        return new HashMap<String, String>();
    }

    protected SAMLAssertion build(String userName, Object issuer, Map<String, String> attributes) {

        return new SAMLAssertion();
    }

    protected String getIssuer(Issuer issuer) {

        return "";
    }

    protected String getUserName(Subject subject) {

        return "";
    }

    private List<SAMLAttributeFinder> getAttributeFinders() {

        return new ArrayList<SAMLAttributeFinder>();
    }
}
