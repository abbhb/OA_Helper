package com.qc.printers.custom.test.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestJson implements Serializable {
    Long id;
    String name;
    String url;
    String message;
}
