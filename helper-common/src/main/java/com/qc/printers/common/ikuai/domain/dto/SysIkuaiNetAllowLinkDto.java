package com.qc.printers.common.ikuai.domain.dto;

import com.qc.printers.common.ikuai.domain.entity.SysIkuaiNetAllow;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class SysIkuaiNetAllowLinkDto extends SysIkuaiNetAllow implements Serializable {
    private String linkName;
}
