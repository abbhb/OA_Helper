package com.qc.printers.common.chat.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatMemberWListResp implements Serializable {




    private Integer index;// 全局的排序，方便前端去解析
    // 块名称，例如群聊 A, B
    private String key;

    /**
     * 对应前端模板
     * 1：新的朋友，暂时本系统无此块
     * 2：群聊
     * 3：普通单聊
     */
    private Integer type;


    /**
     * 公共抽象，保障各个字段都有
     */
    private List<ChatMemberWResp> info;


}
