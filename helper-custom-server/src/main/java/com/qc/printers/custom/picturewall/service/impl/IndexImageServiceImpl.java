package com.qc.printers.custom.picturewall.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.picturewall.dao.IndexImageDeptDao;
import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import com.qc.printers.common.picturewall.domain.entity.IndexImageData;
import com.qc.printers.common.picturewall.domain.entity.IndexImageDept;
import com.qc.printers.common.picturewall.service.IIndexImageService;
import com.qc.printers.common.signin.domain.entity.BcRule;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.picturewall.domain.vo.IndexImageAddReq;
import com.qc.printers.custom.picturewall.domain.vo.IndexImageAddResp;
import com.qc.printers.custom.picturewall.service.IndexImageService;
import com.qc.printers.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndexImageServiceImpl implements IndexImageService {

    @Autowired
    MinIoUtil minIoUtil;

    @Autowired
    private IIndexImageService iIndexImageService;

    @Autowired
    private IndexImageDeptDao indexImageDeptDao;

    @Override
    public List<String> allLabel() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        LambdaQueryWrapper<IndexImage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<IndexImage> list = iIndexImageService.list(lambdaQueryWrapper);
        List<Long> idS = list.stream().map(IndexImage::getId).collect(Collectors.toSet()).stream().toList();
        LambdaQueryWrapper<IndexImageDept> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(IndexImageDept::getDeptId,idS);
        List<IndexImageDept> list1 = indexImageDeptDao.list(lambdaQueryWrapper1);
        if (list1==null){
            list1 = new ArrayList<>();
        }
        // 使用Stream API来创建Map

// 使用Stream API来创建Map
        Map<Long, Set<Long>> idToDeptIdsSetMap = list1.stream()
                .collect(Collectors.groupingBy(
                        IndexImageDept::getIndexImageId, // 根据id分组
                        Collectors.mapping(IndexImageDept::getDeptId, Collectors.toCollection(LinkedHashSet::new)) // 收集每个组的deptId到LinkedHashSet中
                ));
        list.sort((m1, m2) -> {
            Integer order1 = m1.getSort();
            Integer order2 = m2.getSort();
            return order1.compareTo(order2);
        });
        Set<String> strings = new HashSet<>();
        List<String> stringList = new ArrayList<>();
        for (IndexImage indexImage : list) {
            if (indexImage.getVisibility().equals(2)){
                if (!idToDeptIdsSetMap.containsKey(indexImage.getId())){
                   continue;
                }
                if (!idToDeptIdsSetMap.get(indexImage.getId()).contains(currentUser.getDeptId())){
                    continue;
                }
            }

            if (!strings.contains(indexImage.getLabel())) {
                stringList.add(indexImage.getLabel());
            }
            strings.add(indexImage.getLabel());

        }
        return stringList;
    }

    @Override
    public IndexImage labelImage(String label) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        LambdaQueryWrapper<IndexImage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(IndexImage::getLabel, label);
        IndexImage indexImage = iIndexImageService.getOne(lambdaQueryWrapper);
        if (indexImage==null){
            throw new CustomException("信息异常");
        }
        if (indexImage.getVisibility().equals(2)){
            LambdaQueryWrapper<IndexImageDept> deptLambdaQueryWrapper = new LambdaQueryWrapper<>();
            deptLambdaQueryWrapper.eq(IndexImageDept::getDeptId,currentUser.getDeptId());
            deptLambdaQueryWrapper.eq(IndexImageDept::getIndexImageId,indexImage.getId());
            List<IndexImageDept> list = indexImageDeptDao.list(deptLambdaQueryWrapper);
            if (list==null||list.size()<1){
                throw new CustomException("无权限!");
            }
        }
        List<IndexImageData> data = indexImage.getData();
        List<IndexImageData> indexImageData = JSON.parseArray(data.toString(), IndexImageData.class);

        for (IndexImageData datum : indexImageData) {
            datum.setImage(OssDBUtil.toUseUrl(datum.getImage()));
        }
        indexImage.setData(indexImageData);
        return indexImage;
    }

    @Transactional
    public void check(IndexImageAddReq indexImageAddReq){
        if (indexImageAddReq == null) {
            throw new IllegalArgumentException("请检查");
        }
        IndexImage indexImage = indexImageAddReq.getIndexImage();
        if (indexImage.getData()==null||indexImage.getData().size()==0) {
            throw new IllegalArgumentException("必须包含图片");
        }
        if (indexImage.getVisibility()==null){
            throw new CustomException("必须包含可见性");
        }
        if (indexImage.getVisibility().equals(2)){
            if (indexImageAddReq.getDeptIds()==null||indexImageAddReq.getDeptIds().size()==0){
                throw new CustomException("最少包含一个可见部门");
            }
        }else if (!indexImage.getVisibility().equals(1)){
            throw new CustomException("当前只允许1，2");
        }

        if (StringUtils.isEmpty(indexImage.getLabel())) {
            throw new IllegalArgumentException("必须包含标签");
        }
        if (indexImage.getSort() == null) {
            throw new IllegalArgumentException("请输入排序");
        }
    }
    @Transactional
    @Override
    public String addIndexImage(IndexImageAddReq indexImageAddReq) {
        IndexImage indexImage = indexImageAddReq.getIndexImage();
        this.check(indexImageAddReq);
        for (IndexImageData datum : indexImage.getData()) {
            datum.setImage(OssDBUtil.toDBUrl(datum.getImage()));
        }
        iIndexImageService.save(indexImage);

        if (indexImage.getVisibility().equals(2)){
            List<IndexImageDept> depts = new ArrayList<>();
            for (Long deptId : indexImageAddReq.getDeptIds()) {
                IndexImageDept indexImageDept = new IndexImageDept();
                indexImageDept.setIndexImageId(indexImage.getId());
                indexImageDept.setDeptId(deptId);
                depts.add(indexImageDept);
            }
            indexImageDeptDao.saveBatch(depts);
        }

        return "添加成功";
    }

    @Transactional
    @Override
    public String updateIndexImage(IndexImageAddReq indexImageAddReq) {
        this.check(indexImageAddReq);

        IndexImage indexImage = indexImageAddReq.getIndexImage();

        if (indexImage.getId() == null) {
            throw new IllegalArgumentException("请检查");
        }
        for (IndexImageData datum : indexImage.getData()) {
            datum.setImage(OssDBUtil.toDBUrl(datum.getImage()));
        }
        iIndexImageService.updateById(indexImage);
        // 先删除部门关系
        LambdaQueryWrapper<IndexImageDept> deptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deptLambdaQueryWrapper.eq(IndexImageDept::getIndexImageId,indexImage.getId());
        indexImageDeptDao.remove(deptLambdaQueryWrapper);
        // 在添加部门关系
        if (indexImage.getVisibility().equals(2)){
            List<IndexImageDept> depts = new ArrayList<>();
            for (Long deptId : indexImageAddReq.getDeptIds()) {
                IndexImageDept indexImageDept = new IndexImageDept();
                indexImageDept.setIndexImageId(indexImage.getId());
                indexImageDept.setDeptId(deptId);
                depts.add(indexImageDept);
            }
            indexImageDeptDao.saveBatch(depts);
        }
        return "更新成功";
    }

    @Transactional
    @Override
    public String deleteIndexImage(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("请检查");
        }
        iIndexImageService.removeById(id);
        LambdaQueryWrapper<IndexImageDept> deptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deptLambdaQueryWrapper.eq(IndexImageDept::getIndexImageId,id);
        indexImageDeptDao.remove(deptLambdaQueryWrapper);
        return "删除成功";
    }

    @Override
    public List<IndexImageAddResp> list() {
        List<IndexImage> list = iIndexImageService.list();
        List<IndexImageAddResp> listR = new ArrayList<>();
        for (IndexImage indexImage : list) {
            IndexImageAddResp indexImageAddResp = new IndexImageAddResp();
            List<IndexImageData> data = indexImage.getData();
            List<IndexImageData> indexImageData = JSON.parseArray(data.toString(), IndexImageData.class);
            for (IndexImageData datum : indexImageData) {
                datum.setImage(OssDBUtil.toUseUrl(datum.getImage()));
            }
            indexImage.setData(indexImageData);
            BeanUtils.copyProperties(indexImage,indexImageAddResp);
            if (indexImageAddResp.getVisibility().equals(1)){
                indexImageAddResp.setDepts(new ArrayList<>());
            }else {
                LambdaQueryWrapper<IndexImageDept> deptLambdaQueryWrapper = new LambdaQueryWrapper<>();
                deptLambdaQueryWrapper.eq(IndexImageDept::getIndexImageId,indexImageAddResp.getId());
                List<Long> list1 = indexImageDeptDao.list(deptLambdaQueryWrapper).stream().map(IndexImageDept::getDeptId).toList();
                indexImageAddResp.setDepts(list1);
            }
            listR.add(indexImageAddResp);
        }

        return listR;
    }
}
