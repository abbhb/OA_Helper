package com.qc.printers.custom.print.service.strategy.fileconfig;

import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

/**
 * 文件配置根据状态type不同的策略
 */
public interface FileConfigTypeRStrategy {
    PrinterBaseResp<PrintFileConfigResp> buildResp(PrintFileConfigTypeDto printFileConfigTypeDto);
}
