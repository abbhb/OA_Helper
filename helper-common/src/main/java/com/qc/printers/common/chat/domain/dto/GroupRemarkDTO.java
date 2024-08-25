package com.qc.printers.common.chat.domain.dto;

import com.qc.printers.common.chat.domain.entity.RoomGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupRemarkDTO extends RoomGroup implements Serializable {

    /**
     * 关键就是为了备注名
     */
    private String remarkName;
}
