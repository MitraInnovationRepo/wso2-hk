/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.hkjc.wso2.identity.service.rest.internal;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.user.core.service.RealmService;

import com.hkjc.wso2.identity.service.rest.AppMgtServlet;
import com.hkjc.wso2.identity.service.rest.UserMgtServlet;

/**
 * OSGI bunble component for extended authenticators
 * The class is used during Identity server startup and shutdown.
 * Also can be used for options keeping because entity of class
 * is alive during all IS working.
 * Can not be rebundled without server restarting.
 *
 * @scr.component name="vn.vpbank.wso2.identity.service.rest"
 * immediate="true"
 * @scr.reference name="osgi.httpservice"
 * interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService"
 * unbind="unsetHttpService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class RESTServiceComponent {
    /**
     * Option keeper class for RESTConfiguration.
     * It use ${IS_HOME}/repository/conf/api-manager.xml
     */
    public class RESTConfiguration {



    }

    private static Log log = LogFactory.getLog(RESTServiceComponent.class);
    private static final String APPMGT_SERVLET_URL = "/identity/apps/v1";
    private static final String USERMGT_SERVLET_URL = "/identity/users/v1";
    private static RealmService realmService;
    private static HttpService httpService;
    private static RESTConfiguration restOptions;

    public static RESTConfiguration getRESTOptions() {
        return restOptions;
    }

    /**
     * Activation of component.
     * Is called after all osgi services setups.
     * Should register all servlrts/validators/authenticators to responsible
     * osgi services. Can be used for configuration registering.
     *
     * @param ctxt unused but is needed at declaration for osgi initiation
     */
    protected void activate(ComponentContext ctxt) {
        try {
            //initiate options
            restOptions = new RESTConfiguration();

            // servlets instances creating
            Servlet appServlet = new ContextPathServletAdaptor(
                    new AppMgtServlet(),
                    APPMGT_SERVLET_URL);
            Servlet userServlet = new ContextPathServletAdaptor(
                    new UserMgtServlet(),
                    USERMGT_SERVLET_URL);

            // servlets registration.
            // when new request is catch then a servlet will be chosen
            // by Identity Server from list of registered servlets in
            // accordance with context url
            try {
                httpService.registerServlet(APPMGT_SERVLET_URL, appServlet,
                        null, null);
                httpService.registerServlet(USERMGT_SERVLET_URL, userServlet,
                        null, null);
            } catch (Exception e) {
                throw new RuntimeException("Error when registering REST Services Extension Servlet via the HttpService.", e);
            }

        } catch (Throwable e) {
            log.error("REST Services bundle activation Failed", e);
        }

		log.info("REST Services bundle activation success");
    }


    /**
     * Deactivation of component.
     * Is processed during server shutdown.
     *
     * @param ctxt unused but is needed at declaration for osgi initiation
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("REST Services bundle is deactivated");
        }
    }


    /**
     * HTTP OSGI service sharing.
     * Received object should be used for servlet registration.
     *
     * @param httpService is given by the osgi manager for servlet registration
     */
    protected void setHttpService(HttpService httpService) {
        log.debug("Setting the HTTP Service");
        RESTServiceComponent.httpService = httpService;
    }


    /**
     * HTTP OSGI service unset
     *
     * @param httpService is unused
     */
    protected void unsetHttpService(HttpService httpService) {
        log.debug("UnSetting the HTTP Service");
        RESTServiceComponent.httpService = null;
    }


    /**
     * Realm OSGI service sharing.
     *
     * @param realmService is given by the osgi manager accessing to
     *                     data storages at run-time
     */
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        RESTServiceComponent.realmService = realmService;
    }


    /**
     * Realm OSGI service unset.
     *
     * @param realmService is unused
     */
    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        RESTServiceComponent.realmService = null;
    }


    /**
     * Realm OSGI service providing.
     *
     * @return realmService received from osgi manager
     */
    public static RealmService getRealmService() {
        return realmService;
    }
}
