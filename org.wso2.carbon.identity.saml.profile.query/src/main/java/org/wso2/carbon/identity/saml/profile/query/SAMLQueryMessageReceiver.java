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

package org.wso2.carbon.identity.saml.profile.query;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.transport.TransportUtils;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.saml.profile.query.processor.SAMLProcessorFactory;
import org.wso2.carbon.identity.saml.profile.query.processor.SAMLQueryProcessor;
import org.wso2.carbon.identity.saml.profile.query.util.SAMLQueryRequestUtil;
import org.wso2.carbon.identity.saml.profile.query.validation.SAMLQueryValidator;
import org.wso2.carbon.identity.saml.profile.query.validation.SAMLValidatorFactory;

import javax.xml.stream.XMLStreamException;

/**
 * Axis2 Message receiver for SAML Query
 */
public class SAMLQueryMessageReceiver extends AbstractInOutMessageReceiver {
    OMElement queryOM = null;
    boolean isValidMessage = false;


    @Override
    public void invokeBusinessLogic(MessageContext inMessageContext, MessageContext outMessageContext) throws AxisFault {

        if (inMessageContext.getEnvelope().getBody() != null) {
            queryOM = inMessageContext.getEnvelope().getBody().getFirstElement();

            System.out.println("SAMLQueryMessageReceiver Executed!!!!!!  ");
            System.out.println(queryOM.toString());
            RequestAbstractType request = ((RequestAbstractType) SAMLQueryRequestUtil.unmarshall(queryOM.toString()));
            if (request == null) {
                log.error("No way to proceed .request is empty");
                return;
            } else {

                SAMLQueryValidator validator = SAMLValidatorFactory.getValidator(request);
                isValidMessage = validator.validate(request);
                if (isValidMessage) {
                    log.info("request message is validated completely");

                    //Process Request message
                    SAMLQueryProcessor processor = SAMLProcessorFactory.getProcessor(request);
                    Response response = processor.process(request);
                    try {
                        String nonEncodedResponse = SAMLQueryRequestUtil.marshall((response));


                        /////////////
//                        SOAPEnvelope env1 = SAMLQueryRequestUtil.createSOAPEnvelope(inMessageContext.getEnvelope().
//                                getNamespace().getNamespaceURI());
                        OMElement myOMElement = null;
                        try {
                            myOMElement = AXIOMUtil.stringToOM(nonEncodedResponse);
                        } catch (XMLStreamException e) {
                            e.printStackTrace();
                        }
                        SOAPEnvelope soapEnvelope = TransportUtils.createSOAPEnvelope(myOMElement);


                        outMessageContext.setEnvelope(soapEnvelope);


                        ///////////

                        log.info("Response created....... :   " + nonEncodedResponse);

                    } catch (IdentityException e) {
                        e.printStackTrace();
                    }


                } else {
                    log.info("Request message contain validation issues!");
                }
            }
        } else {

            log.info("SOAP message body is empty");
        }


//		try {
//			Claim[] claims = SAMLQueryServiceComponent.getRealmservice().getTenantUserRealm(-1234).
//                    getUserStoreManager().getUserClaimValues("gayan", "default");
//		} catch (UserStoreException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println("User claims =======================");


//		request.getIssuer();
//
//		request.getSignature();
//
//		if (request instanceof AttributeQueryImpl) {
//			request = (AttributeQueryImpl) request;
//			request.get
//		}

    }


}
