package com.qc.printers.common.chat.domain.entity.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.qc.printers.common.common.utils.discover.domain.UrlInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Description: 消息扩展属性
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-05-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageExtra implements Serializable {
    private static final long serialVersionUID = 1L;
    //url跳转链接
    private Map<String, UrlInfo> urlContentMap;
    //消息撤回详情
    private MsgRecall recall;
    //艾特的uid
    private Set<String> atUidList;
    //文件消息
    private FileMsgDTO fileMsg;
    //图片消息
    private ImgMsgDTO imgMsgDTO;
    //语音消息
    private SoundMsgDTO soundMsgDTO;
    //文件消息
    private VideoMsgDTO videoMsgDTO;

    /**
     * 表情图片信息
     */
    private EmojisMsgDTO emojisMsgDTO;
}
