package com.qc.printers.custom.activiti.pojo.dto;

import com.qc.printers.common.activiti.entity.vo.workflow.DefinitionListVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeployGroupActDto implements Serializable {

    private Long deployGroupId;

    private String deployGroupName;

    private Integer sort;

    private List<DefinitionListVo> definitionListVoList;
}
