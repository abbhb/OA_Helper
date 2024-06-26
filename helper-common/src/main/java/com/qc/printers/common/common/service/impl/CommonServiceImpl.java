package com.qc.printers.common.common.service.impl;


import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.ToEmail;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.apiCount.ApiCount;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.config.MinIoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
    @Autowired
    MinIoProperties minIoProperties;


    public R<String> uploadFileTOMinio(MultipartFile file) {
        try {
            String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
            log.info("imageUrl={}",fileUrl);
//            String[] split = fileUrl.split("\\?");

            return R.successOnlyObject(OssDBUtil.toDBUrl(fileUrl));
        }catch (Exception e){
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public String getImageUrl(String imageKey) {
        if (imageKey.contains("http")){
            return imageKey;
        }
        return minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+imageKey;
    }

    @Override
    public R<String> sendEmailCode(ToEmail toEmail) {
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setFrom("3482238110@qq.com");
//
//        message.setTo(toEmail.getTos());
//
//        message.setSubject("您本次的验证码是");
//
//        String verCode = VerCodeGenerateUtil.generateVerCode();
//        //需要同时存入redis,key使用emailcode:userid
//        Claim id = null;
//        try {
//            DecodedJWT decodedJWT = JWTUtil.deToken(token);
//            id = decodedJWT.getClaim("id");
//
//        }catch (Exception e){
//            return R.error(Code.DEL_TOKEN,"登陆过期");
//        }
//        User byId = userService.getById(Long.valueOf(id.asString()));
//
//        if (byId==null){
//            return R.error("err");
//        }
//        message.setText("尊敬的"+byId.getName()
//                +",您好:\n"
//                + "\n本次请求的邮件验证码为:" + verCode + ",本验证码 5 分钟内效，请及时输入。（请勿泄露此验证码）\n"
//                + "\n如非本人操作，请忽略该邮件。\n(这是一封通过自动发送的邮件，请不要直接回复）");
//
//        if (id==null){
//            return R.error("登陆过期");
//        }
//        mailSender.send(message);
//        iRedisService.setTokenWithTime("emailcode:"+id.asString(),verCode, 300L);
        return R.success("业务暂停");
    }

    @Override
    public Integer countApi() {
//      获取日志表当日请求数
        return ApiCount.getApiCount();
    }

    @Override
    public Integer apiCountLastday() {
        Integer lastDayCountApi = ApiCount.getLastDayCountApi();
        if (lastDayCountApi != null) {
            return lastDayCountApi;
        }
        return 0;
    }

    @Override
    public String getAllImageUrl(String key) {

        return OssDBUtil.toUseUrl(key);
    }
}