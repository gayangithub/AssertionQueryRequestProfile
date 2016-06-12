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

package org.wso2.carbon.identity.saml.profile.query.internal;

import org.apache.commons.logging.*;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="identity.saml.query" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */

public class SAMLQueryServiceComponent {

	private static Log log = LogFactory.getLog(SAMLQueryServiceComponent.class);

	private static RealmService realmservice = null;

	protected void activate(ComponentContext ctxt) {

		System.out.println("Component is activated");
		log.info("SAMLQueryService Message: Bundle activated********************************");

	}

	protected void deactivate(ComponentContext ctxt) {

    }



	/**
	 * sets realm service
	 *
	 * @param realmService <code>RealmService</code>
	 */
	protected void setRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("DefaultUserRealm set in to bundle");
		}
		this.realmservice = realmService;
	}

	/**
	 * un-sets realm service
	 *
	 * @param realmService <code>RealmService</code>
	 */
	protected void unsetRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("DefaultUserRealm unset in to bundle");
		}
		this.realmservice = null;
	}

	public static RealmService getRealmservice() {
		return realmservice;
	}
}
