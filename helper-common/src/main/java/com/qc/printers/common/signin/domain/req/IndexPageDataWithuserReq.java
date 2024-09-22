package com.qc.printers.common.signin.domain.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class IndexPageDataWithuserReq implements Serializable {


    @NotNull(message = "分页参数不能为空")
    private Integer pageNum;

    @NotNull(message = "分页参数不能为空")
    private Integer pageSize;

    /**
     * 筛选
     */
    private Integer state;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate start;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate end;
}
