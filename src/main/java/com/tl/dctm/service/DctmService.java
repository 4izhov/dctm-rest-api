package com.tl.dctm.service;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.services.workflow.inbox.IInbox;
import com.documentum.services.workflow.inbox.IInboxCollection;
import com.tl.dctm.dto.LoginInfoDto;
import com.tl.dctm.dto.TaskContentInfoDto;
import com.tl.dctm.dto.TaskInfoDto;
import com.tl.dctm.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
        inboxService.setQueryDocInfo(true);
        inboxService.setAdditionalAttributes("message");
        inboxService.setFilter(IInbox.DF_WORKFLOWTASK);
        inboxService.setSortBy("date_sent", true);
        IInboxCollection inboxCollection = inboxService.getItems();
        while (inboxCollection.next()){
            Calendar calendar = getCalendarData(inboxCollection.getDateSent().getDate());
            taskCollection.add(
                    TaskInfoDto.builder()
                            .id(inboxCollection.getObjectId().getId())
                            .stage(TaskState.getState(inboxCollection.getTaskState()))
                            .title(inboxCollection.getTaskName())
                            .author(inboxCollection.getSentBy())
                            .body(getTaskMessage(inboxCollection))
                            .dateSent(inboxCollection.getDateSent().getDate().getTime())
                            .priority(TaskPriority.getPriority(inboxCollection.getPriority()))
                            .content(getContentInfo(inboxCollection))
                            .dueDate(getDueDate(inboxCollection.getDueDate(),calendar.getTime()))
                            .build()
            );
        }
        inboxCollection.close();
        return taskCollection;
    }

    private Long getDueDate(IDfTime dueDate, Date defaultDate) {
        return Objects.equals(dueDate,DfTime.DF_NULLDATE)?
                defaultDate.getTime():dueDate.getDate().getTime();
    }

    private Calendar getCalendarData(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.roll(Calendar.DAY_OF_MONTH,3);
        return calendar;
    }

    private Collection<TaskContentInfoDto> getContentInfo(
            IInboxCollection inboxCollectionItem) throws DfException {
        Collection<TaskContentInfoDto> result = new ArrayList<>();
        int countOfDocs = inboxCollectionItem.getDocumentCount();
        for (int i = 0; i < countOfDocs; i++) {
            result.add(TaskContentInfoDto.builder()
                    .name(inboxCollectionItem.getDocumentObjectName(i))
                    .id(inboxCollectionItem.getDocumentObjectId(0).getId())
                    .build());
        }
        return result;
    }

    private String getTaskMessage(IInboxCollection inboxCollectionItem) throws DfException {
        return inboxCollectionItem.getString(
                inboxCollectionItem.hasAttr("message")?"message":"task_subject"
        );
    }

    public byte[] getObjectContent(String objectId) throws DfException {
        byte[] data = new byte[]{};
        IDfPersistentObject dfObject = dfSession.getObject(DfUtil.toId(objectId));
        if (dfObject instanceof IDfSysObject){
            data = ((IDfSysObject)dfObject).getContent().readAllBytes();
        }
        return data;
    }

    private enum TaskState {
        Acquired("acquired"),Dormant("dormant"),Faulted("faulted"),
        Finished("finished"),Paused("paused"),unknown("unknown");

        private final String state;

        TaskState(String state) {
            this.state = state;
        }

        public static String getState(Integer wiState){
            switch (wiState) {
                case IDfWorkitem.DF_WI_STATE_ACQUIRED:
                    return Acquired.state;
                case IDfWorkitem.DF_WI_STATE_DORMANT:
                    return Dormant.state;
                case IDfWorkitem.DF_WI_STATE_FAULTED:
                    return Faulted.state;
                case IDfWorkitem.DF_WI_STATE_FINISHED:
                    return Finished.state;
                case IDfWorkitem.DF_WI_STATE_PAUSED:
                    return Paused.state;
                default: return unknown.state;
            }
        }
    }

    private enum TaskPriority{
        Low("low"),Medium("medium"),High("high"),Unknown("unknown");

        private final String priority;

        TaskPriority(String value) {
            this.priority =value;
        }

        public static String getPriority(Integer value){
            switch (value){
                case 0: return Low.priority;
                case 5: return Medium.priority;
                case 10: return High.priority;
                default: return Unknown.priority;
            }
        }
    }
}
