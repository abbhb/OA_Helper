package com.qc.printers.custom.print.service;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.ValueLabelResult;
import com.qc.printers.common.print.domain.vo.CountTop10VO;
import com.qc.printers.custom.print.domain.vo.PrinterResult;
import com.qc.printers.custom.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.custom.print.domain.vo.response.PrintImageResp;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 专用于记录
 */
public interface PrinterService {

    R<PageData<PrinterResult>> listPrinter(Integer pageNum, Integer pageSize, String name, String date);

    R<PageData<PrinterResult>> listAllPrinter(Integer pageNum, Integer pageSize, String name, String user);

    R<List<ValueLabelResult>> getAllUserPrinter();

    R<List<CountTop10VO>> getUserPrintTopList(Integer type);

    R<Integer> getTodayPrintCount();

    R<Integer> addPrinterLog(MultipartFile file, Integer pageStart, Integer pageEnd, Integer copiesNum, String username, Integer duplex, String fileName);


    String uploadPrintFile(MultipartFile file);

    PrinterBaseResp<PrintImageResp> thumbnailPolling(Long id);

    PrinterBaseResp<PrintFileConfigResp> fileConfigurationPolling(Long id);
}
