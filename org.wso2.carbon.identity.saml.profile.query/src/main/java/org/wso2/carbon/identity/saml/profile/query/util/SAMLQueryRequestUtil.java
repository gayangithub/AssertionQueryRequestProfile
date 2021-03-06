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

package org.wso2.carbon.identity.saml.profile.query.util;

import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.persistence.IdentityPersistenceManager;
import org.wso2.carbon.identity.saml.profile.query.dto.InvalidItemDTO;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.SSOServiceProviderConfigManager;
import org.wso2.carbon.identity.sso.saml.builders.SignKeyDataHolder;
import org.wso2.carbon.identity.sso.saml.util.CarbonEntityResolver;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SAMLQueryRequestUtil {

    final static Log log = LogFactory.getLog(SAMLQueryRequestUtil.class);
    private static boolean isBootStrapped = false;

    /**
     * convert xml string into DOM object
     *
     * @param xmlString
     * @return XMLObject
     */
    public static XMLObject unmarshall(List<InvalidItemDTO> invalidItems,String xmlString) {
        InputStream inputStream = null;
        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new CarbonEntityResolver());
            inputStream = new ByteArrayInputStream(xmlString.trim().getBytes(StandardCharsets.UTF_8));
            Document document = docBuilder.parse(inputStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            invalidItems.add(new InvalidItemDTO(SAMLValidatorConstants.ValidationType.VAL_UNMARSHAL,
                    SAMLValidatorConstants.ValidationMessage.VAL_UNMARSHAL_FAIL));
            log.error(SAMLValidatorConstants.ValidationMessage.VAL_UNMARSHAL_FAIL, e);
        }

        return null;
    }

    /**
     * Bootstrap the OpenSAML2 library only if it is not bootstrapped
     */
    public static void doBootstrap() {

        if (!isBootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                isBootStrapped = true;
                log.info("[][]Sucesfully Bosstrapped.");
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    /**
     * Load Service Provider Configurations
     *
     * @param issuer
     * @return SAMLSSOServiceProviderDO
     * @throws IdentityException
     */
    public static SAMLSSOServiceProviderDO getServiceProviderConfig(String issuer)
            throws IdentityException {
        try {
            SSOServiceProviderConfigManager idPConfigManager =
                    SSOServiceProviderConfigManager.getInstance();
            SAMLSSOServiceProviderDO ssoIdpConfigs = idPConfigManager.getServiceProvider(issuer);
            if (ssoIdpConfigs == null) {
                IdentityPersistenceManager persistenceManager =
                        IdentityPersistenceManager.getPersistanceManager();
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                UserRegistry registry =
                        SAMLSSOUtil.getRegistryService()
                                .getConfigSystemRegistry(tenantId);
                ssoIdpConfigs = persistenceManager.getServiceProvider(registry, issuer);
            }
            return ssoIdpConfigs;
        } catch (Exception e) {
            throw IdentityException.error(
                    SAMLValidatorConstants.ValidationMessage.ERROR_LOADING_SP_CONF,
                    e);
        }
    }

    /**
     * Build SAML assertion
     *
     * @param ssoIdPConfigs
     * @param userName
     * @return Assertion object
     * @throws IdentityException
     */
    public static Assertion buildSAMLAssertion(String userName, Map<String, String> claims,
                                               SAMLSSOServiceProviderDO ssoIdPConfigs)
            throws IdentityException {

        DateTime currentTime = new DateTime();

        DateTime notOnOrAfter =
                new DateTime(currentTime.getMillis() +
                        (long) SAMLSSOUtil.getSAMLResponseValidityPeriod() * 60 *
                                1000);

        Assertion samlAssertion = new AssertionBuilder().buildObject();
        samlAssertion.setID(SAMLSSOUtil.createID());
        samlAssertion.setVersion(SAMLVersion.VERSION_20);
        samlAssertion.setIssuer(SAMLSSOUtil.getIssuer());
        samlAssertion.setIssueInstant(currentTime);
        Subject subject = new SubjectBuilder().buildObject();
        NameID nameId = new NameIDBuilder().buildObject();
        String claimValue = null;

        if (claimValue == null) {
            nameId.setValue(userName);
        }

        if (ssoIdPConfigs.getNameIDFormat() != null) {
            nameId.setFormat(ssoIdPConfigs.getNameIDFormat());
        } else {
            nameId.setFormat(NameIdentifier.EMAIL);
        }

        subject.setNameID(nameId);

        SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder().buildObject();
        subjectConfirmation.setMethod(SAMLSSOConstants.SUBJECT_CONFIRM_BEARER);

        SubjectConfirmationData subjectConfirmationData =
                new SubjectConfirmationDataBuilder().buildObject();
        subjectConfirmationData.setRecipient(ssoIdPConfigs.getAssertionConsumerUrl());
        subjectConfirmationData.setNotOnOrAfter(notOnOrAfter);

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        samlAssertion.setSubject(subject);

        AuthnStatement authStmt = new AuthnStatementBuilder().buildObject();
        authStmt.setAuthnInstant(new DateTime());

        AuthnContext authContext = new AuthnContextBuilder().buildObject();
        AuthnContextClassRef authCtxClassRef = new AuthnContextClassRefBuilder().buildObject();
        authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authContext.setAuthnContextClassRef(authCtxClassRef);
        authStmt.setAuthnContext(authContext);
        samlAssertion.getAuthnStatements().add(authStmt);

        if (claims != null) {
            samlAssertion.getAttributeStatements().add(buildAttributeStatement(claims));
        }

        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience issuerAudience = new AudienceBuilder().buildObject();
        issuerAudience.setAudienceURI(ssoIdPConfigs.getIssuer());
        audienceRestriction.getAudiences().add(issuerAudience);
        if (ssoIdPConfigs.getRequestedAudiences() != null) {
            for (String requestedAudience : ssoIdPConfigs.getRequestedAudiences()) {
                Audience audience = new AudienceBuilder().buildObject();
                audience.setAudienceURI(requestedAudience);
                audienceRestriction.getAudiences().add(audience);
            }
        }

        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(currentTime);
        conditions.setNotOnOrAfter(notOnOrAfter);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        samlAssertion.setConditions(conditions);

        if (ssoIdPConfigs.isDoSignAssertions()) {
            SAMLSSOUtil.setSignature(samlAssertion, ssoIdPConfigs.getSigningAlgorithmUri(), ssoIdPConfigs
                    .getDigestAlgorithmUri(), new SignKeyDataHolder(userName));
        }

        return samlAssertion;
    }

    /**
     * Build Attribute Statement
     *
     * @param claims
     * @return AttributeStatement
     */

    public static AttributeStatement buildAttributeStatement(Map<String, String> claims) {
        AttributeStatement attStmt = null;
        if (claims != null) {
            attStmt = new AttributeStatementBuilder().buildObject();
            Iterator<String> ite = claims.keySet().iterator();

            for (int i = 0; i < claims.size(); i++) {
                Attribute attrib = new AttributeBuilder().buildObject();
                String claimUri = ite.next();
                attrib.setName(claimUri);

                XSStringBuilder stringBuilder =
                        (XSStringBuilder) Configuration.getBuilderFactory()
                                .getBuilder(XSString.TYPE_NAME);
                XSString stringValue =
                        stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                                XSString.TYPE_NAME);
                stringValue.setValue(claims.get(claimUri));
                attrib.getAttributeValues().add(stringValue);
                attStmt.getAttributes().add(attrib);
            }
        }
        return attStmt;
    }

    /**
     * Serialize the Auth. Request
     *
     * @param xmlObject
     * @return serialized auth. req
     */
    public static String marshall(XMLObject xmlObject) throws IdentityException {

        ByteArrayOutputStream byteArrayOutputStrm = null;
        try {
            doBootstrap();
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);
            byteArrayOutputStrm = new ByteArrayOutputStream();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(byteArrayOutputStrm);
            writer.write(element, output);
            return byteArrayOutputStrm.toString("UTF-8");
        } catch (Exception e) {
            log.error("Error Serializing the SAML Response");
            throw IdentityException.error("Error Serializing the SAML Response", e);
        } finally {
            if (byteArrayOutputStrm != null) {
                try {
                    byteArrayOutputStrm.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }
        }
    }

    /**
     * create soap envelop accrding tgo SOAP version
     *
     * @param nsUri
     * @return
     */
    public static SOAPEnvelope createSOAPEnvelope(String nsUri) {
        return nsUri != null && "http://schemas.xmlsoap.org/soap/envelope/".equals(nsUri) ?
                DOOMAbstractFactory.getSOAP11Factory().getDefaultEnvelope() :
                DOOMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
    }
}
