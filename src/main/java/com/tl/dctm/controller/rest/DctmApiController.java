package com.tl.dctm.controller.rest;

import com.documentum.fc.common.DfException;
import com.tl.dctm.dto.*;
import com.tl.dctm.service.DctmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("${api.base.url}")
public class DctmApiController {
    @Value("${http-request.header.dctm-ticket}")
    private String httpHeaderNameDctmTicket;
    private final DctmService dctmService;
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    public DctmApiController(DctmService dctmService) {
        this.dctmService = dctmService;
    }

    @GetMapping("/v1/userInfo")
    public ResponseEntity<ApiResponse<UserInfoDto>> handleUserInfoRequest(
            @RequestParam String user,
            HttpServletResponse httpServletResponse,
            HttpServletRequest httpServletRequest){
        ApiResponse<UserInfoDto> response;
        try {
            logger.log(Level.INFO,"ticket: {0}",
                    httpServletRequest.getHeader(httpHeaderNameDctmTicket)
                            .substring(0,128).concat("..."));
            boolean isTicketOk = dctmService.validateTicket(
                    httpServletRequest.getHeader(httpHeaderNameDctmTicket));
            UserInfoDto userInfoDto = dctmService.getUserInfo(user);
            response = ApiResponse.<UserInfoDto>builder()
                    .data(Collections.singleton(userInfoDto))
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .message(httpServletRequest.getRequestURL().toString())
                    .debugMessage(httpServletRequest.getRequestURI())
                    .build();
        } catch (DfException exception) {
            response = ApiResponse.<UserInfoDto>builder()
                    .debugMessage(httpServletRequest.getRequestURI())
                    .message(exception.getLocalizedMessage())
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .data(Collections.singleton(UserInfoDto.builder().build()))
                    .build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v1/login")
    public ResponseEntity<ApiResponse<LoginInfoDto>> handleUserLoginInfoRequest(
            @RequestBody LoginInfoDto payload,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ApiResponse<LoginInfoDto> response;
        try {
            LoginInfoDto loginInfoDto =
                    dctmService.getUserLoginInfo(payload.getUserName());
            response = ApiResponse.<LoginInfoDto>builder()
                    .message(httpServletRequest.getRequestURL().toString())
                    .debugMessage(httpServletRequest.getRequestURI())
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .data(Collections.singleton(loginInfoDto))
                    .build();
        } catch (DfException exception){
            response = ApiResponse.<LoginInfoDto>builder()
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .debugMessage(httpServletRequest.getRequestURI())
                    .message(exception.getLocalizedMessage())
                    .data(Collections.singleton(
                            LoginInfoDto.builder().userName(payload.getUserName()).build()))
                    .returnCode(1)
                    .build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/tasks")
    public ResponseEntity<ApiResponse<TaskInfoDto>> handleUserTasksRequest(
            @RequestParam String user,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ApiResponse<TaskInfoDto> response;
        // получаем задачи пользователя
        // пока, в сессии супера. TODO изменить на получение пользовательской сесии
        try {
            boolean isTicketOk = dctmService.validateTicket(
                    httpServletRequest.getHeader(httpHeaderNameDctmTicket));

            Collection<TaskInfoDto> taskInfoDtoCollection =
                    dctmService.getUsersInboxItems(user);
            response = ApiResponse.<TaskInfoDto>builder()
                    .data(taskInfoDtoCollection)
                    .httpStatusCode(httpServletResponse.getStatus())
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .message(httpServletRequest.getRequestURL().toString())
                    .debugMessage(httpServletRequest.getRequestURI())
                    .build();
        } catch (DfException exception) {
            response = ApiResponse.<TaskInfoDto>builder()
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .debugMessage(httpServletRequest.getRequestURI())
                    .message(exception.getLocalizedMessage())
                    .data(Collections.singleton(TaskInfoDto.builder().build()))
                    .returnCode(1)
                    .build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/content")
    public ResponseEntity<ApiResponse<ContentDto>> handleContentRequest(
            @RequestParam String objectId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse){
        ApiResponse<ContentDto> response;
        try {
            boolean isTicketOk = dctmService.validateTicket(
                    httpServletRequest.getHeader(httpHeaderNameDctmTicket));

            byte[] data = dctmService.getObjectContent(objectId);
            response = ApiResponse.<ContentDto>builder()
                    .debugMessage(httpServletRequest.getRequestURI())
                    .message(httpServletRequest.getRequestURL().toString())
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .data(Collections.singleton(ContentDto.builder().data(data).build()))
                    .build();
        } catch (Exception exception) {
            response = ApiResponse.<ContentDto>builder()
                    .httpStatus(HttpStatus.resolve(httpServletResponse.getStatus()))
                    .httpStatusCode(httpServletResponse.getStatus())
                    .debugMessage(httpServletRequest.getRequestURI())
                    .message(exception.getLocalizedMessage())
                    .data(Collections.singleton(ContentDto.builder().data(new byte[]{}).build()))
                    .returnCode(1)
                    .build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v2/content")
    public ResponseEntity<ByteArrayResource> handleContentRequest(
            @RequestParam String objectId,
            HttpServletRequest httpServletRequest) throws DfException {
        boolean isTicketOk = dctmService.validateTicket(
                httpServletRequest.getHeader(httpHeaderNameDctmTicket));

        byte[] data = dctmService.getObjectContent(objectId);
        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment;filename="+ path.getFileName().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

}
