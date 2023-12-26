package com.qc.printers.custom.notice.domain.vo.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeMangerListResp implements Serializable {

    private Long id;

    private String title;

    //列表没必要携带内容，内容在进编辑再获取
//    private String content;

    private Integer status;

    /**
     * 类型
     * 1为内容模式
     * 2为url模式
     */
    private Integer type;

    /**
     * 正常是不需要的，但是后来加的URL模式需要
     */
    private String content;

    private Integer amount;

    //是否携带了附件
    private Integer isAnnex;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

//    private Long createUser;

    private String createUserName;

//    private Long updateUser;

    private String updateUserName;

    private String tag;

    //若已发布，则携带发布人信息
//    private Long releaseUser;

    /**
     * 可能用户已经改名了，此处只记录发布该通知时用户的昵称,可以再通过接口去尝试更新改昵称
     */
    private String releaseUserName;

    private LocalDateTime releaseTime;

//    private Long releaseDept;

    private String releaseDeptName;

    /**
     * 紧要程度
     */
    private Integer urgency;

    private Integer version;

    private Integer visibility;

    /**
     * visibility为仅部门可见才存在
     */
    private List<Long> deptIds;
}
