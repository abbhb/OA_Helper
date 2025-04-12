package com.qc.printers.common.ikuai.domain.vo.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class IkuaiAllowLinkQuery implements Serializable {
    private Integer pageNum;
    private Integer pageSize;
}
