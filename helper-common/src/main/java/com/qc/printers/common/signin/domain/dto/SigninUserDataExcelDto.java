package com.qc.printers.common.signin.domain.dto;

import com.qc.printers.common.common.annotation.Excel;
import com.qc.printers.common.signin.domain.entity.SigninUserData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SigninUserDataExcelDto extends SigninUserData implements Serializable {

    @Excel(name = "用户名称", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "用户名称")
    private String name;

    @Excel(name = "部门名称", cellType = Excel.ColumnType.STRING,type = Excel.Type.EXPORT,prompt = "部门名称")
    private String deptName;
}
