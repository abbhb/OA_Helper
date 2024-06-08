package com.qc.printers.common.picturewall.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class IndexImageData implements Serializable {
    private String image;

    private String extra;

}
