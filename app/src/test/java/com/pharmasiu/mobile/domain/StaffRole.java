package com.pharmasiu.mobile.domain;

public enum StaffRole {
    CASHIER("Nhân viên bán hàng"),ADMIN("Quản trị");
    public final String label;
    StaffRole(String label){this.label=label;}
}
