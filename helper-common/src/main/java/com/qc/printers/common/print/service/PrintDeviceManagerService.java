package com.qc.printers.common.print.service;


import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.print.domain.dto.PrintDeviceUserDto;
import com.qc.printers.common.print.domain.vo.PrintDeviceNotRegisterVO;
import com.qc.printers.common.print.domain.vo.request.CreatePrintDeviceReq;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceUserQuery;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;

import java.util.List;

public interface PrintDeviceManagerService {
    List<PrintDeviceNotRegisterVO> getUnRegisterPrintDeviceList();

    String createPrintDevice(CreatePrintDeviceReq data);

    String deletePrintDevice(String id);

    String updatePrintDeviceStatus(UpdatePrintDeviceStatusReq data);

    PageData<PrintDeviceUserDto> getPrintDeviceUsers(PrintDeviceUserQuery params);
}
