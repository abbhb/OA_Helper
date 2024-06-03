package com.qc.printers.custom.signin.domain.req;

import com.qc.printers.custom.signin.domain.vo.SigninUserCardDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SigninUserCardDataReq implements Serializable {

    private List<SigninUserCardDataResp> data;

    private String syncModel;
    private String deviceId;
}
