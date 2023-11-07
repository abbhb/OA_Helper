package com.qc.printers.custom.oauth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CanAuthorize implements Serializable {
    private boolean isCan;
    private String msg;
}
