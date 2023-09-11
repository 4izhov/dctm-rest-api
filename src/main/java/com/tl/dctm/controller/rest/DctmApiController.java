package com.tl.dctm.controller.rest;

import com.documentum.fc.common.DfException;
import com.tl.dctm.dto.UserInfoDto;
import com.tl.dctm.service.DctmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/dctm")
public class DctmApiController {
    private final DctmService dctmService;

    @Autowired
    public DctmApiController(DctmService dctmService) {
        this.dctmService = dctmService;
    }

    @GetMapping("/{userName}")
    public ResponseEntity<UserInfoDto> getUserInfo(
            @PathVariable("userName") String userName) throws DfException {
        return ResponseEntity.ok(
                Optional.ofNullable(dctmService.getUserInfo(userName))
                        .orElse(UserInfoDto.builder().build())
        );
    }
}
