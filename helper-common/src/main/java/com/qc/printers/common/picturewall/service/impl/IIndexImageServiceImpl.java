package com.qc.printers.common.picturewall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import com.qc.printers.common.picturewall.mapper.IndexImageMapper;
import com.qc.printers.common.picturewall.service.IIndexImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IIndexImageServiceImpl extends ServiceImpl<IndexImageMapper, IndexImage> implements IIndexImageService {

}
