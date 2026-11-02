package com.pharmasiu.mobile.ui.staff;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;
import static org.junit.Assert.*;

/** Pure JVM verification of local-picker media and staff permissions. */
public class StaffMediaViewsTest {
    @Test public void contentProviderImagesAllowedWithoutAnyNetworkAccess() {
        String photo = "content://com.android.providers.media.documents/document/image%3A15";
        for (String type : new String[] {
            "image/jpeg", "image/png", "image/webp", "image/heif",
            "image/heic", "image/gif", "image/bmp"
        }) assertTrue(type, StaffMediaViews.isAllowedLocalPhoto(photo, type));
    }

    @Test public void rejectsExternalNetworkAndLocalFilesRegardlessOfMime() {
        for (String invalid : new String[] {
            "https://cdn.example.com/test.png", "http://example.com/x.jpg",
            "file:///storage/emulated/0/DCIM/a.jpg",
            "data:image/png;base64,SGVsbG8=", "android.resource://example/drawable/a",
            "javascript:alert(1)", "/storage/emulated/0/photo.png"
        }) assertFalse(invalid,
            StaffMediaViews.isAllowedLocalPhoto(invalid, "image/png"));
    }

    @Test public void mimeMustBeARecognizedNativeImageType() {
        String image = "content://media/external/images/media/9";
        for (String invalid : new String[] {
            "application/pdf", "application/octet-stream", "image/svg+xml",
            "image/svg", "text/plain", "video/mp4", "image/x-icon", "",
            "image/jpeg; charset=UTF-8"
        }) assertFalse(invalid, StaffMediaViews.isAllowedLocalPhoto(image, invalid));
        assertFalse(StaffMediaViews.isAllowedLocalPhoto(image, null));
    }

    @Test public void rejectsInvalidAndAmbiguousContentReferences() {
        for (String invalid : new String[] {
            "", "   ", "content:", "content://", "content:///missing-provider.jpg",
            "content://provider", "content://user@provider/img.png",
            "content://provider/with space.jpg", "content://provider/photo.png#fragment",
            "content:\\provider\\photo.png"
        }) assertFalse(invalid, StaffMediaViews.isAllowedLocalPhoto(invalid, "image/png"));
        assertFalse(StaffMediaViews.isAllowedLocalPhoto(null, "image/png"));
        assertFalse(StaffMediaViews.isAllowedLocalPhoto(
            "content://provider/" + "x".repeat(4096), "image/png"));
    }

    @Test public void permitsEncodedPhotoNamesAndValidMimeCase() {
        assertTrue(StaffMediaViews.isAllowedLocalPhoto(
            "content://com.example.provider/document/%E1%BA%A3nh%20s%E1%BA%A3n%20ph%E1%BA%A9m.png",
            "  IMAGE/PNG  "));
    }

    @Test public void onlyStaffMayViewAndOnlyManagerMayEdit() {
        assertFalse(StaffMediaViews.mayView(DemoState.NONE));
        assertFalse(StaffMediaViews.mayView(DemoState.CUSTOMER));
        assertTrue(StaffMediaViews.mayView(DemoState.CASHIER));
        assertTrue(StaffMediaViews.mayView(DemoState.ADMIN));
        assertFalse(StaffMediaViews.mayEdit(DemoState.CASHIER));
        assertFalse(StaffMediaViews.mayEdit(DemoState.CUSTOMER));
        assertTrue(StaffMediaViews.mayEdit(DemoState.ADMIN));
    }
}
