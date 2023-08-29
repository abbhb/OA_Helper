package com.qc.printers.custom.study.domain.dto;


import com.qc.printers.common.study.domain.entity.StudyClock;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AddClock30DTO extends StudyClock {
    private String sign;
}
