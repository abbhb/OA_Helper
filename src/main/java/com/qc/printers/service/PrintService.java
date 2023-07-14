package com.qc.printers.service;


public interface PrintService {

    boolean printsForPDF(String newName, String oldName, Integer copies,Integer printingDirection,Integer printBigValue,Integer needPrintPagesEndIndex,Integer isDuplex,Long userId);

    boolean printsForWord(String newName,String bakUrl, String originName, Integer copies, Integer printingDirection, Integer printBigValue, Integer needPrintPagesEndIndex,Integer isDuplex,Long userId);
}
