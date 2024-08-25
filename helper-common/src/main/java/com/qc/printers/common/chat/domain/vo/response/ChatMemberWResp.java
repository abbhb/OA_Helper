package com.qc.printers.common.chat.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMemberWResp implements Serializable {


    /**
     * 业务id，type为1此处可以为null，新的朋友只是为了加载板块，群聊为群聊的房间id，单聊为对方的的uid
     */
    private Long id;

    /**
     * 备注名
     */
    private String remarkName;

    /**
     * 会话名
     */
    private String name;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 对应前端模板
     * 1：新的朋友，暂时本系统无此块
     * 2：群聊
     * 3：普通单聊
     */
    private Integer type;

    /**
     * 其他不是公共的部分动态返回
     * USER:activeStatus在线状态
     */
    private Map<String,Object> ext;


}
