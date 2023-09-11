package com.tl.dctm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserInfoDto {
    private String id;
    private String name;
    private String os;
    private String address;
    private String folder;
    private String description;
    private String aclName;
    private Integer state;
    private Integer privileges;
    private Integer capability;
}
