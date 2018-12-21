Migration Notes:
 * @MemberGroupLayout was removed
 * Axon Eventbus Plugin: switching from axon 2.x to 3.x which involves that axon's *EventHandler* annotation has moved: org.axonframework.eventhandling.annotation.EventHandler -> org.axonframework.eventhandling.EventHandler
 * _IsisMatchers_ is no longer part of the 'core' API, but still available within test-scope.
 * _Ensure_ as part of the 'core' API now accepts Java Predicates instead of hamcrest Matchers
 * deployment types SERVER_EXPLORATION, UNIT_TESTING have been removed
 * Some ways of setting the DeploymentType (using web.xml or WebServer cmd-line flags -t or --type) have been removed. Instead running in PROTOTYPING (exemplified with Jetty) can be done in following ways:
{code:java}
 export PROTOTYPING=true ; mvn jetty:run
 mvn -DPROTOTYPING=true jetty:run
 mvn -Disis.deploymentType=PROTOTYPING jetty:run
{code}
We also introduced a SPI to customize this behavior. This issue is tracked by https://issues.apache.org/jira/browse/ISIS-1991

 * web.xml: no longer required to install listeners, filters and servlets; but is still required to configure the welcome page; _org.apache.isis.core.webapp.IsisWebAppContextListener_ acts as the single application entry-point to setup the dynamic part of the ServletContext.
 ** ResourceCachingFilter is now configured via annotations (Servlet 3.0 spec), no longer needed to be declared in web.xml
 ** ResourceServlet is now configured via annotations (Servlet 3.0 spec), no longer needed to be declared in web.xml
 ** IsisTransactionFilterForRestfulObjects is now configured via annotations (Servlet 3.0 spec), no longer needed to be declared in web.xml
 ** webjars Servlet was removed, no longer needed to be declared in web.xml
 ** Shiro Environment, no longer needs to be declared in web.xml
 ** Wicket Environment, no longer needs to be declared in web.xml
 ** RestEasy Environment, no longer needs to be declared in web.xml
 ** IsisSessionFilter is now part of the RestEasy WebModule, no longer needs to be declared in web.xml
 ** LogOnExceptionLogger, no longer needs to be declared in web.xml
 * web.xml apart from the new WebContextListener we introduce new web-specific (optional) config values, nothing else needs to configured here:
{code:xml}
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	id="WebApp_ID" version="3.1">
	<display-name>My App</display-name>

	<welcome-file-list>
		<welcome-file>about/index.html</welcome-file>
	</welcome-file-list>

	<!-- unique bootstrapping entry-point for web-applications -->
        <listener>
		<listener-class>org.apache.isis.core.webapp.IsisWebAppContextListener</listener-class>
	</listener>

	<!-- optional for overriding default 'wicket' -->
	<context-param>
		<param-name>isis.viewer.wicket.basePath</param-name>
		<param-value>my-wicket</param-value>
	</context-param>

	<!-- optional for overriding default 'org.apache.isis.viewer.wicket.viewer.IsisWicketApplication' -->
	<context-param>
		<param-name>isis.viewer.wicket.app</param-name>
		<param-value>domainapp.webapp.MyDomainApplication</param-value>
	</context-param>
	
	<!-- optional for overriding default 'restful' -->
	<context-param>
		<param-name>isis.viewer.restfulobjects.basePath</param-name>
		<param-value>my-restful</param-value>
	</context-param>

</web-app>
{code}

 * module 'shiro' moved from `/core` to `/core/plugins` and its maven artifactId changed, to be in line with the other core-plugins:
{code:xml}
<dependency>
	<groupId>org.apache.isis.core</groupId>
	<artifactId>isis-core-plugins-security-shiro</artifactId>
</dependency>
{code}

 * ObjectAdapter is no longer holding a reference to an ObjectSpecification for the element type of collections. ObjectAdapter#getElementSpecification() moved to ObjectSpecification#getElementSpecification().
 * ServicesInjector does now implement interface ServiceRegistry, where the service lookup method changed:
{code:java}
// previous
<T> T lookupService(final Class<T> serviceClass);
// new with 2.0.0-M2
<T> Optional<T> lookupService(final Class<T> serviceClass);
{code}

 * We do now provide JAXB XmlAdapters for Java built-in temporal types in 'applib': org.apache.isis.applib.adapters.JaxbAdapters
 * Wicket-Viewer: Instead of browser built-in tooltip rendering, the framework now provides tooltips using Javascript and CSS, currently with following stylesheet defaults:
{noformat}
.ui-tooltip {
    max-width: 300px;
    background-color: WhiteSmoke;
    text-align: center;
    padding: 5px 10px;
    border-radius: 4px;
    font-size: 12px;
    box-shadow: 0 0 7px black;
 
    position: absolute;
    z-index: 9999;
}

span.isis-component-with-tooltip, 
label.isis-component-with-tooltip, 
.isis-component-with-tooltip label, 
strong.isis-component-with-tooltip  {
   text-decoration: underline dashed;
}

.ui-helper-hidden-accessible { display:none; } /* accessibility support disabled */
{noformat}

 * Rest Viewer: The content negotiation parameter 'suppress' does now allow more control on which '$$..' properties one wants to suppress. New options are
{code:java}
public static enum SuppressionType {
    /** suppress '$$RO', RO Spec representation*/
    RO,
    
