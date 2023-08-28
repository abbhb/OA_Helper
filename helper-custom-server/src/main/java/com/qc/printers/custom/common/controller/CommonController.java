package com.qc.printers.custom.common.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.utils.RSAUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/common")
@Api("公共接口")
public class CommonController {
    @Autowired
    private CommonService commonService;
    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/uploadimage")
    @ApiOperation("上传图像到minio上，返回url,启用了跨域")
    public R<String> uploadImage(MultipartFile file){
        return commonService.uploadFileTOMinio(file);

    }

    //此接口后期需要优化，存日志时顺便写一下redis，然后从redis中取
    @CrossOrigin("*")
    @GetMapping("/api_count")
    @ApiOperation(value = "日总请求数",notes = "")
    public R<Integer> userCount(){
        log.info("获取日总请求数");
        Integer integer = commonService.countApi();
        return R.success(integer);
    }

    @CrossOrigin("*")
    @GetMapping("/api_count_lastday")
    @ApiOperation(value = "昨日请求数", notes = "")
    public R<Integer> userCountToday() {
        log.info("获取昨日请求数");
        Integer integer = commonService.apiCountLastday();
        if (integer == null) {
            return R.success(0);
        }
        return R.success(integer);
    }

    @CrossOrigin("*")
    @GetMapping("/get_all_image_url")
    @ApiOperation(value = "获取完整的图片url地址", notes = "")
    public R<String> getAllImageUrl(String key) {
        log.info("获取完整的图片url地址");
        if (StringUtils.isEmpty(key)) {
            return R.error("无法获取");
        }
        String imageUrl = commonService.getAllImageUrl(key);
        return R.successOnlyObject(imageUrl);
    }

    @CrossOrigin("*")
    @GetMapping("/get_public_key")
    @ApiOperation(value = "获取RSA公钥", notes = "")
    public R<String> getPublicKey() {
        log.info("获取RSA公钥");
        return R.successOnlyObject(RSAUtil.publicKey);
    }

}
