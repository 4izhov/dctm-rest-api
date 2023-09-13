package com.tl.dctm.controller.rest;

import com.documentum.fc.common.DfException;
import com.tl.dctm.dto.ApiResponse;
import com.tl.dctm.dto.LoginInfoDto;
import com.tl.dctm.dto.TaskInfoDto;
import com.tl.dctm.dto.UserInfoDto;
import com.tl.dctm.service.DctmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping("${api.base.url}")
public class DctmApiController {
    private final DctmService dctmService;

    @Autowired
    public DctmApiController(DctmService dctmService) {
        this.dctmService = dctmService;
    }

    @GetMapping("/v1/{userName}")
    public ResponseEntity<ApiResponse<UserInfoDto>> handleUserInfoRequest(
            @PathVariable("userName") String userName,
            HttpServletResponse httpServletResponse,
            HttpServletRequest httpServletRequest){
        ApiResponse<UserInfoDto> response;
        try {
            UserInfoDto userInfoDto = dctmService.getUserInfo(userName);
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

    @GetMapping("/v1/login/{userName}")
    public ResponseEntity<ApiResponse<LoginInfoDto>> handleUserLoginInfoRequest(
            @PathVariable("userName") String userName,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ApiResponse<LoginInfoDto> response;
        try {
            LoginInfoDto loginInfoDto = dctmService.getUserLoginInfo(userName);
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
                    .data(Collections.singleton(LoginInfoDto.builder().userName(userName).build()))
                    .build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/tasks/{userName}")
    public ResponseEntity<ApiResponse<TaskInfoDto>> handleUserTasksRequest(
            @PathVariable("userName") String userName,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ApiResponse<TaskInfoDto> response;
        // get user's inbox items from DCTM
        try {
            Collection<TaskInfoDto> taskInfoDtoCollection = dctmService.getUsersInboxItems(userName);
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
                    .build();
        }
        return ResponseEntity.ok(response);
    }
}
