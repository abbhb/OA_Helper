package com.qc.printers.custom.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.ValueLabelResult;
import com.qc.printers.common.common.event.print.FileToPDFEvent;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.domain.OssReq;
import com.qc.printers.common.common.utils.oss.domain.OssResp;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.vo.CountTop10VO;
import com.qc.printers.common.print.mapper.PrinterMapper;
import com.qc.printers.common.print.service.IPrinterService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.mapper.UserMapper;
import com.qc.printers.common.user.service.IUserService;
import com.qc.printers.custom.print.domain.vo.PrinterResult;
import com.qc.printers.custom.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.custom.print.domain.vo.response.PrintImageResp;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;
import com.qc.printers.custom.print.service.PrinterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PrinterServiceImpl implements PrinterService {

    private final CommonService commonService;
    private final UserMapper userMapper;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private MinIoUtil minIoUtil;

    @Autowired
    private MinIoProperties minIoProperties;
    private final PrinterMapper printerMapper;

    @Autowired
    private IPrinterService iPrinterService;

    @Autowired
    public PrinterServiceImpl(CommonService commonService, UserMapper userMapper, PrinterMapper printerMapper) {
        this.commonService = commonService;
        this.userMapper = userMapper;
        this.printerMapper = printerMapper;
    }



    @Override
    public R<PageData<PrinterResult>> listPrinter(Integer pageNum, Integer pageSize, String name, String date) {
        if (pageNum==null){
            return R.error("传参错误");
        }
        if (pageSize==null){
            return R.error("传参错误");
        }
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser==null){
            return R.error("系统异常");
        }
        Page pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Printer::getCreateTime);
        lambdaQueryWrapper.eq(Printer::getCreateUser,currentUser.getId());
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Printer::getName,name);
        //暂时不支持通过日期模糊查询
        Page page = iPrinterService.page(pageInfo, lambdaQueryWrapper);
        if (page==null){
            return R.error("啥也没有");
        }
        PageData<PrinterResult> pageData = new PageData<>();
        List<PrinterResult> results = new ArrayList<>();
        for (Object printerItem : pageInfo.getRecords()) {
            Printer printerItem1 = (Printer) printerItem;

            PrinterResult printerResult = new PrinterResult();
            printerResult.setName(printerItem1.getName());
            printerResult.setContentHash(printerItem1.getContentHash());
            printerResult.setCreateTime(printerItem1.getCreateTime());
            printerResult.setIsDuplex(printerItem1.getIsDuplex());
            printerResult.setUrl(minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+printerItem1.getUrl());
            printerResult.setCopies(printerItem1.getCopies());
            printerResult.setNeedPrintPagesEndIndex(printerItem1.getNeedPrintPagesEndIndex());
            printerResult.setNeedPrintPagesIndex(printerItem1.getNeedPrintPagesIndex());
            User user1 = userMapper.getUserIncludeDeleted(printerItem1.getCreateUser());
            printerResult.setCreateUser(currentUser.getName());
            printerResult.setOriginFilePages(printerItem1.getOriginFilePages());
            printerResult.setId(String.valueOf(printerItem1.getId()));
            printerResult.setSingleDocumentPaperUsage(printerItem1.getSingleDocumentPaperUsage());

            results.add(printerResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return R.success(pageData);
    }

    @Override
    public R<PageData<PrinterResult>> listAllPrinter(Integer pageNum, Integer pageSize, String name, String user) {
        if (pageNum==null){
            return R.error("传参错误");
        }
        if (pageSize==null){
            return R.error("传参错误");
        }

        Page pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Printer::getCreateTime);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Printer::getName,name);
        lambdaQueryWrapper.eq(!StringUtils.isEmpty(user),Printer::getCreateUser,user);

        //暂时不支持通过日期模糊查询
        Page page = iPrinterService.page(pageInfo, lambdaQueryWrapper);
        if (page==null){
            return R.error("啥也没有");
        }
        PageData<PrinterResult> pageData = new PageData<>();
        List<PrinterResult> results = new ArrayList<>();
        for (Object printerItem : pageInfo.getRecords()) {
            Printer printerItem1 = (Printer) printerItem;

            PrinterResult printerResult = new PrinterResult();
            printerResult.setName(printerItem1.getName());
            printerResult.setContentHash(printerItem1.getContentHash());
            printerResult.setCreateTime(printerItem1.getCreateTime());
            printerResult.setIsDuplex(printerItem1.getIsDuplex());
            printerResult.setCopies(printerItem1.getCopies());
            printerResult.setNeedPrintPagesEndIndex(printerItem1.getNeedPrintPagesEndIndex());
            printerResult.setNeedPrintPagesIndex(printerItem1.getNeedPrintPagesIndex());
            printerResult.setSingleDocumentPaperUsage(printerItem1.getSingleDocumentPaperUsage());
            printerResult.setOriginFilePages(printerItem1.getOriginFilePages());
            printerResult.setUrl(minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+printerItem1.getUrl());
            User user1 = userMapper.getUserIncludeDeleted(printerItem1.getCreateUser());
            if (user1==null){
                printerResult.setCreateUser(String.valueOf(printerItem1.getCreateUser())+"(用户信息已丢失)");
            }else {
                printerResult.setCreateUser(user1.getName());
            }

            printerResult.setCopies(printerItem1.getCopies());
            printerResult.setId(String.valueOf(printerItem1.getId()));
            results.add(printerResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return R.success(pageData);
    }

    @Override
    public R<List<ValueLabelResult>> getAllUserPrinter() {
        List<User> allUserIncludeDeleted = userMapper.getAllUserIncludeDeleted();
        List<ValueLabelResult> valueLabelResults =new ArrayList<>();
        if (valueLabelResults==null){
            return R.error("空");
        }
        for (User user:
                allUserIncludeDeleted) {
            ValueLabelResult valueLabelResult = new ValueLabelResult(String.valueOf(user.getId()),user.getName());
            valueLabelResults.add(valueLabelResult);
        }
        return R.success(valueLabelResults);
    }

    /**
     * 耗时操作，定时任务在每天4：00执行一次
     * @return
     */
    @Override
    public R<List<CountTop10VO>> getUserPrintTopList(Integer type) {
        if (type.equals(1)){
            //需要优化 每天只统计一次
            List<CountTop10VO> countTop10 = printerMapper.getCountTop10();
            if (countTop10==null){
                throw new CustomException("业务异常");
            }
            return R.success(countTop10);
        }else if(type.equals(2)){
            //需要优化 每天只统计一次
            List<CountTop10VO> countTop10 = printerMapper.getCountTop10EveryDay();
            if (countTop10==null){
                throw new CustomException("业务异常");
            }
            return R.success(countTop10);
        }
        return null;

    }

    @Override
    public R<Integer> getTodayPrintCount() {
        Integer todayPrintCount = printerMapper.getPrintCount();
        return R.success(todayPrintCount);
    }

    @Override
    public R<Integer> addPrinterLog(MultipartFile file, Integer pageStart, Integer pageEnd, Integer copiesNum,String username,Integer duplex,String fileName) {
        String originName = file.getOriginalFilename();
        if (originName.contains("\\?") || originName.contains("？")) {
            return R.error("文件名里不允许包含？请修改后在打印");
        }
        boolean isDuplex = !duplex.equals(1);
        User one = userDao.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (one==null){
            throw new CustomException("用户不存在");
        }
        // 先在minio上传一份原始文件,若打印失败可以调用其余方式
        String fileURL = commonService.uploadFileTOMinio(file).getData();
        if (StringUtils.isEmpty(fileURL)) {
            return R.error("文件上传失败");
        }
        // 保存打印记录
        Printer printer = new Printer();
        printer.setCreateTime(LocalDateTime.now());
        printer.setCreateUser(one.getId());
        printer.setUrl(fileURL);
        printer.setIsDuplex(isDuplex?1:0);
        printer.setCopies(copiesNum);
        printer.setNeedPrintPagesEndIndex(pageEnd);
        printer.setNeedPrintPagesIndex(pageStart);
        printer.setName(fileName);
        printer.setSingleDocumentPaperUsage((isDuplex ? (int) Math.ceil((double) (pageEnd - pageStart + 1) / 2.0) : (pageEnd - pageStart + 1)));
        log.info("printer={}", printer);
        boolean save = iPrinterService.save(printer);
        if (!save) {
            throw new CustomException("保存失败");
        }
        return R.success(1);
    }

    //todo:可以通过策略模式或者模板模式优化
    @Transactional
    @Override
    public String uploadPrintFile(MultipartFile file) {
        //先将文件上传到minio，这一步失败直接返回错误
        String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
        if (StringUtils.isEmpty(fileUrl)) {
            throw new CustomException("上传异常");
        }
        log.info("imageUrl={}", fileUrl);
        String[] split = fileUrl.split("\\?");
        String fileKey = split[0].split("/aistudio/")[1];
        //同步数据
        Printer printer = new Printer();
        printer.setIsPrint(0);
        printer.setUrl(fileKey);
        printer.setName(file.getOriginalFilename());
        boolean save = iPrinterService.save(printer);
        if (!save) {
            throw new CustomException("数据同步异常");
        }
        PrinterRedis printerRedis = new PrinterRedis();
        BeanUtils.copyProperties(printer, printerRedis);
        printerRedis.setSTU(1);
        printerRedis.setNeedPrintPagesIndex(1);//从第一页开始
        printerRedis.setPageNums(0);
        OssResp preSignedObjectUrl = minIoUtil.getPreSignedObjectUrl(new OssReq("/temp-image", printer.getName(), printer.getId(), true));
        printerRedis.setImageUploadUrl(preSignedObjectUrl.getUploadUrl());
        printerRedis.setImageDownloadUrl(preSignedObjectUrl.getDownloadUrl());
        printerRedis.setCanGetImage(false);
        RedisUtils.set(MyString.print + printer.getId(), printerRedis);
        //如果不是pdf开始转换，修改为统一进入该事件，是不是pdf处理端区分
        applicationEventPublisher.publishEvent(new FileToPDFEvent(this, printer.getId()));
        return "上传成功，请等待转换!";

    }

    @Override
    public PrinterBaseResp<PrintImageResp> thumbnailPolling(Long id) {
        if (!RedisUtils.hasKey(MyString.print + id)) {
            throw new CustomException("请刷新重试");
        }
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + id, PrinterRedis.class);
        if (printerRedis == null) {
            throw new CustomException("请刷新重试");
        }
        PrinterBaseResp<PrintImageResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        if (printerRedis.getSTU() < 3) {
            printImageRespPrinterBaseResp.setType(0);
            return printImageRespPrinterBaseResp;
        }
        if (!printerRedis.isCanGetImage()) {
            printImageRespPrinterBaseResp.setType(0);
            return printImageRespPrinterBaseResp;
        }
        if (StringUtils.isNotEmpty(printerRedis.getImageDownloadUrl())) {
            printImageRespPrinterBaseResp.setType(1);
            printImageRespPrinterBaseResp.setData(new PrintImageResp(id, printerRedis.getImageDownloadUrl()));
            return printImageRespPrinterBaseResp;
        }
        printImageRespPrinterBaseResp.setType(0);
        return printImageRespPrinterBaseResp;
    }

    @Override
    public PrinterBaseResp<PrintFileConfigResp> fileConfigurationPolling(Long id) {
        if (!RedisUtils.hasKey(MyString.print + id)) {
            throw new CustomException("请刷新重试");
        }
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + id, PrinterRedis.class);
        if (printerRedis == null) {
            throw new CustomException("请刷新重试");
        }
        PrinterBaseResp<PrintFileConfigResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        if (printerRedis.getSTU() < 3) {
            printImageRespPrinterBaseResp.setType(0);
            return printImageRespPrinterBaseResp;
        }

        if (!printerRedis.getPageNums().equals(0)) {
            printImageRespPrinterBaseResp.setType(1);
            printImageRespPrinterBaseResp.setData(new PrintFileConfigResp(id, printerRedis.getNeedPrintPagesIndex(), printerRedis.getPageNums(), printerRedis.getName()));
            return printImageRespPrinterBaseResp;
        }
        printImageRespPrinterBaseResp.setType(0);
        return printImageRespPrinterBaseResp;
    }

}
