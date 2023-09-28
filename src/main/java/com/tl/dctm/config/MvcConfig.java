package com.tl.dctm.config;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Value("${dctm.repo.name}")
    private String dctmRepoName;
    @Value("${dctm.repo.user.name}")
    private String repoUserName;
    @Value("${dctm.repo.user.pwd}")
    private String repoUserPassword;

    @Bean
    public DfLoginInfo dfLoginInfo(){
        DfLoginInfo dfLoginInfo = new DfLoginInfo();
        dfLoginInfo.setUser(repoUserName);
        dfLoginInfo.setPassword(repoUserPassword);
        return dfLoginInfo;
    }

    @Bean
    public IDfClient dfClient() throws DfException {
        return new DfClientX().getLocalClient();
    }

    @Bean
    public IDfSession dfSession() throws DfException {
        IDfSessionManager dfSessionManager = dfClient().newSessionManager();
        if (!dfSessionManager.hasIdentity(dctmRepoName))
            dfSessionManager.setIdentity(dctmRepoName,dfLoginInfo());
        return dfSessionManager.getSession(dctmRepoName);
    }
}
