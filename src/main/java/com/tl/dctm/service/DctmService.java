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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class DctmService {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final IDfSession dfSession;
    private final IDfClient dfClient;
    @Value("${dctm.repo.name}")
    private String dctmRepoName;
    @Value("${ticket.validity.timeout}")
    private Integer ticketValidityTimeout;

    @Autowired
    public DctmService(IDfSession dfSession, IDfClient dfClient) {
        this.dfSession = dfSession;
        this.dfClient = dfClient;
    }

    public UserInfoDto getUserInfo(String userName,String ticket) throws DfException {
        validateUser(userName);
        IDfSession session = null;
        try {
            session = dfClient.newSession(dctmRepoName,new DfLoginInfo(userName,ticket));
            IDfUser dfUser = session.getUser(userName);
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
        } finally {
            if (!Objects.isNull(session) && session.isConnected())
                session.disconnect();
        }
    }

    public LoginInfoDto getUserLoginInfo(String userName, String userPwd) throws DfException {
        validateUser(userName);
        IDfLoginInfo dfLoginInfo = new DfLoginInfo(userName,userPwd);
        try {
            dfClient.authenticate(dctmRepoName,dfLoginInfo);
        } catch (DfAuthenticationException exception) {
            logger.log(Level.SEVERE, exception.getLocalizedMessage());
            throw new DfException(exception);
        }
        return LoginInfoDto.builder()
                .userName(userName)
                .userTicket(dfSession.getLoginTicketEx(userName,"global",
                        ticketValidityTimeout,false,null))
                .build();
    }

    public Collection<TaskInfoDto> getUsersInboxItems(String userName, String ticket)
            throws DfException {
        validateUser(userName);
        IDfSession oneTimeDfSession = null;
        try {
            oneTimeDfSession = dfClient.newSession(dctmRepoName, new DfLoginInfo(userName,ticket));
            Collection<TaskInfoDto> taskCollection = new ArrayList<>();
            IInbox inboxService = (IInbox) oneTimeDfSession.getClient().newService(
                    IInbox.class.getName(), oneTimeDfSession.getSessionManager());
            inboxService.setDocbase(oneTimeDfSession.getDocbaseName());
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
        } finally {
            if (!Objects.isNull(oneTimeDfSession) && oneTimeDfSession.isConnected())
                oneTimeDfSession.disconnect();
        }
    }

    private void validateUser(String userName) throws DfException {
        if (Objects.isNull(userName) || userName.isBlank()
                || Objects.isNull(dfSession.getUser(userName))){
            throw new DfException("The user must be a valid Documentum User");
        }
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

    public Map<String,Object> getObjectContent(String objectId, String ticket)
            throws DfException {
        String mimeType = "unknown";
        byte[] data = new byte[]{};
        IDfSession oneTimeDfSession = null;
        try {
            oneTimeDfSession = dfClient.newSession(dctmRepoName,
                    new DfLoginInfo(extractUserNameFromTicket(ticket),ticket));
            IDfPersistentObject dfObject = oneTimeDfSession.getObject(DfUtil.toId(objectId));
            if (dfObject instanceof IDfSysObject){
                data = ((IDfSysObject)dfObject).getContent().readAllBytes();
                mimeType = ((IDfSysObject)dfObject).getFormat().getMIMEType();
            }
            return Map.of("mimeType",mimeType,"data",data);
        } finally {
            if (!Objects.isNull(oneTimeDfSession) && oneTimeDfSession.isConnected())
                oneTimeDfSession.disconnect();
        }
    }

    public boolean validateTicket(String ticket) throws DfException {
        if (Objects.isNull(ticket) || ticket.isBlank())
            throw new DfException("The HTTP Header with login ticket not found or empty.");
        logger.log(Level.INFO,"ticket: {0}",ticket.substring(0,128).concat("..."));
        try {
            dfClient.authenticate(dctmRepoName,
                    new DfLoginInfo(extractUserNameFromTicket(ticket),ticket));
        } catch (DfAuthenticationException exception){
            logger.log(Level.SEVERE,exception.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private String extractUserNameFromTicket(String ticket) throws DfException {
        String dump = getDump(ticket);
        return dump
                .substring(dump.indexOf("User"),dump.indexOf("Domain"))
                .split(":")[1]
                .trim();
    }

    public String getDump(String value) throws DfException {
        return dfClient.getLoginTicketDiagnostics(value);
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
