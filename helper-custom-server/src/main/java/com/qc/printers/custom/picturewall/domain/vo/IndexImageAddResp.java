package com.qc.printers.custom.picturewall.domain.vo;

import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class IndexImageAddResp extends IndexImage implements Serializable {
    private List<Long> depts;
}
