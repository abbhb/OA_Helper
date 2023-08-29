package com.qc.printers.common.picturewall.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class IndexImage implements Serializable {
    private Long id;

    private String label;

    private String image;
}
