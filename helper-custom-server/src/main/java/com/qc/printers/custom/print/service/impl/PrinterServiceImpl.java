package com.qc.printers.custom.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.chat.domain.enums.MessageTypeEnum;
import com.qc.printers.common.chat.domain.vo.request.ChatMessageReq;
import com.qc.printers.common.chat.domain.vo.request.msg.TextMsgReq;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.ValueLabelResult;
import com.qc.printers.common.common.event.print.FileToPDFEvent;
import com.qc.printers.common.common.event.print.PDFToImageEvent;
import com.qc.printers.common.common.event.print.PrintPDFEvent;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.common.utils.FileMD5;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.common.utils.oss.domain.OssReq;
import com.qc.printers.common.common.utils.oss.domain.OssResp;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.config.system.SystemMessageConfig;
import com.qc.printers.common.print.domain.dto.CancelPrintDto;
import com.qc.printers.common.print.domain.dto.DiffItem;
import com.qc.printers.common.print.domain.dto.PrintDeviceDto;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.enums.FileTypeEnum;
import com.qc.printers.common.print.domain.vo.CountTop10VO;
import com.qc.printers.common.print.domain.vo.request.PreUploadPrintFileReq;
import com.qc.printers.common.print.domain.vo.response.UnoServiceInfo;
import com.qc.printers.common.print.mapper.PrinterMapper;
import com.qc.printers.common.print.service.IPrinterService;
import com.qc.printers.common.print.service.engine.FileVerificationEngine;
import com.qc.printers.common.print.service.engine.FileVerificationEngineFactory;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.mapper.UserMapper;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.common.user.service.IUserService;
import com.qc.printers.custom.print.domain.enums.PrintDataRespTypeEnum;
import com.qc.printers.common.print.domain.vo.PrinterResult;
import com.qc.printers.common.print.domain.vo.request.PrintFileReq;
import com.qc.printers.common.print.domain.vo.response.*;
import com.qc.printers.custom.print.service.PrinterService;
import com.qc.printers.custom.print.service.strategy.AbstratePrintDataHandler;
import com.qc.printers.custom.print.service.strategy.PrintDataHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.qc.printers.common.common.constant.MQConstant.*;

@Service
@Slf4j
public class PrinterServiceImpl implements PrinterService {

    private final CommonService commonService;
    private final UserMapper userMapper;

    @Autowired
    private ConsulService consulService;

    @Autowired
    private RestTemplate restTemplate;

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
    private ChatService chatService;

    @Autowired
    private SystemMessageConfig systemMessageConfig;

    @Autowired
    private ISysDeptService iSysDeptService;



    @Autowired
    public PrinterServiceImpl(CommonService commonService, UserMapper userMapper, PrinterMapper printerMapper) {
        this.commonService = commonService;
        this.userMapper = userMapper;
        this.printerMapper = printerMapper;
    }



