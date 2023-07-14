package com.qc.printers.pojo.dto;

import com.qc.printers.pojo.StudyClock;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AddClock30DTO extends StudyClock {
    private String sign;
}
