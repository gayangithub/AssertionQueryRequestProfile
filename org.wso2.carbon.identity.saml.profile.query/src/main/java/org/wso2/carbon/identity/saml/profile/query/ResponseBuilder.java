package org.wso2.carbon.identity.saml.profile.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLQueryRequestUtil;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.builders.SignKeyDataHolder;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;


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
        response.setStatus(buildStatus(SAMLSSOConstants.StatusCodes.SUCCESS_CODE, null));
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
}
