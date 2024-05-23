package com.qc.printers.common.signin.domain.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class FaceDataResp implements Serializable {
    @JsonProperty("face_data")
    private String faceData;

    private Integer code;
}
