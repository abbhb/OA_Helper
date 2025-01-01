package com.qc.printers.common.print.domain.vo;

import com.qc.printers.common.print.domain.vo.response.PrintDeviceResp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 这里其实一致的，未注册打印设备
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PrintDeviceNotRegisterVO extends PrintDeviceResp {

}
