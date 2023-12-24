package com.qc.printers.custom.notice.domain.vo.req;

import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通知更新请求实体类
 */
@Data
public class NoticeUpdateReq implements Serializable {
    private Notice notice;

    // 可见部门列表为基础信息不在此接口，所以此处与创建有区别
    // 附件列表，如果有
    private List<NoticeAnnex> annexes;

    /**
     * 限制了某些部门可见时，需要传入部门id列表,为空时就是都不可见，通知列表里管理员也不可见
     */
    private List<Long> deptIds;
}
