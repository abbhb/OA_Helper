package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.R;
import com.qc.printers.pojo.PrinterResult;
import com.qc.printers.pojo.UserResult;
import com.qc.printers.pojo.ValueLabelResult;
import com.qc.printers.pojo.entity.PageData;
import com.qc.printers.pojo.entity.Printer;

import java.util.List;

/**
 * 专用于记录
 */
public interface PrinterService extends IService<Printer> {

    boolean addPrinter(Printer printer,String urlName);

    R<PageData<PrinterResult>> listPrinter(Integer pageNum, Integer pageSize, String token, String name, String date);

    R<PageData<PrinterResult>> listAllPrinter(Integer pageNum, Integer pageSize, String name, String user);

    R<List<ValueLabelResult>> getAllUserPrinter();

    R<List<UserResult>> getUserPrintTopList();
}
