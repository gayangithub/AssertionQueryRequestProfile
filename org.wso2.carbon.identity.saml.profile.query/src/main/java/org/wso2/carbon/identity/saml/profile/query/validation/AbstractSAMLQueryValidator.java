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
import org.opensaml.saml2.core.RequestAbstractType;

/**
 * Created by Gayan on 6/11/2016.
 */
public class AbstractSAMLQueryValidator implements SAMLQueryValidator {

    private final static Log log = LogFactory.getLog(AbstractSAMLQueryValidator.class);

    public boolean validate(RequestAbstractType request) {

        //status of validation
        boolean isIssuerValidated = false;
        boolean isSignatureValidated = false;
        //validate issuer of request message
        isIssuerValidated = this.validateIssuer(request);

        //validate signature of request message
        isSignatureValidated= this.validateSignature(request);

        if(isIssuerValidated && isSignatureValidated)
            return true;
        else
            return false;
    }


    protected boolean validateSignature(RequestAbstractType request){

        return false;
    }

    protected boolean validateIssuer(RequestAbstractType request){

        return false;
    }
}
