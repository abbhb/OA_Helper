package com.qc.printers.custom.user.service.strategy;

import com.qc.printers.custom.user.domain.enums.ThirdLoginEnum;
import com.qc.printers.custom.user.utils.UniquekerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public class QQLoginHandel extends ThirdLoginHandel {
    @Override
    ThirdLoginEnum getDataTypeEnum() {
        return ThirdLoginEnum.QQ;
    }

    @Override
    public String thirdLoginHandel(HttpServletRequest request, HttpServletResponse response) {
        //            String serverName = request.getServerName();
//            int serverPort = request.getServerPort();
        String forwardLoginUrl = UniquekerUtil.getForwardLoginUrl(getDataTypeEnum().getType());

//            response.sendRedirect(forwardLoginUrl);
        return forwardLoginUrl;
    }
}