    /** suppress '$$href', hyperlink to the representation*/
    HREF,
    
    /** suppress '$$instanceId', instance id of the domain object*/
    ID,

    /** suppress '$$domainType', object spec of the domain object */
    DOMAIN_TYPE,    

    /** suppress '$$title', title of the domain object*/
    TITLE,
    
    /** suppress all '$$...' entries*/
    ALL
}{code}
where these are case-insensitive and may be combined to a comma-separated set.
 Eg. to suppress $$title and $$href one could simply request
{noformat}
application/json;profile=urn:org.apache.isis/v1;suppress=title,href
{noformat}
We do not break the previous behavior with 'suppress=true' being equivalent to 'suppress=ro'

 * Adds a new JAX-RS 2.0 compliant RestfulClient to core-applib:
 Client-Side Setup:
{code:xml}
<dependency>
	<groupId>org.apache.isis.core</groupId>
	<artifactId>isis-core-applib</artifactId>
	<version>2.0.0-M2-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>javax.ws.rs</groupId>
	<artifactId>javax.ws.rs-api</artifactId>
	<version>2.1.1</version>
</dependency>
<dependency>
	<groupId>org.glassfish.jersey.core</groupId>
	<artifactId>jersey-client</artifactId>
	<version>2.25.1</version>
</dependency>
<dependency>
	<groupId>org.eclipse.persistence</groupId>
	<artifactId>org.eclipse.persistence.moxy</artifactId>
	<version>2.6.0</version>
</dependency>
{code}
Synchronous example with Basic-Auth:
{code:java}
RestfulClientConfig clientConfig = new RestfulClientConfig();
clientConfig.setRestfulBase("http://localhost:8080/helloworld/restful/");
// setup basic-auth
clientConfig.setUseBasicAuth(true);
clientConfig.setRestfulAuthUser("sven");
clientConfig.setRestfulAuthPassword("pass");

RestfulClient client = RestfulClient.ofConfig(clientConfig);

Builder request = client.request(
				"services/myService/actions/lookupMyObjectById/invoke", 
				SuppressionType.setOf(SuppressionType.RO));

Entity<String> args = client.arguments()
		.addActionParameter("id", "12345")
		.build();

Response response = request.post(args);

ResponseDigest<MyObject> digest = client.digest(response, MyObject.class);

if(digest.isSuccess()) {
	System.out.println("result: "+ digest.get().get$$instanceId());
} else {
	digest.getFailureCause().printStackTrace();
}
{code}
Asynchronous example with Basic-Auth:
{code:java}
RestfulClientConfig clientConfig = new RestfulClientConfig();
clientConfig.setRestfulBase("http://localhost:8080/helloworld/restful/");
// setup basic-auth
clientConfig.setUseBasicAuth(true);
clientConfig.setRestfulAuthUser("sven");
clientConfig.setRestfulAuthPassword("pass");

RestfulClient client = RestfulClient.ofConfig(clientConfig);

Builder request = client.request(
                "services/myService/actions/lookupMyObjectById/invoke", 
                SuppressionType.setOf(SuppressionType.RO));

Entity<String> args = client.arguments()
        .addActionParameter("id", "12345")
        .build();

Future<Response> asyncResponse = request
        .async()
        .post(args);

CompletableFuture<ResponseDigest<MyObject>> digestFuture = 
                client.digest(asyncResponse, MyObject.class);
        
ResponseDigest<MyObject> digest = digestFuture.get(); // blocking

if(digest.isSuccess()) {
    System.out.println("result: "+ digest.get().get$$instanceId());
} else {
    digest.getFailureCause().printStackTrace();
}
{code}

 * Support for concurrent computation within an open session utilizing a ForkJoinPool
{code:java}
Supplier<T> computation = ()->doSomeComputation(); 
CompletableFuture<T> completableFuture = IsisContext.compute(computation);

T result = completableFuture.get(); // blocking call
{code}

 * ConfigurationService and its internal implementation(s) were removed, instead use IsisConfiguration, which can be retrieved either via injection or static method:
{code:java}
@Inject IsisConfiguration configuration;
// or
IsisConfiguration configuration = IsisContext.getConfiguration();
{code}
The Configuration Menu within the UI now uses its own (and completely separated) interface, that handles masking of sensitive values (eg. passwords):
{code:java}
package org.apache.isis.applib.services.confview;

public interface ConfigurationViewService {
    /**
     * Returns all properties, each as an instance of {@link ConfigurationProperty} (a view model).
     * Mask sensitive values if required.
     */
    Set<ConfigurationProperty> allProperties();
}
{code}
_@PostConstuct_ methods declared with domain objects no longer get passed over the IsisConfiguration. For now only zero-arg initializers are supported. (We might re-add parameter support, this is work in progress)

 * Wicket-Viewer: Customize the ThemeChooser by providing your own implementation of IsisWicketThemeSupport
{code:java}
public interface IsisWicketThemeSupport {
    ThemeProvider getThemeProvider();
    List<String> getEnabledThemeNames();
}
{code}
to be configured using
{noformat}
isis.viewer.wicket.themes.provider=org.my.IsisWicketThemeSupport
{noformat}
