package com.pharmasiu.mobile.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class StaffPhotoRulesTest {
    @Test public void onlyAdminCanAttachLocalContentPhoto(){
        DemoState s=new DemoState();
        assertFalse(s.changePhoto("mask-01","content://local/pic"));
        assertTrue(s.login("nhanvien@pharmasiu.demo","1111"));
        assertFalse(s.changePhoto("mask-01","content://local/pic"));
        s.logout();
        assertTrue(s.login("quanly@pharmasiu.demo","2222"));
        assertFalse(s.changePhoto("missing","content://local/pic"));
        assertFalse(s.changePhoto("mask-01","https://example.com/pic"));
        assertFalse(s.changePhoto("mask-01","file:///storage/img"));
        assertTrue(s.changePhoto("mask-01","content://local/pic"));
        assertEquals("content://local/pic",s.staffPhotoUris.get("mask-01"));
        assertTrue(s.changePhoto("mask-01",null));
        assertFalse(s.staffPhotoUris.containsKey("mask-01"));
    }
}
