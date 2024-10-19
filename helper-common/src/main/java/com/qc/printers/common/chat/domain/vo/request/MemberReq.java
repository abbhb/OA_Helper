package com.qc.printers.common.chat.domain.vo.request;

import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-07-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberReq extends CursorPageBaseReq {
    @ApiModelProperty("房间号")
    private Long roomId = 1L;

    /**
     * 支持多字段模糊匹配
     * 存在才启用
     */
    @ApiModelProperty("万能搜索")
    private String search;
}
