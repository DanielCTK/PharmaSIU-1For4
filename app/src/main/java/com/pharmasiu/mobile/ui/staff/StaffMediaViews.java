package com.pharmasiu.mobile.ui.staff;
import android.app.Activity;
import android.net.Uri;
import android.view.View;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.staff.media.StaffPhotoStore;
import com.pharmasiu.mobile.ui.staff.media.StaffMediaPreview;

/** Stable facade; separate RAM photo store and feature-specific previews below ui.staff.media. */
public final class StaffMediaViews {
    private StaffMediaViews(){}
    public static Uri demoPhoto(DemoState state,String productId){
        return StaffPhotoStore.demoPhoto(state,productId);
    }
    public static boolean changeDemoPhoto(DemoState state,String productId,Uri uri,String mime){
        return StaffPhotoStore.changeDemoPhoto(state,productId,uri,mime);
    }
    public static View createPreview(Activity activity,UiHost host,DemoState.Product product,
                                     Uri photo,Runnable replace,Runnable remove,Runnable close){
        return StaffMediaPreview.createPreview(activity,host,product,photo,replace,remove,close);
    }
    public static View createThumbnail(Activity activity,UiHost host,DemoState.Product product,
                                       Uri photo,Runnable open){
        return StaffMediaPreview.createThumbnail(activity,host,product,photo,open);
    }
    public static boolean isAllowedLocalPhoto(String value,String mime){
        return StaffPhotoStore.isAllowedLocalPhoto(value,mime);
    }
    static boolean mayView(String role){return StaffPhotoStore.mayView(role);}
    static boolean mayEdit(String role){return StaffPhotoStore.mayEdit(role);}
}
