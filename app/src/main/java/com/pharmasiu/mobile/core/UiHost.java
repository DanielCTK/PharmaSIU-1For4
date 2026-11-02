package com.pharmasiu.mobile.core;

public interface UiHost {
    DemoState state();
    void navigate(String route);
    void message(String message);
    void logout();
    void choosePrescriptionFile();
    default void chooseStaffProductPhoto(String productId) {
        message("Chức năng chọn ảnh chỉ hỗ trợ trên màn quản trị DEMO.");
    }
}
