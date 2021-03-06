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
import org.wso2.carbon.identity.saml.profile.query.dto.InvalidItemDTO;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLQueryRequestUtil;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLValidatorConstants;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

import java.util.List;

public class AbstractSAMLQueryValidator implements SAMLQueryValidator {

    private final static Log log = LogFactory.getLog(AbstractSAMLQueryValidator.class);


    private SAMLSSOServiceProviderDO ssoIdpConfig = null;
    private String IssuerName = null;
    private String IssuerSPProvidedID = null;
    private String alias = null;
    private String domainName = null;
    private boolean isValidSig = true;

    public AbstractSAMLQueryValidator() {

    }



    public boolean validate(List<InvalidItemDTO> invalidItems, RequestAbstractType request) {

        //status of validation
        boolean isIssuerValidated = false;
        boolean isSignatureValidated = false;
        boolean isValidSAMLVersion = false;

        try {
            //validate SAML Request vertion
            isValidSAMLVersion = this.validateSAMLVersion(request);

            if (isValidSAMLVersion) {
                //validate Issuer of Request
                isIssuerValidated = this.validateIssuer(request);
            } else {
                //invalid SAML version
                invalidItems.add(new InvalidItemDTO(SAMLValidatorConstants.ValidationType.VAL_VERSION,
                        SAMLValidatorConstants.ValidationMessage.VAL_VERSION_ERROR));
                log.error(SAMLValidatorConstants.ValidationMessage.VAL_VERSION_ERROR);
                return false;

            }

            if (isIssuerValidated) {
                //validate Signature of Request
                isSignatureValidated = this.validateSignature(request);
            } else {
                //invalid issuer
                invalidItems.add(new InvalidItemDTO(SAMLValidatorConstants.ValidationType.VAL_ISSUER,
                        SAMLValidatorConstants.ValidationMessage.VAL_ISSUER_ERROR));
                log.error(SAMLValidatorConstants.ValidationMessage.VAL_ISSUER_ERROR);
                return false;
            }
            if (!isSignatureValidated) {
                //invalid signature
                invalidItems.add(new InvalidItemDTO(SAMLValidatorConstants.ValidationType.VAL_SIGNATURE,
                        SAMLValidatorConstants.ValidationMessage.VAL_SIGNATURE_ERROR));
                log.error(SAMLValidatorConstants.ValidationMessage.VAL_ISSUER_ERROR);
            }
        } catch (IdentityException ex) {
            log.error(SAMLValidatorConstants.ServiceMessages.SERVER_ERROR_PROCESSING_ISSUER_SIG_VERSION);
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
            log.error(SAMLValidatorConstants.ServiceMessages.SIGNATURE_VALIDATION_FAILED);
            log.error(ex.getMessage());
        }
        return isValidSig;
    }

    protected boolean validateIssuer(RequestAbstractType request) throws IdentityException {
        Issuer issuer = request.getIssuer();
        boolean validIssuer = false;
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
                            log.error(SAMLValidatorConstants.ServiceMessages.NULL_ISSUER);
                            return validIssuer;
                        } else {
                            log.info(SAMLValidatorConstants.ServiceMessages.SUCCESS_ISSUER + ssoIdpConfig.getIssuer());
                            validIssuer = true;
                        }
                    } catch (IdentityException e) {
                        log.error(SAMLValidatorConstants.ServiceMessages.ISSUER_VALIDATION_FAILED);
                        log.error(e.getMessage());
                    }

                } else {

                    log.error(SAMLValidatorConstants.ServiceMessages.NO_ISSUER_PRESENTED);
                    throw IdentityException.error(
                            SAMLValidatorConstants.ValidationMessage.EXIT_WITH_ERROR);
                }

            }

        }


        return validIssuer;
    }

    protected boolean validateSAMLVersion(RequestAbstractType request) throws IdentityException {
        boolean isValidversion = false;
        if (request.getVersion().equals(SAMLVersion.VERSION_20)) {
            isValidversion = true;
        } else {
            log.error(SAMLValidatorConstants.ServiceMessages.NON_COMPAT_SAML_VERSION);
            isValidversion = false;
            throw IdentityException.error(SAMLValidatorConstants.ValidationMessage.EXIT_WITH_ERROR);

        }
        return isValidversion;
    }

    public SAMLSSOServiceProviderDO getSsoIdpConfig() {
        return ssoIdpConfig;
    }


}
