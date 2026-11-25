package com.pharmasiu.mobile.ui.customer;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogCategories;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Regression coverage for the audited catalog being wrongly lumped into devices. */
public class CatalogCategoriesTest {
    private DemoState.Product product(String code,String name) {
        return new DemoState.Product(code,name,"Hộp · ThiẾt bị",36000,"ThiẾt bị",false,"Hộp","");
    }

    @Test public void auditedCottonAndSanitaryProductsAreNotDevices() {
        assertEquals("Chăm sóc",CatalogCategories.categoryOf(product("VTYT-006","Bông tẩy trang 100 miếng")));
        assertEquals("Chăm sóc",CatalogCategories.categoryOf(product("VTYT-009","Băng vệ sinh Kotex")));
        assertEquals("Chăm sóc",CatalogCategories.categoryOf(product("VTYT-010","Băng vệ sinh Diana")));
        assertEquals("Vật tư y tế",CatalogCategories.categoryOf(product("VTYT-008","Bông tẩy trang/bông y tế (gói trắng)")));
        assertEquals("Vật tư y tế",CatalogCategories.categoryOf(product("VTYT-001","Khẩu trang y tế xanh 4 lớp")));
        assertEquals("Vật tư y tế",CatalogCategories.categoryOf(product("VTYT-003","Khẩu trang y tế 4 lớp (hộp vàng)")));
    }

    @Test public void deviceFilterContainsOnlyAuditedDevicesInMixedRealCatalog() {
        List<DemoState.Product> products=Arrays.asList(
            product("VTYT-006","Bông tẩy trang 100 miếng"),
            product("VTYT-008","Bông tẩy trang/bông y tế (gói trắng)"),
            product("VTYT-009","Băng vệ sinh Kotex"),
            product("VTYT-011","Băng cá nhân"),
            product("VTYT-012","Nhiệt kế trán Omron"),
            product("TIEUDUNG-008","Hút mũi con voi"));
        List<DemoState.Product> result=CatalogSupport.filterProducts(products,"","Thiết bị","Đề xuất");
        assertEquals(2,result.size());
        assertEquals("VTYT-012",result.get(0).id);
        assertEquals("TIEUDUNG-008",result.get(1).id);
        assertEquals(result,CatalogSupport.filterProducts(products,"","ThiẾt bị","Đề xuất"));
    }

    @Test public void unknownVtytDefaultsToSuppliesRatherThanInventingADevice() {
        DemoState.Product unknown=product("VTYT-999","Sản phẩm mới");
        assertEquals("Vật tư y tế",CatalogCategories.categoryOf(unknown));
        assertFalse(CatalogCategories.matches(unknown,"Thiết bị"));
        assertTrue(CatalogCategories.matches(unknown,"Vật tư y tế"));
        assertEquals("ThiẾt bị",unknown.category);
        assertFalse(unknown.requiresPrescription);
    }

    @Test public void deviceCasingIsCanonicalAndAvailableGroupsRemoveLegacyTypo() {
        DemoState.Product thermometer=product("VTYT-012","Nhiệt kế trán Omron");
        assertEquals("Thiết bị",CatalogCategories.categoryOf(thermometer));
        List<String> groups=CatalogCategories.available(Arrays.asList(thermometer,product("VTYT-009","Băng vệ sinh Kotex")));
        assertTrue(groups.contains("Thiết bị"));
        assertTrue(groups.contains("Chăm sóc"));
        assertFalse(groups.contains("ThiẾt bị"));
        assertEquals("Tất cả",groups.get(0));
    }

    @Test public void searchDoesNotReintroduceWrongLegacyDeviceSuffix() {
        List<DemoState.Product> products=Arrays.asList(product("VTYT-006","Bông tẩy trang"),product("VTYT-012","Nhiệt kế trán Omron"));
        List<DemoState.Product> devices=CatalogSupport.filterProducts(products,"thiet bi","Tất cả","Đề xuất");
        assertEquals(1,devices.size());
        assertEquals("VTYT-012",devices.get(0).id);
        assertEquals(1,CatalogSupport.filterProducts(products,"bong tay trang","Chăm sóc","Đề xuất").size());
    }

    @Test public void prescriptionStatusStillWinsAndSupplementSearchRetainsFoodGrouping() {
        DemoState.Product rx=product("VTYT-009","Tên do quản lý thay đổi");
        rx.requiresPrescription=true;
        assertEquals("Kê đơn",CatalogCategories.categoryOf(rx));
        assertTrue(CatalogCategories.matches(rx,"Kê đơn"));
        assertFalse(CatalogCategories.matches(rx,"Chăm sóc"));
        assertFalse(CatalogCategories.matches(rx,"Thiết bị"));
        DemoState.Product supplement=new DemoState.Product("TPCN-001","Ginkgo Biloba 120","Hộp · Thực phẩm",90000,"Thực phẩm",false,"Hộp","");
        assertEquals("Thực phẩm bổ sung",CatalogCategories.categoryOf(supplement));
        assertTrue(CatalogCategories.matches(supplement,"Thực phẩm"));
        assertTrue(CatalogCategories.matches(supplement,"Thực phẩm bổ sung"));
        assertFalse(supplement.requiresPrescription);
    }
}
