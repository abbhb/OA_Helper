package com.qc.printers.common.activiti.mapper;


import com.qc.printers.common.activiti.entity.TableColumns;
import com.qc.printers.common.activiti.entity.TableInfo;
import com.qc.printers.common.activiti.entity.dto.workflow.TableColumnsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 表结构
 *
 * @author liuguofeng
 * @date 2023/12/13 10:16
 **/
@Mapper
public interface TableMapper {

    /**
     * 数据库表信息
     *
     * @param tableName 表名称或表备注
     * @return 表信息
     */
    List<TableInfo> tableList(String tableName);

    /**
     * 数据库表结构
     *
     * @param tableName 表名称或表备注
     * @param columnKey 行键
     * @return 表结构信息
     */
    List<TableColumns> tableColumns(@Param("tableName") String tableName,
                                    @Param("columnKey") String columnKey);

    /**
     * 创建表
     *
     * @param tableName    表名
     * @param tableComment 表备注
     * @param idName       主键名
     * @param columns      字段信息
     */
    void createTable(@Param("tableName") String tableName,
                     @Param("tableComment") String tableComment,
                     @Param("idName") String idName,
                     @Param("columns") List<TableColumnsDto> columns);


    /**
     * 查询数据是否存在
     *
     * @param idName    主键名称
     * @param idValue   主键值
     * @param tableName 表名
     * @return 0:不存在,>0:存在
     */
    long exist(@Param("idName") String idName,
               @Param("idValue") Object idValue,
               @Param("tableName") String tableName);

    /**
     * 根据表名列名插入数据
     *
     * @param tableName 表名
     * @param listData  表中数据(key=字段名,value=值)
     * @return 结果
     */
    Integer insertData(@Param("tableName") String tableName,
                       @Param("listData") Map<String, Object> listData);

    /**
     * 根据表名列名插入数据
     *
     * @param idName    主键名称
     * @param idValue   主键值
     * @param tableName 表名
     * @param listData  表中数据(key=字段名,value=值)
     * @return 结果
     */
    Integer updateDataById(@Param("idName") String idName,
                           @Param("idValue") Object idValue,
                           @Param("tableName") String tableName,
                           @Param("listData") Map<String, Object> listData);


}
