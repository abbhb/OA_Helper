package com.qc.printers.custom.common.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadFileResp implements Serializable {
    String url;

    String name;
}
