package com.qc.printers.custom.notice.domain.vo.resp;

import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.domain.entity.NoticeDept;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 包含跟通知相关的所有信息
 */
@Data
public class NoticeViewResp implements Serializable {
    /**
     * 通知基本信息
     */
    private Notice notice;

    /**
     * 部门列表
     */
    private List<NoticeDept> noticeDepts;

    /**
     * 附件列表
     */
    private List<NoticeAnnex> noticeAnnexes;

}
