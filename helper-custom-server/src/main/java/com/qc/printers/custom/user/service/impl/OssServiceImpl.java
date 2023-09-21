package com.qc.printers.custom.user.service.impl;

import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.oss.domain.OssReq;
import com.qc.printers.common.common.utils.oss.domain.OssResp;
import com.qc.printers.common.user.domain.enums.OssSceneEnum;
import com.qc.printers.common.user.domain.vo.request.oss.UploadUrlReq;
import com.qc.printers.custom.user.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-20
 */
@Service
public class OssServiceImpl implements OssService {
    @Autowired
    private MinIoUtil minIoUtil;

    @Override
    public OssResp getUploadUrl(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum = OssSceneEnum.of(req.getScene());
        AssertUtil.isNotEmpty(sceneEnum, "场景有误");
        OssReq ossReq = OssReq.builder()
                .fileName(req.getFileName())
                .filePath(sceneEnum.getPath())
                .uid(uid)
                .build();
        return minIoUtil.getPreSignedObjectUrl(ossReq);
    }
}
