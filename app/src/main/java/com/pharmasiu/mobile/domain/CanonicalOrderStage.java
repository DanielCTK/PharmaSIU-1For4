package com.pharmasiu.mobile.domain;

/** Seven human-readable external state groups; not evidence of a fulfilled order. */
public enum CanonicalOrderStage {
    PENDING("CHỜ XỬ LÝ"),
    RX_PENDING("CHỜ DUYỆT TOA"),
    REJECTED("TỪ CHỐI"),
    HELD("ĐÃ GIỮ HÀNG"),
    READY("SẴN SÀNG NHẬN"),
    COMPLETED("HOÀN TẤT"),
    CANCELLED("ĐÃ HỦY");
    public final String label;
    CanonicalOrderStage(String label){this.label=label;}
}
