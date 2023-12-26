package com.qc.printers.custom.notice.domain.vo.resp;

import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NoticeUserReadResp implements Serializable {
    private Notice notice;

    private List<NoticeAnnex> noticeAnnexes;
}
