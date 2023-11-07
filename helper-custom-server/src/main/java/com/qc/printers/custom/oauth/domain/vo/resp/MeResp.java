package com.qc.printers.custom.oauth.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MeResp implements Serializable {
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("open_id")
    private String openId;

    /**
     * 当异常时才会返回以下字段
     */
    private Integer code;

    private String msg;
}
