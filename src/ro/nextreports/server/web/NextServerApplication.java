/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.protocol.HTTP;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.devutils.DevUtilsPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlEncoder;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.response.filter.AjaxServerAndClientTimeFilter;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.odlabs.wiquery.core.WiQuerySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.IFrameSettings;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.MaintenanceException;
import ro.nextreports.server.schedule.QuartzJobHandler;
import ro.nextreports.server.schedule.UserSynchronizerJob;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.misc.NoVersionMountMapper;
import ro.nextreports.server.web.core.ErrorPage;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.LicenseErrorPage;
import ro.nextreports.server.web.core.MaintenancePage;
import ro.nextreports.server.web.core.SecurePage;
import ro.nextreports.server.web.core.settings.LogoResourceReference;
import ro.nextreports.server.web.dashboard.WidgetWebPage;
import ro.nextreports.server.web.debug.SystemInfoPage;
import ro.nextreports.server.web.debug.SystemLogPage;
import ro.nextreports.server.web.integration.DashboardsPage;
import ro.nextreports.server.web.integration.ReportsPage;
import ro.nextreports.server.web.security.LoginPage;
import ro.nextreports.server.web.security.SecurityUtil;
import ro.nextreports.server.web.security.cas.CasLoginErrorPage;
import ro.nextreports.server.web.security.cas.CasLoginPage;
import ro.nextreports.server.web.security.cas.CasUtil;
import ro.nextreports.server.web.security.recover.ForgotPasswordPage;
import ro.nextreports.server.web.security.recover.ResetPasswordPage;
import ro.nextreports.server.web.themes.ThemesManager;

/*
import com.asf.license.License;
import com.asf.license.LicenseException;
import com.asf.license.LicenseManager;
import com.asf.license.LicenseNotFoundException;
*/

/**
 * @author Decebal Suiu
 */
public class NextServerApplication extends WebApplication  {

	private static volatile boolean maintenance = false;
	private static final Logger LOG = LoggerFactory.getLogger(NextServerApplication.class);			
	
	public NextServerApplication() {
		super();		
	}

	public static NextServerApplication get() {
		return (NextServerApplication) WebApplication.get();
	}

	@Override
	public void init() {
		super.init();
		
		// spring
		addSpringInjection();        

		// markup settings
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		
		// application settings
		if (CasUtil.isCasUsed()) {
			getApplicationSettings().setPageExpiredErrorPage(CasLoginPage.class);
//			getApplicationSettings().setInternalErrorPage(CasLoginErrorPage.class);
			getApplicationSettings().setAccessDeniedPage(CasLoginPage.class);
		} else {
			getApplicationSettings().setPageExpiredErrorPage(LoginPage.class);
//			getApplicationSettings().setInternalErrorPage(LoginErrorPage.class);
			getApplicationSettings().setAccessDeniedPage(LoginPage.class);
		}				
		
		// show internal error page rather than default developer page 
//		getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE); 		

		// exception settings
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		
		// security settings
		addSecurityAuthorization();
		
		// request cycle settings
//		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
		
		// debug
//		getDebugSettings().setAjaxDebugModeEnabled(false);
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);
		
		// activate some options only in "DEVELOPMENT" mode
		if (usesDevelopmentConfig()) {
			// enable request logger
			getRequestLoggerSettings().setRequestLoggerEnabled(true);
			getRequestLoggerSettings().setRequestsWindowSize(3000);
			
			// locate where wicket markup comes from your browser's source view
			getDebugSettings().setOutputMarkupContainerClassName(true);
		}

		// mount
//		new AnnotatedMountScanner().scanPackage(NextServerApplication.class.getPackage().getName()).mount(this);
		
		mount(new NoVersionMountMapper("/home", HomePage.class));		
		
		if (CasUtil.isCasUsed()) {
			//mountPage("/login", CasLoginPage.class);
			mount(new NoVersionMountMapper("/login", CasLoginPage.class));
			// this matches the value set in securityCas.xml
			//mountPage("/cas/error", CasLoginErrorPage.class); 
			mount(new NoVersionMountMapper("/cas/error", CasLoginErrorPage.class));
		} else {
			//mountPage("/login", LoginPage.class);
			mount(new NoVersionMountMapper("/login", LoginPage.class));
		}
		mountPage("/debug", DevUtilsPage.class);		
		mountPage("/sysinfo", SystemInfoPage.class);
		mountPage("/syslog", SystemLogPage.class);		
//		mountPage("/addFolders", AddFoldersPage.class); // for development
//		mountPage("/pivot", PivotPage.class); // for development
		mountPage("/forgot", ForgotPasswordPage.class);
		mountPage("/reset", ResetPasswordPage.class);
		mountPage("/dashboards", DashboardsPage.class);
		mountPage("/reports", ReportsPage.class);
		
