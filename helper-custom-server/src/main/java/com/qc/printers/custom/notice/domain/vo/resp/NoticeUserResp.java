package com.qc.printers.custom.notice.domain.vo.resp;

import com.qc.printers.common.notice.domain.entity.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NoticeUserResp extends Notice implements Serializable {
    private boolean userRead;
}
