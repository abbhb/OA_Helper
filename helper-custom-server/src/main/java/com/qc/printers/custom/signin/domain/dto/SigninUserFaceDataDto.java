package com.qc.printers.custom.signin.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SigninUserFaceDataDto implements Serializable {
    @JsonProperty("student_id")
    private String studentId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("face_data")
    private String faceData;
}
