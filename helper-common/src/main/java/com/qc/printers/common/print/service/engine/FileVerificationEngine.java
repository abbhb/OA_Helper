package com.qc.printers.common.print.service.engine;

import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.enums.MessageTypeEnum;
import com.qc.printers.common.chat.domain.vo.request.ChatMessageReq;
import com.qc.printers.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.FileSizeUtil;
import com.qc.printers.common.print.domain.enums.FileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;

public abstract class FileVerificationEngine {
    public final String supportFileExt = "pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,jpeg,png,bmp";
    @PostConstruct
    private void init() {
        //实现类都会继承该抽象模板，都会注册为组件，在注册时在工厂注册
        FileVerificationEngineFactory.register(getFileTypeEnum().getType(), this);
    }

    /**
     * 文件类型
     */
    protected abstract FileTypeEnum getFileTypeEnum();

    private boolean pubCheck(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.contains("\\?") || originalFilename.contains("？")) {
            throw new CustomException("文件名里不允许包含？请修改后在打印");
        }
        // 判断文件拓展名是否再支持的列表里
        if (!supportFileExt.contains(originalFilename.substring(originalFilename.lastIndexOf(".") + 1))) {
            throw new CustomException(file.getOriginalFilename() + ",不支持该文件，请先转成pdf!");
        }
        if (file.isEmpty()) {
            throw new CustomException("文件为空");
        }
        FileSizeUtil.check(file);// 文件不能超过大小
        return true;
    }


    /**
     * 校验方法具体实现
     */
     protected abstract boolean checkWay(MultipartFile file);

     public void check(MultipartFile file){
         if (!pubCheck(file))throw new CustomException("文件公共校验不通过");
         if (!checkWay(file))throw new CustomException("文件专属校验不通过");
     }

}
