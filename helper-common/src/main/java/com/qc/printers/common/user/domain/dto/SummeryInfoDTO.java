package com.qc.printers.common.user.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Description: 修改用户名
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummeryInfoDTO {
    @ApiModelProperty(value = "用户id")
    private Long uid;
    @ApiModelProperty(value = "是否需要刷新")
    private Boolean needRefresh = Boolean.TRUE;
    @ApiModelProperty(value = "用户昵称")
    private String name;
    @ApiModelProperty(value = "用户头像")
    private String avatar;
    @ApiModelProperty(value = "归属地")
    private String locPlace;


    public static SummeryInfoDTO skip(Long uid) {
        SummeryInfoDTO dto = new SummeryInfoDTO();
        dto.setUid(uid);
        dto.setNeedRefresh(Boolean.FALSE);
        return dto;
    }
}
