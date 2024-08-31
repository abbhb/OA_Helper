package com.qc.printers.common.signin.domain.dto;

import com.qc.printers.common.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDataImportErrorDto implements Serializable {

    @Excel(name = "失败ID", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "失败ID")
    private Long userId;

    @Excel(name = "昵称", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "昵称")
    private String name;

    @Excel(name = "学号", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "学号")
    private String studentId;

    @Excel(name = "部门编号", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "部门编号")
    private String deptId;

    @Excel(name = "电子邮箱", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "电子邮箱")
    private String email;

    @Excel(name = "失败原因", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "失败原因")
    private String error;
}
