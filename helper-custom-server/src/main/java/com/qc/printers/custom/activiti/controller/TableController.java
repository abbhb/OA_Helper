package com.qc.printers.custom.activiti.controller;

import com.qc.printers.common.activiti.entity.TableColumns;
import com.qc.printers.common.activiti.entity.TableInfo;
import com.qc.printers.common.activiti.service.TableService;
import com.qc.printers.common.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RequestMapping("table")
@RestController
public class TableController {

    @Autowired
    private TableService tableService;

    /**
     * 获取组件类型
     * 仅支持MYSQL
     *
     * @return 结果
     */
    @GetMapping("getWidgetDataType")
    public R<Map<String, Object>> getWidgetDataType() {
        return R.success(tableService.getWidgetDataType());
    }

    /**
     * 表名称
     *
     * @param tableName 数据库表信息
     * @return 结果
     */
    @GetMapping("list")
    public R<List<TableInfo>> list(String tableName) {
        List<TableInfo> list = tableService.tableList(tableName);
        return R.success(list);
    }

    /**
     * 数据库表结构
     *
     * @param tableName 表名称或表备注
     * @return 表结构信息
     */
    @GetMapping("tableColumns")
    public R<List<TableColumns>> tableColumns(@RequestParam String tableName,
                                              @RequestParam(required = false) String columnKey) {
        List<TableColumns> columns = tableService.tableColumns(tableName, columnKey);
        return R.success(columns);
    }


}