		// need to have a static url to view logo in maintenance page
		mountResource("/../images/logo.png", new LogoResourceReference());

		// load all jobs from repository to scheduler
		addJobsInScheduler();						
		
	    StorageService storageService = (StorageService)getSpringBean("storageService");	    		
		if (storageService.getSettings().getSynchronizer().isRunOnStartup()) {
			runUserSynchronizerJob();
		}
		
		IFrameSettings iframeSettings = storageService.getSettings().getIframe();
		if ((iframeSettings != null) && iframeSettings.isEnable()) { 
			mountPage("/widget", WidgetWebPage.class);
		}
		
		// set the current color theme at startup
		ThemesManager.getInstance().setTheme(storageService.getSettings().getColorTheme());
		
		getRequestCycleListeners().add(new ExceptionRequestCycleListener());
		getRequestCycleListeners().add(new LoggingRequestCycleListener());
		getRequestCycleListeners().add(new MaintenanceRequestCycleListener());
	}			

	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}
	
	@Override
	public Session newSession(Request request, Response response) {
		return new NextServerSession(request);
	}
	
	public Object getSpringBean(String beanName) {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        if (!applicationContext.containsBean(beanName)) {
        	return null;
        }
        
        return applicationContext.getBean(beanName);
	}
	
    protected void addSpringInjection() {
    	getComponentInstantiationListeners().add(new SpringComponentInjector(this));
	}
	
	protected void addSecurityAuthorization() {
		Class<? extends Page> signInPageClass = LoginPage.class;
		if (CasUtil.isCasUsed()) {
			signInPageClass = CasLoginPage.class;
		}
		
		IAuthorizationStrategy authStrategy = new SimplePageAuthorizationStrategy(SecurePage.class, signInPageClass) {

			@Override
			protected boolean isAuthorized() {
				boolean b = NextServerSession.get().isSignedIn();
				if (!b) {
					if (CasUtil.isCasUsed()) {
						LOG.debug("Checking if context contains CAS authentication");
						b = NextServerSession.get().checkForSignIn();
						if (!b) {
							String serviceUrl = CasUtil.getServiceProperties().getService();
							String loginUrl = CasUtil.getLoginUrl();
							LOG.debug("cas authentication: service URL: " + serviceUrl);
							String redirectUrl = loginUrl + "?service=" + serviceUrl;
							LOG.debug("attempting to redirect to: " + redirectUrl);
							throw new RestartResponseAtInterceptPageException(new RedirectPage(redirectUrl));
						}
					}
				}
				
				/*
				if (b) {
					checkLicense();
				}
				*/
				 
				return b;
			}

		};
		getSecuritySettings().setAuthorizationStrategy(authStrategy);
	}

	protected void addJobsInScheduler() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Add jobs in scheduler...");
		}
		long t = System.currentTimeMillis();		
		
		SchedulerJob[] schedulerJobs = getSchedulerJobs();
    	QuartzJobHandler quartzJobHandler = (QuartzJobHandler) getSpringBean("quartzJobHandler");
        for (SchedulerJob schedulerJob : schedulerJobs) {
			try {
				quartzJobHandler.addJob(schedulerJob);
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}
		}        
        
        if (LOG.isDebugEnabled()) {
        	t = System.currentTimeMillis() - t;
        	LOG.debug("Added jobs in scheduler in " + t + " ms");
        }
	}

	@Override
	protected void validateInit() {
	    super.validateInit();
	    
	    WiQuerySettings.get().setAutoImportJQueryResource(false);
	}
	
	@SuppressWarnings("unchecked")
	private SchedulerJob[] getSchedulerJobs() {
		PlatformTransactionManager transactionManager = (PlatformTransactionManager) getSpringBean("transactionManager");
    	TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    	SchedulerJob[] schedulerJobs = (SchedulerJob[]) transactionTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus transactionStatus) {
				StorageDao storageDao = (StorageDao) getSpringBean("storageDao");
				try {
			    	Entity[] entities = storageDao.getEntitiesByClassName(StorageConstants.SCHEDULER_ROOT, SchedulerJob.class.getName());
			    	SchedulerJob[] schedulerJobs = new SchedulerJob[entities.length];
			    	System.arraycopy(entities, 0, schedulerJobs, 0, entities.length);
			    	
			    	return schedulerJobs;
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
                    transactionStatus.setRollbackOnly();
                    return null;
				}
			}

    	});

		return schedulerJobs;
	}
	
	/*
	private void checkLicense() {
		try {
			License license = LicenseManager.getInstance().getLicense();
			boolean b = LicenseManager.getInstance().isValidLicense(license);
			if (!b) {
				throw new RuntimeException(new LicenseException("Invalid license"));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	*/
	
	private void runUserSynchronizerJob() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Run user synchronizer job ...");
		}
		long t = System.currentTimeMillis();		
		
