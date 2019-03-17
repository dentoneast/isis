package springapp.boot.web;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.config.AppConfig;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.core.security.authentication.bypass.AuthenticatorBypass;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.security.authorization.bypass.AuthorizorBypass;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import springapp.dom.DomainModule;

/**
 * Bootstrap the application.
 */
public class SpringAppManifest extends AppManifestAbstract2 implements AppConfig {

    public static final Builder BUILDER = Builder
            .forModule(new DomainModule())
            .withConfigurationPropertiesFile(
                    SpringAppManifest.class, "isis-non-changing.properties")
            .withAuthMechanism("shiro")
            ;

    public SpringAppManifest() {
        super(BUILDER);
        
        ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
                ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;
        
    }

    // Implementing AppConfig, to tell the framework how to bootstrap the configuration.
    @Override @Produces @Singleton
    public IsisConfiguration isisConfiguration() {
        return IsisConfiguration.buildFromAppManifest(this);
    }
    
     /**
     * The standard authentication manager, configured with the 'bypass' authenticator 
     * (allows all requests through).
     * <p>
     * integration tests ignore appManifest for authentication and authorization.
     */
    @Produces @Singleton
    public AuthenticationManager authenticationManagerWithBypass() {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard();
        authenticationManager.addAuthenticator(new AuthenticatorBypass());
        return authenticationManager;
    }
    
    @Produces @Singleton
    public AuthorizationManager authorizationManagerWithBypass() {
        final AuthorizationManagerStandard authorizationManager = new AuthorizationManagerStandard() {
            {
                authorizor = new AuthorizorBypass();
            }  
        };
        return authorizationManager;
    }
    
    @Produces @Singleton
    public WebAppConfigBean webAppConfigBean() {
        return WebAppConfigBean.builder()
                .build();
    }

    
    
    
}
