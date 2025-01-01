package com.qc.printers.custom.print.service.strategy;

import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.custom.print.domain.enums.PrintDataRespTypeEnum;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

import javax.annotation.PostConstruct;

/**
 * Description: 打印数据处理器抽象类
 */
public abstract class AbstratePrintDataHandler {
    @PostConstruct
    private void init() {
        //实现类都会继承该抽象模板，都会注册为组件，在注册时在工厂注册
        PrintDataHandlerFactory.register(getDataTypeEnum().getType(), this);
    }

    /**
     * 数据的类型
     */
    abstract PrintDataRespTypeEnum getDataTypeEnum();

    /**
     * 生成返回数据
     * 返回数据都基于PrinterBaseResp<T></>
     */
    public abstract PrinterBaseResp createR(PrinterRedis printerRedis);

}
