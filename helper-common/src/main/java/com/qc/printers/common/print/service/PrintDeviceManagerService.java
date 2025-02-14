package com.qc.printers.common.print.service;


import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.print.domain.dto.PrintDeviceLinkDto;
import com.qc.printers.common.print.domain.vo.PrintDeviceNotRegisterVO;
import com.qc.printers.common.print.domain.vo.request.CreatePrintDeviceReq;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceLinkQuery;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceLinkReq;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;
import com.qc.printers.common.print.domain.vo.response.PrintDeviceVO;

import java.util.List;
import java.util.Map;

public interface PrintDeviceManagerService {
    List<PrintDeviceNotRegisterVO> getUnRegisterPrintDeviceList();

    String createPrintDevice(CreatePrintDeviceReq data);

    String deletePrintDevice(String id);

    String updatePrintDeviceStatus(UpdatePrintDeviceStatusReq data);

    PageData<PrintDeviceLinkDto> getPrintDeviceLinks(PrintDeviceLinkQuery params);

    // 返回一个已选择用户id列表,用于添加设备过滤
    Map<Integer,List<Long>> getPrintDeviceLinkIds(Long printDeviceId);

    String addPrintDeviceLinks(PrintDeviceLinkReq data);

    String removePrintDeviceLink(String printDeviceId, String linkId,Integer linkType);

    String updatePrintDeviceLinkRole(PrintDeviceLinkReq data);

    List<PrintDeviceVO> getPrintDeviceList();
}
