package com.qc.printers.common.sensitive;

import com.qc.printers.common.common.utils.sensitiveWord.IWordDeny;
import com.qc.printers.common.sensitive.dao.SensitiveWordDao;
import com.qc.printers.common.sensitive.domain.SensitiveWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyWordDeny implements IWordDeny {
    @Autowired
    private SensitiveWordDao sensitiveWordDao;

    @Override
    public List<String> deny() {
        return sensitiveWordDao.list()
                .stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
    }
}
