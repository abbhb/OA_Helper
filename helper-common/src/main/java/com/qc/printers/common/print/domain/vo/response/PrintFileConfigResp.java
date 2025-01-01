package com.qc.printers.common.print.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件配置轮询具体内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrintFileConfigResp implements Serializable {
    //打印文件的id
    private Long id;

    //文件起始页
    private Integer firstPage;

    //文件的终止页
    private Integer lastPage;

    //文件的名称
    private String fileName;

}
