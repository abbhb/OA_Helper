package com.qc.printers.common.ikuai.domain.vo.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class IkuaiAllowLinkReq implements Serializable {
    private List<String> linkIds;
    private Integer linkType;
}
