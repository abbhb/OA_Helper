package com.qc.printers.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class GroupTree implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;

    private Long key;//id of the group,未分组组id为0L

    private List<GroupTreeNode> children;

}