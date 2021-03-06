package org.wso2.carbon.identity.saml.profile.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.saml.profile.query.dto.InvalidItemDTO;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLQueryRequestUtil;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLValidatorConstants;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.builders.SignKeyDataHolder;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

import java.util.List;


public class ResponseBuilder {

    final static Log log = LogFactory.getLog(SAMLQueryRequestUtil.class);

    /**
     * @param assertions
     * @param ssoIdPConfigs
     * @param userName
     * @return
     * @throws IdentityException
     */
    public static Response build(Assertion[] assertions, SAMLSSOServiceProviderDO ssoIdPConfigs, String userName) throws IdentityException {
        if (log.isDebugEnabled()) {
            log.debug("Building SAML Response for the consumer '");
        }
        Response response = new org.opensaml.saml2.core.impl.ResponseBuilder().buildObject();
        response.setIssuer(SAMLSSOUtil.getIssuer());
        response.setID(SAMLSSOUtil.createID());
        response.setStatus(buildStatus(SAMLSSOConstants.StatusCodes.SUCCESS_CODE,null));
        response.setVersion(SAMLVersion.VERSION_20);
        DateTime issueInstant = new DateTime();
        response.setIssueInstant(issueInstant);
        /**
         * adding assertions into array
         */
        for (Assertion assertion : assertions) {
            response.getAssertions().add(assertion);
        }

        //Sign on response message
        SAMLSSOUtil.setSignature(response, ssoIdPConfigs.getSigningAlgorithmUri(), ssoIdPConfigs
                .getDigestAlgorithmUri(), new SignKeyDataHolder(userName));

        return response;
    }

    public static Response build(List<InvalidItemDTO> invalidItem) throws IdentityException {

        Response response = new org.opensaml.saml2.core.impl.ResponseBuilder().buildObject();
        response.setIssuer(SAMLSSOUtil.getIssuer());
        response.setID(SAMLSSOUtil.createID());
        String statusCode ="";
        String statusMessage = "";

        //selecting Status Code
        if(invalidItem.size() > 0 ){
            statusMessage = invalidItem.get(0).getMessage();
            statusCode = invalidItem.get(0).getValidationtype();
            statusCode = filterStatusCode(statusCode);
        }
        response.setStatus(buildStatus(statusCode,statusMessage));
        response.setVersion(SAMLVersion.VERSION_20);
        DateTime issueInstant = new DateTime();
        response.setIssueInstant(issueInstant);

        //Sign on response message
       // SAMLSSOUtil.setSignature(response, XMLSignature.ALGO_ID_SIGNATURE_RSA, ssoIdPConfigs
       //         .getDigestAlgorithmUri());

        return response;
    }

    /**
     * Get status of message
     *
     * @param status
     * @param statMsg
     * @return Status object
     */
    public static Status buildStatus(String status, String statMsg) {

        Status stat = new StatusBuilder().buildObject();

        // Set the status code
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        statCode.setValue(status);
        stat.setStatusCode(statCode);

        // Set the status Message
        if (statMsg != null) {
            StatusMessage statMesssage = new StatusMessageBuilder().buildObject();
            statMesssage.setMessage(statMsg);
            stat.setStatusMessage(statMesssage);
        }

        return stat;
    }

    public static String filterStatusCode(String validationType){
        String statusCode;
        if(validationType.equalsIgnoreCase(SAMLValidatorConstants.ValidationType.VAL_VERSION)){
            statusCode = SAMLSSOConstants.StatusCodes.VERSION_MISMATCH;
        }
        else if(validationType.equalsIgnoreCase(SAMLValidatorConstants.ValidationType.VAL_ISSUER)){
           statusCode = SAMLSSOConstants.StatusCodes.UNKNOWN_PRINCIPAL;
        }
        else if(validationType.equalsIgnoreCase(SAMLValidatorConstants.ValidationType.VAL_SIGNATURE)){
            statusCode = SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR;
        }
        else if(validationType.equalsIgnoreCase(SAMLValidatorConstants.ValidationType.VAL_MESSAGE_TYPE)){
            statusCode = SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR;
        }
        else if(validationType.equalsIgnoreCase(SAMLValidatorConstants.ValidationType.VAL_MESSAGE_BODY)){
            statusCode = SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR;
        }
        else{
            statusCode = SAMLSSOConstants.StatusCodes.IDENTITY_PROVIDER_ERROR;
        }
    return statusCode;
    }


}
