package com.qc.printers.common.user.domain.vo.response.ws;

import com.qc.printers.common.signin.domain.resp.AddLogExtInfo;
import com.qc.printers.common.user.domain.enums.WSBaseResp;
import com.qc.printers.common.user.domain.enums.WSRespTypeEnum;
import lombok.*;

/**
 * 大屏推送服务
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WSSigninPush extends AddLogExtInfo {

    String studentId;

    String name;

    public WSSigninPush(String avatarUrl, String deptName, String studentId, String name) {
        super(avatarUrl, deptName);
        this.studentId = studentId;
        this.name = name;
    }

    public WSSigninPush(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }

}
