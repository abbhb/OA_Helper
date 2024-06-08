package com.qc.printers.custom.picturewall.domain.vo;

import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class IndexImageAddReq implements Serializable {
    private IndexImage indexImage;
    /**
     * 首次添加且限制了某些部门可见时，需要传入部门id列表
     */
    private List<Long> deptIds;
}