    @Override
    public R<PageData<PrinterResult>> listPrinter(Integer pageNum, Integer pageSize, String name, Integer onlyPrinted, LocalDateTime startDate, LocalDateTime endDate) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize == null) {
            return R.error("传参错误");
        }
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return R.error("系统异常");
        }

        Page pageInfo = new Page(pageNum, pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Printer::getCreateTime);
        lambdaQueryWrapper.eq(Printer::getCreateUser, currentUser.getId());
        lambdaQueryWrapper.eq(onlyPrinted.equals(1), Printer::getIsPrint, onlyPrinted);
        lambdaQueryWrapper.eq(Printer::getIsDelete, 0);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Printer::getName, name);
        //创建时间大于startTime
        lambdaQueryWrapper.ge(startDate != null, Printer::getCreateTime, startDate);
        lambdaQueryWrapper.le(endDate != null, Printer::getCreateTime, endDate);

        //暂时不支持通过日期模糊查询
        Page page = iPrinterService.page(pageInfo, lambdaQueryWrapper);
        if (page == null) {
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
            printerResult.setUrl(OssDBUtil.toUseUrl(printerItem1.getUrl()));
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
    public R<PageData<PrinterResult>> listAllPrinter(Integer pageNum, Integer pageSize, String name, Integer onlyPrinted, LocalDateTime startDate, LocalDateTime endDate, List<Long> onlyUser) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize == null) {
            return R.error("传参错误");
        }
        List<Long> uidS = userDao.listUserIdsWithScope(new User());
        if (uidS==null||uidS.size()==0){
            throw new CustomException("无权看见");
        }


        Page pageInfo = new Page(pageNum, pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Printer::getCreateTime);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Printer::getName, name);
        lambdaQueryWrapper.eq(onlyPrinted.equals(1), Printer::getIsPrint, onlyPrinted);
        lambdaQueryWrapper.eq(Printer::getIsDelete, 0);
        lambdaQueryWrapper.in(onlyUser != null && onlyUser.size() > 0, Printer::getCreateUser, onlyUser);
        //创建时间大于startTime
        lambdaQueryWrapper.ge(startDate != null, Printer::getCreateTime, startDate);
        lambdaQueryWrapper.le(endDate != null, Printer::getCreateTime, endDate);
        lambdaQueryWrapper.and(lambdaQueryWrapper1->{
            lambdaQueryWrapper1.in(!uidS.isEmpty(),Printer::getCreateUser,uidS);
        });
        //暂时不支持通过日期模糊查询
        Page page = iPrinterService.page(pageInfo, lambdaQueryWrapper);
        if (page == null) {
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
            printerResult.setUrl(OssDBUtil.toUseUrl(printerItem1.getUrl()));
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
            List<CountTop10VO> countTop10 = printerMapper.getCountTop10(new User());
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
        printer.setIsDuplex(isDuplex?2:1);
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
    public String uploadPrintFile(MultipartFile file, String hash) {
        String originalFilename = file.getOriginalFilename();

        FileVerificationEngine fileVerificationEngine = FileVerificationEngineFactory
                .getStrategyNoNull(
                        FileTypeEnum.of(originalFilename.substring(
                                originalFilename.lastIndexOf(".") + 1
                        )).getType()
                );
        fileVerificationEngine.check(file);// 统一校验文件，有异常此处就直接抛出

        if (StringUtils.isEmpty(hash)){
            // 本地计算hash-md5
            // 获取系统中的临时目录
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

            // 临时文件使用 UUID 随机命名
            Path tempFile = tempDir.resolve(Paths.get(UUID.randomUUID().toString()));

            try {
                // copy 到临时文件
                file.transferTo(tempFile);
                hash = FileMD5.md5HashCode32(new FileInputStream(tempFile.toFile()));
                log.info("打印文件上传：Hash:{}",hash);
                // 一般以前端的hash为准
            } catch (IOException e) {
                log.error("ERROR","error:{}",e);
                throw new CustomException("文件hash计算异常");
            } finally {
                // 始终删除临时文件
                try {
                    Files.delete(tempFile);
                } catch (IOException e) {
                    log.error("ERROR","临时文件删除失败，问题不大");
                }
            }
        }

        //先将文件上传到minio，这一步失败直接返回错误
        String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
        if (StringUtils.isEmpty(fileUrl)) {
            throw new CustomException("上传异常");
        }
        //同步数据
        Printer printer = new Printer();

        printer.setUrl(OssDBUtil.toDBUrl(fileUrl));
        printer.setIsPrint(0);
        if (StringUtils.isNotEmpty(hash)){
            printer.setContentHash(hash);
        }
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
        //统一使用png
        OssResp preSignedObjectUrl = minIoUtil.getPreSignedObjectUrl(new OssReq("temp-image", printer.getName() + ".png", printer.getId(), false));
        printerRedis.setImageUploadUrl(preSignedObjectUrl.getUploadUrl());
        printerRedis.setImageDownloadUrl(preSignedObjectUrl.getDownloadUrl());
        printerRedis.setIsCanGetImage(0);
        //40分钟后过期
        RedisUtils.set(MyString.print + printer.getId(), printerRedis, 2400L, TimeUnit.SECONDS);
        //如果不是pdf开始转换，修改为统一进入该事件，是不是pdf处理端区分
        applicationEventPublisher.publishEvent(new FileToPDFEvent(this, printer.getId()));
        return String.valueOf(printer.getId());

    }
    @Transactional
    @Override
    public String uploadPrintFileForWin(MultipartFile file, PrintFileReq printFileReq,Integer total) {
        if (printFileReq.getCopies() == null) {
            printFileReq.setCopies(1);//不填份数就强制1份
        }
        if (printFileReq.getIsDuplex() == null) {
            throw new IllegalArgumentException("王子(公主)殿下，您是要横着还是竖着呢？");
        }
        if (printFileReq.getStartNum() == null) {
            throw new IllegalArgumentException("王子(公主)殿下，您要从哪里打印到哪里呢？");
        }
        if (printFileReq.getEndNum() == null) {
            throw new IllegalArgumentException("王子(公主)殿下，您要从哪里打印到哪里呢？");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename.contains("\\?") || originalFilename.contains("？")) {
            throw new CustomException("文件名里不允许包含？请修改后在打印");
        }
        String supportFileExt = "pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,jpeg,png,bmp";
        // 判断文件拓展名是否再支持的列表里
        if (!supportFileExt.contains(originalFilename.substring(originalFilename.lastIndexOf(".") + 1))) {
            throw new CustomException(file.getOriginalFilename() + ",不支持该文件，请先转成pdf!");
        }

        if (file.isEmpty()) {
            throw new CustomException("文件为空");
        }
        //先将文件上传到minio，这一步失败直接返回错误
        String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
        if (StringUtils.isEmpty(fileUrl)) {
            throw new CustomException("上传异常");
        }
        //同步数据
        Printer printer = new Printer();
        printer.setIsPrint(1);
        printer.setUrl(OssDBUtil.toDBUrl(fileUrl));
        printer.setCopies(printFileReq.getCopies());
        printer.setPrintingDirection(printFileReq.getLandscape());
        printer.setName(file.getOriginalFilename());
        printer.setCreateTime(LocalDateTime.now());
        printer.setCreateUser(ThreadLocalUtil.getCurrentUser().getId());
        boolean save = iPrinterService.save(printer);
        printFileReq.setId(String.valueOf(printer.getId()));

        if (!save) {
            throw new CustomException("数据同步异常");
        }
        PrinterRedis printerRedis = new PrinterRedis();
        BeanUtils.copyProperties(printer,printerRedis);
        printerRedis.setId(printer.getId());
        printerRedis.setCopies(printFileReq.getCopies());
        printerRedis.setIsDuplex(printFileReq.getIsDuplex());
        printerRedis.setPageNums(total);

        printerRedis.setPrintingDirection(printFileReq.getLandscape());
        printerRedis.setNeedPrintPagesIndex(printFileReq.getStartNum());
        printerRedis.setNeedPrintPagesEndIndex(printFileReq.getEndNum());
        printerRedis.setSTU(4);//开始打印了
        printerRedis.setPdfUrl(fileUrl);
        printerRedis.setDeviceId(printFileReq.getDeviceId());
        //任务发送事务消息，保证成功
        RedisUtils.set(MyString.print + printFileReq.getId(), printerRedis);
        applicationEventPublisher.publishEvent(new PrintPDFEvent(this, printer.getId()));
        return "已添加任务到打印队列";

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
        AbstratePrintDataHandler strategyNoNull = PrintDataHandlerFactory.getStrategyNoNull(PrintDataRespTypeEnum.IMAGE.getType());
        return strategyNoNull.createR(printerRedis);
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
        AbstratePrintDataHandler strategyNoNull = PrintDataHandlerFactory.getStrategyNoNull(PrintDataRespTypeEnum.FILECONFIG.getType());
        return strategyNoNull.createR(printerRedis);
    }

    @Override
    public List<PrintDeviceResp> printDevicePolling() {
        List<HealthService> registeredServices = consulService.getPrintDeviceServices();
        List<PrintDeviceResp> printDeviceResps = new ArrayList<>();
        for (HealthService registeredService : registeredServices) {
            PrintDeviceResp printDeviceResp = new PrintDeviceResp();
            printDeviceResp.setDescription(registeredService.getService().getMeta().get("ZName"));

            printDeviceResp.setName(printDeviceResp.getDescription());
            //筛选了只要状态正常的，所以这里全是正常的
            printDeviceResp.setStatus(1);
            printDeviceResp.setId(registeredService.getService().getId());
            printDeviceResps.add(printDeviceResp);
        }
        return printDeviceResps;
    }

    @Transactional
    @Override
    public String printFile(PrintFileReq printFileReq) {
        if (printFileReq.getCopies() == null) {
            printFileReq.setCopies(1);//不填份数就强制1份
        }
        if (StringUtils.isEmpty(printFileReq.getId())) {
            throw new IllegalArgumentException("无法定位任务");
        }
        if (printFileReq.getIsDuplex() == null) {
            throw new IllegalArgumentException("您是要横着还是竖着呢？");
        }
        if (printFileReq.getStartNum() == null) {
            throw new IllegalArgumentException("您要从哪里打印到哪里呢？");
        }
        if (printFileReq.getEndNum() == null) {
            throw new IllegalArgumentException("您要从哪里打印到哪里呢？");
        }
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printFileReq.getId(), PrinterRedis.class);
        if (printerRedis == null) {
            throw new CustomException("请重新创建任务");
        }
        printerRedis.setCopies(printFileReq.getCopies());
        printerRedis.setIsDuplex(printFileReq.getIsDuplex());
        printerRedis.setPrintingDirection(printFileReq.getLandscape());
        printerRedis.setNeedPrintPagesIndex(printFileReq.getStartNum());
        printerRedis.setNeedPrintPagesEndIndex(printFileReq.getEndNum());
        printerRedis.setSTU(4);//开始打印了
        printerRedis.setDeviceId(printFileReq.getDeviceId());
        //任务发送事务消息，保证成功
        RedisUtils.set(MyString.print + printFileReq.getId(), printerRedis);
        applicationEventPublisher.publishEvent(new PrintPDFEvent(this, Long.valueOf(printFileReq.getId())));
        return "已添加任务到打印队列";
    }

    @Override
    public PrintDeviceInfoResp printDeviceInfoPolling(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("参数异常");
        }
        PrintDeviceInfoResp printDeviceInfoResp = new PrintDeviceInfoResp();
        PrintDeviceDto printDeviceDto = iPrinterService.pollingPrintDevice(id);
        if (printDeviceDto == null) {
            // 查不到详情
            printDeviceInfoResp.setStatusType(0);
            printDeviceInfoResp.setStatusTypeMessage("未知");
            printDeviceInfoResp.setId(id);
            return printDeviceInfoResp;
        }
        // 有详情拼接参数
        printDeviceInfoResp.setPrintJobs(printDeviceDto.getPrintJobs());
        printDeviceInfoResp.setPrintName(printDeviceDto.getPrintName());
        printDeviceInfoResp.setPrintDescription(printDeviceDto.getPrintDescription());
        printDeviceInfoResp.setStatusType(printDeviceDto.getStatusType());
        printDeviceInfoResp.setListNums(printDeviceDto.getListNums());
        printDeviceInfoResp.setStatusTypeMessage(printDeviceDto.getStatusTypeMessage());
        printDeviceInfoResp.setId(printDeviceDto.getId());
        return printDeviceInfoResp;
    }

    @Override
    public void cancelPrint(String id, String deviceId) {
        // 先执行取消任务逻辑
        CancelPrintDto cancelPrintDto = iPrinterService.cancelPrint(id, deviceId);
        if (cancelPrintDto.getStatus().equals(0)) {
            throw new CustomException(cancelPrintDto.getMsg());
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        // 直接使用currentUser就能拿到该用户的信息
        ChatMessageReq chatMessageReq = new ChatMessageReq();
        chatMessageReq.setRoomId(Long.valueOf(systemMessageConfig.getRoomId()));
        chatMessageReq.setMsgType(MessageTypeEnum.TEXT.getType());
        TextMsgReq textMsgReq = new TextMsgReq();
        textMsgReq.setContent("@"+currentUser.getName() + " 取消了一次打印任务，请注意是否为误取消他人任务！");
        List<Long> atUidList = new ArrayList<>();
        atUidList.add(currentUser.getId());
        textMsgReq.setAtUidList(atUidList);
        chatMessageReq.setBody(textMsgReq);
        // 必须为系统用户发送的消息
        chatService.sendMsg(chatMessageReq, Long.valueOf(systemMessageConfig.getUserId()));

    }

    @Transactional
    @Override
    public String preUploadPrintFile(PreUploadPrintFileReq printFileReq) {
        if (StringUtils.isEmpty(printFileReq.getHash())){
            throw new CustomException("hash为空-走后续的正常流程");
        }
        if (StringUtils.isEmpty(printFileReq.getOriginFileName())){
            throw new CustomException("原始文件名获取失败-走后续的正常流程");
        }

        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Printer::getContentHash,printFileReq.getHash());
        List<Printer> list = iPrinterService.list(lambdaQueryWrapper);
        if (list.isEmpty()){
            throw new CustomException("不具备快速打印条件，跳过");
        }
        List<Integer> pdfPagesList = list.stream().map(Printer::getOriginFilePages).toList();
        List<String> pdfImageList = list.stream().map(Printer::getPdfImage).toList();
        if (pdfPagesList.isEmpty() || pdfImageList.isEmpty()){
            throw new CustomException("不具备快速打印条件");
        }
        // 检查所有页数是否相等
        boolean pagesEqual = pdfPagesList.stream().distinct().count() == 1L;
        if (!pagesEqual) {
            log.info("具体pdf页数：{}",pdfPagesList);
            // 清除该hash所有相关hash
            LambdaUpdateWrapper<Printer> printerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            printerLambdaUpdateWrapper.set(Printer::getContentHash,null);
            printerLambdaUpdateWrapper.eq(Printer::getContentHash,printFileReq.getHash());
            iPrinterService.update(printerLambdaUpdateWrapper);
            throw new CustomException("相同文件的PDF 页数不一致,此hash不具备可用性，清理");
        }
        List<Long> needDeleteId = new ArrayList<>();
        // 找到存在的 PDF URL
        Integer pdfZ = 0;
        Printer pdfZU = null;
        for (Printer pdf : list) {
            boolean b = false;
            if (StringUtils.isNotEmpty(pdf.getPdfUrl())){
                b = fileExists(pdf.getPdfUrl());
            }
            if (b){
                pdfZ = 1;
                pdfZU = pdf;
                break;
            }
            needDeleteId.add(pdf.getId());
        }
        if (pdfZ.equals(0)){
            LambdaUpdateWrapper<Printer> printerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            printerLambdaUpdateWrapper.set(Printer::getContentHash,null);
            printerLambdaUpdateWrapper.in(needDeleteId.size()>1,Printer::getId,needDeleteId);
            printerLambdaUpdateWrapper.eq(needDeleteId.size()==1,Printer::getId,needDeleteId.get(0));
            iPrinterService.update(printerLambdaUpdateWrapper);
            throw new CustomException("找不到可用pdf文件");
        }
        // 找到存在的 PNG 文件
        needDeleteId = new ArrayList<>();
        // 找到存在的 PDF URL
        Integer pdfImageZ = 0;
        String pdfImageZU = "";
        for (Printer pdf : list) {
            boolean b = false;
            if (StringUtils.isNotEmpty(pdf.getPdfImage())){
                 b = fileExists(pdf.getPdfImage());
            }
            if (b){
                pdfImageZ = 1;
                pdfImageZU = pdf.getPdfImage();
                break;
            }
            needDeleteId.add(pdf.getId());
        }
        if (pdfImageZ.equals(0)){
            LambdaUpdateWrapper<Printer> printerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            printerLambdaUpdateWrapper.set(Printer::getPdfImage,null);
            printerLambdaUpdateWrapper.in(needDeleteId.size()>1,Printer::getId,needDeleteId);
            printerLambdaUpdateWrapper.eq(needDeleteId.size()==1,Printer::getId,needDeleteId.get(0));
            iPrinterService.update(printerLambdaUpdateWrapper);
            // 找不到缩略图无所谓，中间态也算更快了
        }
        // 接下来肯定是最少包含存在的pdf文件了，直接创建任务即可
        Printer printer = new Printer();
        printer.setUrl(OssDBUtil.toDBUrl(pdfZU.getUrl()));
        printer.setIsPrint(0);
        printer.setPdfUrl(OssDBUtil.toDBUrl(pdfZU.getPdfUrl()));
        printer.setCreateTime(LocalDateTime.now());
        printer.setCreateUser(ThreadLocalUtil.getCurrentUser().getId());
        if (StringUtils.isNotEmpty(printFileReq.getHash())){
            printer.setContentHash(printFileReq.getHash());
        }
        if (pdfImageZ.equals(1)){
            // 形成闭环
            printer.setPdfImage(pdfImageZU);
        }
        printer.setName(printFileReq.getOriginFileName());
        if (pdfZU.getOriginFilePages()==null){
            throw new CustomException("重要参数缺式");
        }
        printer.setOriginFilePages(pdfZU.getOriginFilePages());
        boolean save = iPrinterService.save(printer);
        if (!save) {
            throw new CustomException("数据同步异常");
        }
        PrinterRedis printerRedis = new PrinterRedis();
        BeanUtils.copyProperties(printer, printerRedis);
        printerRedis.setNeedPrintPagesIndex(1);//从第一页开始
        printerRedis.setSTU(3);
        printerRedis.setPdfUrl(OssDBUtil.toUseUrl(printer.getPdfUrl()));
        printerRedis.setUrl(OssDBUtil.toUseUrl(printer.getUrl()));
        printerRedis.setPdfImage(OssDBUtil.toUseUrl(printer.getPdfImage()));
        printerRedis.setPageNums(printer.getOriginFilePages());

        if (pdfImageZ.equals(1)){
            // 都找得到最好
            printerRedis.setIsCanGetImage(1);
            printerRedis.setImageDownloadUrl(OssDBUtil.toUseUrl(pdfImageZU));
            RedisUtils.set(MyString.print + printer.getId(), printerRedis, 2400L, TimeUnit.SECONDS);
            return String.valueOf(printer.getId());
        }
        //统一使用png
        OssResp preSignedObjectUrl = minIoUtil.getPreSignedObjectUrl(new OssReq("temp-image", printer.getName() + ".png", printer.getId(), false));
        printerRedis.setImageUploadUrl(preSignedObjectUrl.getUploadUrl());
        printerRedis.setImageDownloadUrl(preSignedObjectUrl.getDownloadUrl());
        printerRedis.setIsCanGetImage(0);
        //40分钟后过期
        RedisUtils.set(MyString.print + printer.getId(), printerRedis, 2400L, TimeUnit.SECONDS);
        // 生成缩略图
        applicationEventPublisher.publishEvent(new PDFToImageEvent(this, printer.getId()));
        return String.valueOf(printer.getId());
    }

    @Override
    public UnoServiceInfo unoServiceInfo() {
        // 检查各组件状态
        // 从网络请求
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity entity = new HttpEntity(headers);
            ParameterizedTypeReference<R<List<DiffItem>>> responseType = new ParameterizedTypeReference<R<List<DiffItem>>>() {};
            ResponseEntity<R<List<DiffItem>>> resp = restTemplate.exchange("http://127.0.0.1:18454/myself_rocketmq", HttpMethod.GET, entity, responseType);
            if (!resp.getBody().getCode().equals(1)){
                throw new CustomException("状态异常");
            }
            List<DiffItem> list = resp.getBody().getData();
            Integer fileToPdfDiffNumber = 0;
            Integer fileToPdfConsumerNumber = 0;

            Integer pdfToImageDiffNumber = 0;
            Integer pdfToImageConsumerNumber = 0;
            String chulijianyi = "";

            for (DiffItem diffItem : list) {
                if (diffItem.getGroup().contains(SEND_FILE_TOPDF_GROUP)){
                    fileToPdfConsumerNumber = diffItem.getCountOfOnlineConsumers();
                    fileToPdfDiffNumber = diffItem.getDiff();
                }
                if (diffItem.getGroup().contains(SEND_PDF_IMAGE_GROUP)){
                    pdfToImageConsumerNumber = diffItem.getCountOfOnlineConsumers();
                    pdfToImageDiffNumber = diffItem.getDiff();
                }
            }
            if (fileToPdfDiffNumber>5){
                chulijianyi+= "[严重]请检查转pdf程序是不是大量掉线或被异常任务阻塞;";
            }
            if (fileToPdfConsumerNumber<2){
                chulijianyi+= "[严重]转pdf程序存活数小于2个;";
            }
            if (pdfToImageDiffNumber>3){
                chulijianyi+= "[一般]转缩略图任务缓慢;";
            }
            if (fileToPdfDiffNumber>3){
                chulijianyi+= "[一般]文件转PDF任务缓慢;";
            }
            if (pdfToImageDiffNumber>5){
                chulijianyi+= "[严重]请检查转缩略图程序是不是大量掉线或被异常任务阻塞;";
            }
            if (pdfToImageConsumerNumber<2){
                chulijianyi+= "[严重]转缩略图程序存活数小于2个;";
            }
            return UnoServiceInfo.builder()
                    .toPDFConsumerNumber(fileToPdfConsumerNumber)
                    .toImageConsumerNumber(pdfToImageConsumerNumber)
                    .toImageDiffNumber(fileToPdfDiffNumber)
                    .toPDFDiffNumber(pdfToImageDiffNumber)
                    .chulijianyi(chulijianyi)
                    .build();


        }catch (Exception exception){
            log.error("ERROR","获取打印转换节点相关指标失败,err:{}",exception);
            return UnoServiceInfo.builder()
                    .toPDFConsumerNumber(0)
                    .toImageConsumerNumber(0)
                    .toImageDiffNumber(0)
                    .toPDFDiffNumber(0)
                    .chulijianyi("监控启动中;")
                    .build();
        }

    }

    @Transactional
    @Override
    public String deleteHistoryPrints(Long id) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        Printer pr = iPrinterService.getById(id);
        if (pr==null){
            throw new CustomException("请不要多次点击");
        }
        if (!pr.getCreateUser().equals(currentUser.getId())){
            throw new CustomException("仅允许删除自己的记录!");
        }
        LambdaUpdateWrapper<Printer> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(Printer::getId,id);
        queryWrapper.eq(Printer::getCreateUser,currentUser.getId());// 防止越权删除
        queryWrapper.set(Printer::getIsDelete,1);
        iPrinterService.update(queryWrapper);
        return "删除成功";
    }

    // 检查文件是否存在的方法
    public static boolean fileExists(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // 仅检查后缀为 .pdf 或 .png 的文件
        if (!(url.toLowerCase().endsWith(".pdf") || url.toLowerCase().endsWith(".png"))) {
            return false;
        }

        try {
            String[] split = url.split("/");
            StringBuilder obj = new StringBuilder();
            int i = 0;
            for (String s : split) {
                i+=1;
                if (i==1)continue;
                if (i!=2) obj.append("/");
                obj.append(s);
            }
            // 解析 URL，假设 MinIO 中的 bucket 名称和对象名称从 URL 提取
            String bucketName = split[0];
            bucketName = URLDecoder.decode(bucketName,"UTF-8" );

            String objectName = obj.toString();
            objectName = URLDecoder.decode(objectName,"UTF-8" );

            // 获取对象的状态
            MinIoUtil.statObject(bucketName,objectName);
            return true; // 如果成功获取到对象状态，则文件存在
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
