package com.qc.printers.custom.notice.domain.vo.resp;

import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeDept;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 添加通知后的返回，携带id
 */
@Data
public class NoticeAddResp implements Serializable {
    private Notice notice;

    private List<NoticeDept> noticeDepts;


}
