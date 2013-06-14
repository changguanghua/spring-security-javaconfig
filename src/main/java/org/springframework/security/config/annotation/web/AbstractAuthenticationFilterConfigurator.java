/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.config.annotation.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/**
 * Base class for confuring {@link AbstractAuthenticationFilterConfigurator}. This is intended for internal use only.
 *
 * @see FormLoginConfigurator
 * @see OpenIDLoginConfigurator
 *
 * @param T refers to "this" for returning the current configurator
 * @param F refers to the {@link AbstractAuthenticationProcessingFilter} that is being built
 *
 * @author Rob Winch
 * @since 3.2
 */
abstract class AbstractAuthenticationFilterConfigurator<T extends AbstractAuthenticationFilterConfigurator<T,F>, F extends AbstractAuthenticationProcessingFilter> extends BaseHttpConfigurator {

    protected final F authFilter;

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    private LoginUrlAuthenticationEntryPoint authenticationEntryPoint;

    private boolean customLoginPage;
    private String loginPage;
    private String loginProcessingUrl;

    private AuthenticationFailureHandler failureHandler;

    private boolean permitAll;

    private String failureUrl;

    /**
     * Creates a new instance
     * @param authFilter the {@link AbstractAuthenticationProcessingFilter} to use
     * @param defaultLoginProcessingUrl the default URL to use for {@link #loginProcessingUrl(String)}
     */
    AbstractAuthenticationFilterConfigurator(F authFilter, String defaultLoginProcessingUrl) {
        this.authFilter = authFilter;
        loginUrl("/login");
        failureUrl("/login?error");
        loginProcessingUrl(defaultLoginProcessingUrl);
        this.customLoginPage = false;
    }

    /**
     * Specifies where users will go after authenticating successfully if they
     * have not visited a secured page prior to authenticating. This is a
     * shortcut for calling {@link #defaultSuccessUrl(String)}.
     *
     * @param defaultSuccessUrl
     *            the default success url
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T defaultSuccessUrl(String defaultSuccessUrl) {
        return defaultSuccessUrl(defaultSuccessUrl, false);
    }

    /**
     * Specifies where users will go after authenticating successfully if they
     * have not visited a secured page prior to authenticating or
     * {@code alwaysUse} is true. This is a shortcut for calling
     * {@link #successHandler(AuthenticationSuccessHandler)}.
     *
     * @param defaultSuccessUrl
     *            the default success url
     * @param alwaysUse
     *            true if the {@code defaultSuccesUrl} should be used after
     *            authentication despite if a protected page had been previously
     *            visited
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T defaultSuccessUrl(String defaultSuccessUrl, boolean alwaysUse) {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(defaultSuccessUrl);
        handler.setAlwaysUseDefaultTargetUrl(alwaysUse);
        return successHandler(handler);
    }

    /**
     * Specifies the URL used to log in. If the request matches the URL and is an HTTP POST, the
     * {@link UsernamePasswordAuthenticationFilter} will attempt to authenticate
     * the request. Otherwise, if the request matches the URL the user will be sent to the login form.
     *
     * @param loginUrl the URL used to perform authentication
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T loginUrl(String loginUrl) {
        loginProcessingUrl(loginUrl);
        return loginPage(loginUrl);
    }

    /**
     * Specifies the URL to validate the credentials.
     *
     * @param loginProcessingUrl
     *            the URL to validate username and password
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T loginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
        authFilter.setFilterProcessesUrl(loginProcessingUrl);
        return getSelf();
    }

    /**
     * Specifies a custom {@link AuthenticationDetailsSource}. The default is {@link WebAuthenticationDetailsSource}.
     *
     * @param authenticationDetailsSource the custom {@link AuthenticationDetailsSource}
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T authenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
        return getSelf();
    }

    /**
     * Specifies the {@link AuthenticationSuccessHandler} to be used. The
     * default is {@link SavedRequestAwareAuthenticationSuccessHandler} with no
     * additional properites set.
     *
     * @param successHandler
     *            the {@link AuthenticationSuccessHandler}.
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T successHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        return getSelf();
    }

    /**
     * Equivalent of invoking permitAll(true)
     * @return
     */
    public T permitAll() {
        return permitAll(true);
    }

