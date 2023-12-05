package com.qc.printers.custom.common.service;

import org.springframework.ui.Model;

public interface ThymeleafService {

    String reNewPassword(String oneTimeCode, Model model);

}
