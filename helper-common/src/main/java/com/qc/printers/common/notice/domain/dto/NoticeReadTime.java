package com.qc.printers.common.notice.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 仅支持同时存在一个预览密码
 */
@Data
public class NoticeReadTime implements Serializable {
    private String password;
}