//		JobDetail userSynchronizerJob = (JobDetail) getSpringBean("userSynchronizerJob");
		ProviderManager authenticationManager = (ProviderManager) getSpringBean("authenticationManager");
		UserSynchronizerJob userSynchronizerJob = new UserSynchronizerJob();
		userSynchronizerJob.setAuthenticationManager(authenticationManager);
		userSynchronizerJob.setStorageService((StorageService)getSpringBean("storageService"));
		userSynchronizerJob.syncUsers();
		
        if (LOG.isDebugEnabled()) {
        	t = System.currentTimeMillis() - t;
        	LOG.debug("Users synchronized in " + t + " ms");
        }
	}

	private class ExceptionRequestCycleListener extends AbstractRequestCycleListener {
				
		@Override
		public IRequestHandler onException(RequestCycle cycle, Exception e) {			
			if (e instanceof PageExpiredException) {
				LOG.error("Page expired", e); // !?
				return null; // see getApplicationSettings().setPageExpiredErrorPage
			}
			
			Throwable t = ExceptionUtils.getRootCause(e);
			/*
			if ((t instanceof LicenseNotFoundException) || (t instanceof LicenseException)) {
				PageParameters parameters = new PageParameters();
				parameters.add("errorMessage", t.getMessage());
				return new RenderPageRequestHandler(new PageProvider(LicenseErrorPage.class, parameters));
			}
			*/
			
			if (e instanceof MaintenanceException) {				
				return new RenderPageRequestHandler(new PageProvider(MaintenancePage.class));				
			}
						
			String errorCode = String.valueOf(System.currentTimeMillis());
			LOG.error("Error with code " + errorCode, e);
			
			PageParameters parameters = new PageParameters();			
			parameters.add("errorCode", errorCode);
			parameters.add("errorMessage", UrlEncoder.QUERY_INSTANCE.encode(e.getMessage(), HTTP.ISO_8859_1));
			return new RenderPageRequestHandler(new PageProvider(ErrorPage.class, parameters));
		}
		
	}
	
	private class LoggingRequestCycleListener extends AbstractRequestCycleListener {

		@Override
		public void onBeginRequest(RequestCycle cycle) {
			String username = "";
			if (NextServerSession.get().isSignedIn()) {
				username = NextServerSession.get().getUsername();
			}
			
			Session session = NextServerSession.get();
			String sessionId = NextServerSession.get().getId();
			if (sessionId == null) {
				session.bind();
				sessionId = session.getId();
			}
			
			HttpServletRequest request = ((ServletWebRequest) RequestCycle.get().getRequest()).getContainerRequest();
			String ip = request.getHeader("X-Forwarded-For"); 
			if (ip == null) {
				ip = request.getRemoteHost();
			}

			MDC.put("username", username);
			MDC.put("session", sessionId);
			MDC.put("ip", ip);
		}

		@Override
		public void onEndRequest(RequestCycle cycle) {
			MDC.remove("username");
			MDC.remove("session");
			MDC.remove("ip");
		}

	}
	
	private class MaintenanceRequestCycleListener extends AbstractRequestCycleListener {

		@Override
		public void onBeginRequest(RequestCycle cycle) {			
			if (isMaintenance()) {
				User user = SecurityUtil.getLoggedUser();				
				if ((user == null) || ((user != null) && user.isAdmin())) {
					super.onBeginRequest(cycle);
					return;
				}
				throw new MaintenanceException();
			}
		}		
	}

	public static boolean isMaintenance() {
		return maintenance;
	}

	public static void setMaintenance(boolean maintenance) {
		NextServerApplication.maintenance = maintenance;
	}		
	
}
