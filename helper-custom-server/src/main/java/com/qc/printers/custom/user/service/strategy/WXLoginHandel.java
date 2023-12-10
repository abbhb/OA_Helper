package com.qc.printers.custom.user.service.strategy;

import com.qc.printers.custom.user.domain.enums.ThirdLoginEnum;
import com.qc.printers.custom.user.utils.UniquekerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@Slf4j
public class WXLoginHandel extends ThirdLoginHandel {
    @Override
    ThirdLoginEnum getDataTypeEnum() {
        return ThirdLoginEnum.WECHAT;
    }

    @Override
    public void thirdLoginHandel(HttpServletRequest request, HttpServletResponse response) {
        try {
//            String serverName = request.getServerName();
//            int serverPort = request.getServerPort();
            String forwardLoginUrl = UniquekerUtil.getForwardLoginUrl(getDataTypeEnum().getType());
            response.sendRedirect(forwardLoginUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
