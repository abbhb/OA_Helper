package com.qc.printers.common.signin.domain.dto;

import com.qc.printers.common.common.annotation.Excel;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDataImportErrorDto implements Serializable {

    @Excel(name = "失败ID", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "失败ID")
    private Long userId;

    @Excel(name = "失败原因", cellType = Excel.ColumnType.STRING, type = Excel.Type.EXPORT,prompt = "失败原因")
    private String error;
}
