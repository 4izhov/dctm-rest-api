package com.tl.dctm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private HttpStatus httpStatus;
    private Integer httpStatusCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy hh:mm:ss")
    @Builder.Default private LocalDateTime timeStamp = LocalDateTime.now();
    @Builder.Default private String debugMessage = "";
    @Builder.Default private String message = "";
    @Builder.Default private Collection<T> data = Collections.emptyList();
    @Builder.Default private Integer returnCode = 0;
}
