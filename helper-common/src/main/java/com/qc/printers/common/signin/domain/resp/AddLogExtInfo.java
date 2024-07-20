package com.qc.printers.common.signin.domain.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddLogExtInfo implements Serializable {

    private String avatarUrl;

    /**
     * 完整部门名称-分割
     */
    private String deptName;


}
