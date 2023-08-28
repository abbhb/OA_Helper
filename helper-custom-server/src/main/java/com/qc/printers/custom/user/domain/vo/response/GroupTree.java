package com.qc.printers.custom.user.domain.vo.response;

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