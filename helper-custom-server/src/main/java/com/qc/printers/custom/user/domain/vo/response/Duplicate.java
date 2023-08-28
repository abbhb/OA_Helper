package com.qc.printers.custom.user.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 是否重名
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Duplicate implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isDuplicate;

    private String why;

    private List<String> suggestion;
}
