package org.apache.isis.config.beans;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WebAppConfigBean {

    private String applicationName;
    private String applicationVersion;
    private String aboutMessage;
    private String welcomeMessage;
    private String faviconUrl;
    private String faviconContentType;
    private String brandLogoHeader;
    private String brandLogoSignin;
    private String applicationCss;
    private String applicationJs;
    
}
