package com.qc.printers.custom.signin.domain.req;

import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SigninUserFaceDataReq implements Serializable {
    private List<SigninUserFaceDataResp> data;

    private String syncModel;
    private String deviceId;
}
