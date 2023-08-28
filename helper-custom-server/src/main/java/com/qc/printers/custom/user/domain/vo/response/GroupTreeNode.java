package com.qc.printers.custom.user.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupTreeNode implements Serializable {
    private Long key;//id of user

    private String title;//name of user
}
