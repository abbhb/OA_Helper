package com.qc.printers.common.contentpromotion.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocNotificationDept implements Serializable {

    private Long id;

    private Long docNotificationId;

    private Long deptId;
}
