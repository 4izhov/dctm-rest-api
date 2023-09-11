package com.tl.dctm.service;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import com.tl.dctm.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DctmService {
    private final IDfSession dfSession;

    @Autowired
    public DctmService(IDfSession dfSession) {
        this.dfSession = dfSession;
    }

    public UserInfoDto getUserInfo(String userName) throws DfException {
        IDfUser dfUser = dfSession.getUser(userName);
        return UserInfoDto.builder()
                .name(dfUser.getUserName())
                .id(dfUser.getObjectId().getId())
                .aclName(dfUser.getACLName())
                .address(dfUser.getUserAddress())
                .os(dfUser.getUserOSName())
                .state(dfUser.getUserState())
                .capability(dfUser.getClientCapability())
                .description(dfUser.getDescription())
                .folder(dfUser.getDefaultFolder())
                .privileges(dfUser.getUserPrivileges())
                .build();
    }
}
