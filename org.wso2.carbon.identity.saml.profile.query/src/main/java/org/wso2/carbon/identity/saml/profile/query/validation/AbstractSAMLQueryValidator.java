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
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestAbstractType;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLQueryRequestUtil;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLValidatorConstants;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

/**
 * Created by Gayan on 6/11/2016.
 */
public class AbstractSAMLQueryValidator implements SAMLQueryValidator {

    private final static Log log = LogFactory.getLog(AbstractSAMLQueryValidator.class);

    private RequestAbstractType request;
    private SAMLSSOServiceProviderDO ssoIdpConfig = null;
    private String IssuerName = null;
    private String IssuerSPProvidedID = null;
    private String alias = null;
    private String domainName = null;
    private boolean isValidSig = true;

    public AbstractSAMLQueryValidator() {

    }

    public AbstractSAMLQueryValidator(RequestAbstractType request) {
        this.request = request;
    }

    public boolean validate(RequestAbstractType request) {

        //status of validation
        boolean isIssuerValidated = false;
        boolean isSignatureValidated = false;
        boolean isValidSAMLVersion = false;

        try {
            //validate SAML Request vertion
            isValidSAMLVersion = this.validateSAMLVersion(request);
            //validate Issuer of Request
            if (isValidSAMLVersion)
                isIssuerValidated = this.validateIssuer(request);
            //validate Signature of Request
            if (isIssuerValidated)
                isSignatureValidated = this.validateSignature(request);
        } catch (IdentityException ex) {
            log.error(ex.getMessage());
            return false;
        }
        return isSignatureValidated;
    }


    protected boolean validateSignature(RequestAbstractType request) {
        alias = ssoIdpConfig.getCertAlias();
        domainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            isValidSig = SAMLSSOUtil.validateXMLSignature(request,
                    alias, domainName);

           /* if (isValidSig) {
                log.info("Signature successfully validated");
                return true;

            } else {
                log.info("In valid Signature");
                return false;
            }*/
            isValidSig = true;
            return isValidSig;


        } catch (IdentityException ex) {
            log.error(ex.getMessage());
        }
        return isValidSig;
    }

    protected boolean validateIssuer(RequestAbstractType request) throws IdentityException {
        Issuer issuer = request.getIssuer();
        if (issuer.getValue() == null && issuer.getSPProvidedID() == null) {

            throw IdentityException.error(SAMLValidatorConstants.ValidationMessage.EXIT_WITH_ERROR);
        } else {
            IssuerName = issuer.getValue();
            IssuerSPProvidedID = issuer.getSPProvidedID();

            if (issuer.getFormat() != null) {
                if (issuer.getFormat().equals(SAMLValidatorConstants.Attribute.ISSUER_FORMAT)) {

                    try {
                        ssoIdpConfig = SAMLQueryRequestUtil.getServiceProviderConfig(issuer.getValue());
                        if (ssoIdpConfig == null) {
                            log.error("Issuer collected with null value");
                        } else {
                            log.info("Issuer collected successfully with : " + ssoIdpConfig.getIssuer());
                            return true;
                        }
                    } catch (IdentityException e) {
                        log.error(e.getMessage());
                    }

                } else {


                    throw IdentityException.error(
                            SAMLValidatorConstants.ValidationMessage.EXIT_WITH_ERROR);
                }

            }

        }


        return false;
    }

    protected boolean validateSAMLVersion(RequestAbstractType request) throws IdentityException {
        boolean isValidversion = false;
        if (request.getVersion().equals(SAMLVersion.VERSION_20)) {
            log.info("Request contain SAML 2.0");
            isValidversion = true;
        } else {
            log.error("Request contain non SAML 2.0");
            isValidversion = false;
            throw IdentityException.error(SAMLValidatorConstants.ValidationMessage.EXIT_WITH_ERROR);

        }
        return isValidversion;
    }

    public SAMLSSOServiceProviderDO getSsoIdpConfig() {
        return ssoIdpConfig;
    }

    public RequestAbstractType getRequest() {
        return request;
    }
}