    /**
     * Ensures the urls for {@link #failureUrl(String)} and
     * {@link #loginUrl(String)} are granted access to any user.
     *
     * @param permitAll true to grant access to the URLs false to skip this step
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T permitAll(boolean permitAll) {
        this.permitAll = permitAll;
        return getSelf();
    }

    /**
     * The URL to send users if authentication fails. This is a shortcut for
     * invoking {@link #failureHandler(AuthenticationFailureHandler)}. The
     * default is "/login?error".
     *
     * @param failureUrl
     *            the URL to send users if authentication fails (i.e.
     *            "/login?error").
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T failureUrl(String failureUrl) {
        T result = failureHandler(new SimpleUrlAuthenticationFailureHandler(failureUrl));
        this.failureUrl = failureUrl;
        return result;
    }

    /**
     * Specifies the {@link AuthenticationFailureHandler} to use when
     * authentication fails. The default is redirecting to "/login?error" using
     * {@link SimpleUrlAuthenticationFailureHandler}
     *
     * @param failureHandler
     *            the {@link AuthenticationFailureHandler} to use when
     *            authentication fails.
     * @return the {@link FormLoginConfigurator} for additional customization
     */
    public T failureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureUrl = null;
        this.failureHandler = failureHandler;
        return getSelf();
    }

    @Override
    public void init(HttpConfiguration http) throws Exception {
        if(permitAll) {
            PermitAllSupport.permitAll(http, loginPage, loginProcessingUrl, failureUrl);
        }
        http.setSharedObject(AuthenticationEntryPoint.class,registerLifecycle(authenticationEntryPoint));
    }

    @Override
    public void configure(HttpConfiguration http) throws Exception {
        PortMapper portMapper = http.getSharedObject(PortMapper.class);
        if(portMapper != null) {
            authenticationEntryPoint.setPortMapper(portMapper);
        }

        authFilter.setAuthenticationManager(http.authenticationManager());
        authFilter.setAuthenticationSuccessHandler(successHandler);
        authFilter.setAuthenticationFailureHandler(failureHandler);
        if(authenticationDetailsSource != null) {
            authFilter.setAuthenticationDetailsSource(authenticationDetailsSource);
        }
        SessionAuthenticationStrategy sessionAuthenticationStrategy = http.getSharedObject(SessionAuthenticationStrategy.class);
        if(sessionAuthenticationStrategy != null) {
            authFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        }
        RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);
        if(rememberMeServices != null) {
            authFilter.setRememberMeServices(rememberMeServices);
        }
        F filter = registerLifecycle(authFilter);
        http.addFilter(filter);
    }

    /**
     * <p>
     * Specifies the URL to send users to if login is required. If used with
     * {@link WebSecurityConfigurerAdapter} a default login page will be
     * generated when this attribute is not specified.
     * </p>
     *
     * <p>
     * If a URL is specified or this is not being used in conjuction with
     * {@link WebSecurityConfigurerAdapter}, users are required to process the
     * specified URL to generate a login page.
     * </p>
     */
    T loginPage(String loginPage) {
        this.loginPage = loginPage;
        this.authenticationEntryPoint = new LoginUrlAuthenticationEntryPoint(loginPage);
        this.customLoginPage = true;
        return getSelf();
    }

    /**
    *
    * @return true if a custom login page has been specified, else false
    */
   boolean isCustomLoginPage() {
       return customLoginPage;
   }

   /**
    * Gets the login page
    * @return the login page
    */
   String getLoginPage() {
       return loginPage;
   }

   /**
    * Gets the URL to submit an authentication request to (i.e. where
    * username/password must be submitted)
    *
    * @return the URL to submit an authentication request to
    */
   String getLoginProcessingUrl() {
       return loginProcessingUrl;
   }

   /**
    * Gets the URL to send users to if authentication fails
    * @return
    */
   String getFailureUrl() {
       return failureUrl;
   }


    @SuppressWarnings("unchecked")
    private T getSelf() {
        return (T) this;
    }
}
