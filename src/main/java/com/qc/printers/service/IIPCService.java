package com.qc.printers.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IIPCService {
    /**
     * 打开一个流地址
     *
     * @param url
     * @param response
     */
    public void open(String url, HttpServletResponse response, HttpServletRequest request);
}
