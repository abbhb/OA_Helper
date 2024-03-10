package com.qc.printers.common.activiti.entity.vo.workflow;

import lombok.Data;

/**
 * 表结构
 *
 * @author liuguofeng
 * @date 2023/12/13 10:23
 **/
@Data
public class TableColumnsVo {

    /**
     * 行名称
     */
    private String columnName;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 行备注
     */
    private String columnComment;
    /**
     * 行健
     */
    private String columnKey;

}
