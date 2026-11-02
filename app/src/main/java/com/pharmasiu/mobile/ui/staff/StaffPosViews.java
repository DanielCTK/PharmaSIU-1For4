package com.pharmasiu.mobile.ui.staff;

import android.app.Activity;
import android.view.View;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.staff.pos.PosPresenter;

/** Stable Java/XML POS entrypoint; implementations live in the ui.staff.pos package. */
public final class StaffPosViews {
    private StaffPosViews(){}
    public static View create(Activity activity, UiHost host, String route) {
        View root=activity.getLayoutInflater().inflate(R.layout.staff_pos_page,null,false);
        new PosPresenter(activity,host,root).bind();
        return root;
    }
}
