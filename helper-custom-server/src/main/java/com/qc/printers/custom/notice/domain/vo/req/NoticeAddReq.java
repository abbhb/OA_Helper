package com.qc.printers.custom.notice.domain.vo.req;

import com.qc.printers.common.notice.domain.entity.Notice;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NoticeAddReq implements Serializable {
    private Notice notice;

    /**
     * 首次添加且限制了某些部门可见时，需要传入部门id列表
     */
    private List<Long> deptIds;
}
