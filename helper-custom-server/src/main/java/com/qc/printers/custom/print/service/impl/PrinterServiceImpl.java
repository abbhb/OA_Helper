package com.qc.printers.custom.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.chat.domain.enums.MessageTypeEnum;
import com.qc.printers.common.chat.domain.vo.request.ChatMessageReq;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.ValueLabelResult;
import com.qc.printers.common.common.event.print.FileToPDFEvent;
import com.qc.printers.common.common.event.print.PrintPDFEvent;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.domain.OssReq;
import com.qc.printers.common.common.utils.oss.domain.OssResp;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.config.SystemMessageConfig;
import com.qc.printers.common.print.domain.dto.CancelPrintDto;
import com.qc.printers.common.print.domain.dto.PrintDeviceDto;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.vo.CountTop10VO;
import com.qc.printers.common.print.mapper.PrinterMapper;
import com.qc.printers.common.print.service.IPrinterService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.mapper.UserMapper;
import com.qc.printers.common.user.service.IUserService;
import com.qc.printers.custom.print.domain.enums.PrintDataRespTypeEnum;
import com.qc.printers.custom.print.domain.vo.PrinterResult;
import com.qc.printers.custom.print.domain.vo.request.PrintFileReq;
import com.qc.printers.custom.print.domain.vo.response.*;
import com.qc.printers.custom.print.service.PrinterService;
import com.qc.printers.custom.print.service.strategy.AbstratePrintDataHandler;
import com.qc.printers.custom.print.service.strategy.PrintDataHandlerFactory;
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
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PrinterServiceImpl implements PrinterService {

    private final CommonService commonService;
    private final UserMapper userMapper;

    @Autowired
    private ConsulService consulService;

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
            printerResult.setUrl(minIoUtil.getUrlWithHttpByNoHttpKey(printerItem1.getUrl()));
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
    public R<PageData<PrinterResult>> listAllPrinter(Integer pageNum, Integer pageSize, String name, String user, Integer onlyPrinted, LocalDateTime startDate, LocalDateTime endDate) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize == null) {
            return R.error("传参错误");
        }

        Page pageInfo = new Page(pageNum, pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Printer::getCreateTime);
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Printer::getName, name);
        lambdaQueryWrapper.eq(!StringUtils.isEmpty(user), Printer::getCreateUser, user);
        lambdaQueryWrapper.eq(onlyPrinted.equals(1), Printer::getIsPrint, onlyPrinted);
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
        log.info("imageUrl={}", fileUrl);
        String[] split = fileUrl.split("\\?");
        String fileKey = split[0].split("/aistudio/")[1];
        //同步数据
        Printer printer = new Printer();
        printer.setIsPrint(0);
        printer.setUrl(fileKey);
        printer.setIsPrint(0);
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
        OssResp preSignedObjectUrl = minIoUtil.getPreSignedObjectUrl(new OssReq("temp-image", printer.getName() + ".png", printer.getId(), true));
        printerRedis.setImageUploadUrl(preSignedObjectUrl.getUploadUrl());
        printerRedis.setImageDownloadUrl(preSignedObjectUrl.getDownloadUrl());
        printerRedis.setIsCanGetImage(0);
        //40分钟后过期
        RedisUtils.set(MyString.print + printer.getId(), printerRedis, 2400L, TimeUnit.SECONDS);
        //如果不是pdf开始转换，修改为统一进入该事件，是不是pdf处理端区分
        applicationEventPublisher.publishEvent(new FileToPDFEvent(this, printer.getId()));
        return String.valueOf(printer.getId());

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
        List<HealthService> registeredServices = consulService.getRegisteredServices();
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
            throw new IllegalArgumentException("王子(公主)殿下，您是要横着还是竖着呢？");
        }
        if (printFileReq.getStartNum() == null) {
            throw new IllegalArgumentException("王子(公主)殿下，您要从哪里打印到哪里呢？");
        }
        if (printFileReq.getEndNum() == null) {
            throw new IllegalArgumentException("王子(公主)殿下，您要从哪里打印到哪里呢？");
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
        chatMessageReq.setBody(currentUser.getName() + "[id：" + currentUser.getId() + "]取消了一次打印任务，请注意是否为误取消他人任务！");
        chatService.sendMsg(chatMessageReq, RequestHolder.get().getUid());

    }

}
