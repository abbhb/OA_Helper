package com.qc.printers.common.print.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PreUploadPrintFileReq implements Serializable {

    private String hash;

    private String originFileName;
}
