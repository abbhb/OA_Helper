package com.qc.printers.custom.user.domain.vo.request;

import com.qc.printers.custom.user.domain.vo.response.app.AppState;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserFrontConfigReq implements Serializable {
    private AppState appState;
}
