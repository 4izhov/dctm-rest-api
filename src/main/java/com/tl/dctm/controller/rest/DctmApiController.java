package com.tl.dctm.controller.rest;

import com.documentum.fc.common.DfException;
import com.tl.dctm.dto.ApiResponse;
import com.tl.dctm.dto.LoginInfoDto;
import com.tl.dctm.dto.UserInfoDto;
import com.tl.dctm.service.DctmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Objects;

@RestController
@RequestMapping("${api.base.url}")
public class DctmApiController {
    private final DctmService dctmService;

    @Autowired
    public DctmApiController(DctmService dctmService) {
        this.dctmService = dctmService;
    }

    @GetMapping("/{userName}")
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
                    .message(HttpStatus.OK.getReasonPhrase())
                    .debugMessage(HttpStatus.OK.toString())
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

    @GetMapping("/login/{userName}")
    public ResponseEntity<ApiResponse<LoginInfoDto>> handleUserLoginInfoRequest(
            @PathVariable("userName") String userName,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ApiResponse<LoginInfoDto> response;
        try {
            LoginInfoDto loginInfoDto = dctmService.getUserLoginInfo(userName);
            response = ApiResponse.<LoginInfoDto>builder()
                    .message(Objects.requireNonNull(
                            HttpStatus.resolve(httpServletResponse.getStatus()))
                            .getReasonPhrase())
                    .debugMessage(httpServletResponse.toString())
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
}
