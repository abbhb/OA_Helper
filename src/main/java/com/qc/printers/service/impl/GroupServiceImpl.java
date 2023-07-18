package com.qc.printers.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.mapper.GroupMapper;
import com.qc.printers.pojo.Group;
import com.qc.printers.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

}
