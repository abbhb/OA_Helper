package com.qc.printers.custom.print.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 缩略图轮询的返回
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PrintImageResp implements Serializable {
    //打印文件的id
    private Long id;

    //完整url
    private String imgUrl;
}
