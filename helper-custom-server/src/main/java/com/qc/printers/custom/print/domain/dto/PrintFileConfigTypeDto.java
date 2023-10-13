package com.qc.printers.custom.print.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @EqualsAndHashCode
 * @ToString 用来解决子类log.info无法打印父类属性的问题
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor//不加这个是没有有参构造的
public class PrintFileConfigTypeDto extends PrintDataHandlerDto {

}
