package com.qc.printers.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ClockSelfEchartsVO {
    private List<String> xTextList;

    private List<Double> xValueList;
}
