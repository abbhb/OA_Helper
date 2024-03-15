package com.qc.printers.common.activiti.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeployGroupAct implements Serializable {
    private Long id;

    private String deployId;

    private Long deployGroupId;
}
