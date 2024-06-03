package com.qc.printers.custom.signin.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
@Data
public class SigninUserCardDataDto implements Serializable {
    @JsonProperty("student_id")
    private String studentId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("card_id")
    private String cardId;
}
