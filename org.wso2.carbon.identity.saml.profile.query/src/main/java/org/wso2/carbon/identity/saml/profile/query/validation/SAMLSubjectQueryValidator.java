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

package org.wso2.carbon.identity.saml.profile.query.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.SAMLSubject;
import org.opensaml.SAMLSubjectQuery;
import org.opensaml.saml2.core.RequestAbstractType;


/**
 *
 */
public class SAMLSubjectQueryValidator extends AbstractSAMLQueryValidator {

    private final static Log log = LogFactory.getLog(SAMLSubjectQueryValidator.class);
   SAMLSubject subject = null;

    @Override
    public boolean validate(RequestAbstractType request) {
        boolean isSubjectValid = this.validateSubject((SAMLSubjectQuery) request);

        return super.validate(request) && isSubjectValid;
    }

    protected boolean validateSubject(SAMLSubjectQuery request) {
        subject = request.getSubject();
        boolean isValidsubject = false;
        // Validating SubjectID format
        if (subject != null && subject.getNameIdentifier() != null &&
                subject.getNameIdentifier().getFormat() != null && super.getSsoIdpConfig().getNameIDFormat() != null &&
                subject.getNameIdentifier().getFormat().equals(super.getSsoIdpConfig().getNameIDFormat())) {
            log.info("Request subject is valid");
            isValidsubject = true;
        }


        return isValidsubject;
    }
}
