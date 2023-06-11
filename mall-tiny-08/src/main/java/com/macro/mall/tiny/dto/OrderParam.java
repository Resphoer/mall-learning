package com.macro.mall.tiny.dto;

import lombok.Data;

/**
 * 生成订单时传入的参数
 */
@Data
public class OrderParam {
    // 收获地址id
    private Long memberReceiveAddressId;
    // 优惠券id
    private Long couponId;
    // 使用的积分数
    private Integer useIntegration;
    // 支付方式
    private Integer payType;
}
