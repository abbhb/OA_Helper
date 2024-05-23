package com.qc.printers.common.signin.domain.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class FaceFileResp implements Serializable {
    private String url;

    private String name;
}
