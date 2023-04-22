package com.qc.printers.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.CustomException;
import com.qc.printers.common.R;
import com.qc.printers.mapper.PrinterMapper;
import com.qc.printers.mapper.UserMapper;
import com.qc.printers.pojo.PrinterResult;
import com.qc.printers.pojo.ValueLabelResult;
import com.qc.printers.pojo.entity.PageData;
import com.qc.printers.pojo.entity.Printer;
import com.qc.printers.pojo.entity.User;
import com.qc.printers.pojo.vo.CountTop10VO;
import com.qc.printers.service.PrinterService;
import com.qc.printers.service.UserService;
import com.qc.printers.utils.FileMD5;
import com.qc.printers.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PrinterServiceImpl extends ServiceImpl<PrinterMapper, Printer> implements PrinterService {

    private final UserService userService;

    private final UserMapper userMapper;

    private final PrinterMapper printerMapper;

    @Autowired
    public PrinterServiceImpl(UserService userService, UserMapper userMapper, PrinterMapper printerMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.printerMapper = printerMapper;
    }

    @Transactional
    @Override
    public boolean addPrinter(Printer printer,String urlName) {
        if (StringUtils.isEmpty(printer.getName())){
            log.error("打印记录缺失");
            return false;
        }
        if (printer.getCreateUser()==null){
            log.error("打印记录缺失");
            return false;
        }
        // todo: 优化: 双面打印时打印记录中记录使用纸张页数
        // 此处等待NumberOfPrintedPagesIndex字段调整为int类型后再行更改
        // 双面打印时打印记录中记录使用纸张页数, 向上取整
        // if (printer.getIsDuplex() == 1) {
        //     printer.setNumberOfPrintedPagesIndex((int) Math.ceil((double) printer.getNumberOfPrintedPagesIndex() / 2));
        // }
        InputStream inputStream = null;
        try {
            URL url = new URL(urlName);
            inputStream = url.openStream();
            printer.setContentHash(FileMD5.md5HashCode(inputStream));
            return super.save(printer);
        } catch (FileNotFoundException e) {
            log.error("打印记录缺失");
            log.error(e.getMessage());
            return false;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public R<PageData<PrinterResult>> listPrinter(Integer pageNum, Integer pageSize, String token, String name, String date) {
        if (pageNum==null){
            return R.error("传参错误");
        }
        if (pageSize==null){
            return R.error("传参错误");
        }
        DecodedJWT decodedJWT = JWTUtil.deToken(token);
        Claim id = decodedJWT.getClaim("id");
        if (id==null){
            return R.error("缺少关键信息");
        }
        Page pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Printer::getCreateUser,Long.valueOf(id.asString()));
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Printer::getName,name);
        //暂时不支持通过日期模糊查询
        Page page = super.page(pageInfo, lambdaQueryWrapper);
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
            printerResult.setUrl(printerItem1.getUrl());
//            printerResult.setCreateUser(String.valueOf(printerItem1.getCreateUser())); 自己的记录肯定是自己没必要
            printerResult.setNumberOfPrintedPages(printerItem1.getNumberOfPrintedPages());
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
    public R<PageData<PrinterResult>> listAllPrinter(Integer pageNum, Integer pageSize, String name, String user) {
        if (pageNum==null){
            return R.error("传参错误");
        }
        if (pageSize==null){
            return R.error("传参错误");
        }

        Page pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<Printer> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Printer::getName,name);
        lambdaQueryWrapper.eq(!StringUtils.isEmpty(user),Printer::getCreateUser,user);

        //暂时不支持通过日期模糊查询
        Page page = super.page(pageInfo, lambdaQueryWrapper);
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
            printerResult.setUrl(printerItem1.getUrl());
            User user1 = userMapper.getUserIncludeDeleted(printerItem1.getCreateUser());
            if (user1==null){
                printerResult.setCreateUser(String.valueOf(printerItem1.getCreateUser())+"(用户信息已丢失)");
            }else {
                printerResult.setCreateUser(user1.getName());
            }

            printerResult.setNumberOfPrintedPages(printerItem1.getNumberOfPrintedPages());
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
}
