package com.abdecd.moebackend.business.pojo.vo.tag;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
class TagVO {
    private Long id;
    private String tagName;
}
