package com.qc.printers.custom.navigation.service;


import com.qc.printers.common.common.R;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationResult;

import java.util.List;

public interface QuickNavigationService {
    R<List<QuickNavigationResult>> list(Long userId);
}
