package com.qc.printers.common.activiti.service.impl;


import com.alibaba.fastjson.JSON;
import com.qc.printers.common.activiti.constant.ColumnKeyType;
import com.qc.printers.common.activiti.mapper.TableMapper;
import com.qc.printers.common.activiti.entity.NodeColumnItem;
import com.qc.printers.common.activiti.entity.TableColumns;
import com.qc.printers.common.activiti.entity.TableInfo;
import com.qc.printers.common.activiti.entity.dto.workflow.TableInfoDto;
import com.qc.printers.common.activiti.service.TableService;
import com.qc.printers.common.activiti.utils.PropertiesUtils;
import com.qc.printers.common.common.CustomException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库表
 *
 * @author liuguofeng
 * @date 2023/12/13 09:52
 **/
@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private TableMapper tableMapper;


    /**
     * 获取组件类型
     *
     * @return 结果
     */
    @Override
    public Map<String, Object> getWidgetDataType() {
        Map<String, Object> map = new HashMap<>();
        map.put("widgetDataType", PropertiesUtils.widgetDataType);
        map.put("widgetDefaultDataType", PropertiesUtils.defaultWidgetDataType);
        return map;
    }

    /**
     * 数据库表结构
     *
     * @param tableName 表名称或表备注
     * @return 表结构信息
     */
    @Override
    public List<TableInfo> tableList(String tableName) {
        return tableMapper.tableList(tableName);
    }

    /**
     * 数据库表结构
     *
     * @param tableName 表名称或表备注
     * @param columnKey 行键
     * @return 表结构信息
     */
    @Override
    public List<TableColumns> tableColumns(String tableName, String columnKey) {
        return tableMapper.tableColumns(tableName, columnKey);
    }

    /**
     * 创建表结构
     *
     * @param tableInfo 表信息
     */
    @Override
    public void createTable(TableInfoDto tableInfo) {
        // 查询表是否存在
        List<TableInfo> tableInfos = tableList(tableInfo.getTableName());
        // 如果存在就返回
        if (tableInfos.size() != 0) return;

        tableMapper.createTable(tableInfo.getTableName(),
                tableInfo.getTableComment(),
                tableInfo.getTableName() + "_id",
                tableInfo.getColumns());
    }

    /**
     * 保存或更新数据
     *
     * @param id        主键id
     * @param tableName 表名
     * @param columns   节点绑定的表字段
     * @param variables 流程变量
     */
    @Override
    public void saveOrUpdateData(String id, String tableName, List<NodeColumnItem> columns, Map<String, Object> variables) {
        if (columns.size() == 0) return;
        TableColumns primaryColumns = tableColumns(tableName, ColumnKeyType.PRIMARY_KEY)
                .stream().findAny().orElse(null);
        if (primaryColumns == null) throw new CustomException("未找到表主键！");
        // 如果没有设置主表单或者主表单没有绑定字段就只插入id
        Map<String, Object> listData = new HashMap<>();
        listData.put(primaryColumns.getColumnName(), id);
        // 找到流程变量的数据,把数据更新到表中
        for (NodeColumnItem column : columns) {
            String columnName = column.getColumnName();
            Object value = variables.get(columnName);
            if (value == null) continue;
            // 如果是list类型,转成字符串保存到数据库
            if (value instanceof List) {
                value = JSON.toJSONString(value);
            }
            listData.put(columnName, value);
        }
        long exist = tableMapper.exist(primaryColumns.getColumnName(), id, tableName);
        if (exist > 0) {
            tableMapper.updateDataById(primaryColumns.getColumnName(), id, tableName, listData);
        } else {
            tableMapper.insertData(tableName, listData);
        }
    }


}
