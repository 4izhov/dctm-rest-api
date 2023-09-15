package com.tl.dctm.service;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfUtil;
import com.documentum.fc.common.IDfId;
import com.documentum.services.workflow.inbox.IInbox;
import com.documentum.services.workflow.inbox.IInboxCollection;
import com.tl.dctm.dto.LoginInfoDto;
import com.tl.dctm.dto.TaskContentInfoDto;
import com.tl.dctm.dto.TaskInfoDto;
import com.tl.dctm.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class DctmService {
    private final IDfSession dfSession;

    @Autowired
    public DctmService(IDfSession dfSession) {
        this.dfSession = dfSession;
    }

    public UserInfoDto getUserInfo(String userName) throws DfException {
        IDfUser dfUser = dfSession.getUser(userName);
        if (dfUser == null) throw new DfException("User must be a valid Documentum User");
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

    public LoginInfoDto getUserLoginInfo(String userName) throws DfException {
        return LoginInfoDto.builder()
                .userName(userName)
                .userTicket(dfSession.getLoginTicketForUser(userName))
                .build();

    }

    public Collection<TaskInfoDto> getUsersInboxItems(String userName) throws DfException {
        Collection<TaskInfoDto> taskCollection = new ArrayList<>();
        IInbox inboxService = (IInbox) dfSession.getClient().newService(
                IInbox.class.getName(), dfSession.getSessionManager());
        inboxService.setDocbase(dfSession.getDocbaseName());
        inboxService.setUserName(userName);
        inboxService.setFilter(IInbox.DF_WORKFLOWTASK);
        inboxService.setSortBy("date_sent", true);
        IInboxCollection inboxCollection = inboxService.getItems();
        while (inboxCollection.next()){
            taskCollection.add(
                    TaskInfoDto.builder()
                            .id(inboxCollection.getObjectId().getId())
                            .stage(inboxCollection.getTaskState())
                            .title(inboxCollection.getTaskName())
                            .author(inboxCollection.getSentBy())
                            .body(inboxCollection.getString("task_subject"))
                            .date(inboxCollection.getDateSent().getDate().getTime())
                            .level(String.valueOf(inboxCollection.getPriority()))
                            .content(getContentInfo(inboxCollection.getId("item_id")))
                            .build()
            );
        }
        inboxCollection.close();
        return taskCollection;
    }

    private Collection<TaskContentInfoDto> getContentInfo(IDfId workItemId) throws DfException {
        Collection<TaskContentInfoDto> result = new ArrayList<>();
        IDfWorkitem workItem = (IDfWorkitem) dfSession.getObject(workItemId);
        IDfCollection packages = workItem.getPackages("");
        while (packages.next()){
            IDfSysObject packageComponent = getContentObject(packages.getTypedObject());
            result.add(
                    TaskContentInfoDto.builder()
                            .name(packageComponent.getObjectName())
                            .id(packageComponent.getObjectId().getId())
                            .build()
            );
        }
        packages.close();
        return result;
    }

    private IDfSysObject getContentObject(IDfTypedObject packageObject) throws DfException {
        IDfId contentObjId = packageObject.hasAttr("r_component_chron_id") ?
                packageObject.getId("r_component_chron_id") :
                packageObject.getId("r_component_id");
        return  (IDfSysObject) dfSession.getObject(contentObjId);
    }

    public byte[] getObjectContent(String objectId) throws DfException {
        byte[] data = new byte[]{};
        IDfPersistentObject dfObject = dfSession.getObject(DfUtil.toId(objectId));
        if (dfObject instanceof IDfSysObject){
            data = ((IDfSysObject)dfObject).getContent().readAllBytes();
        }
        return data;
    }
}
