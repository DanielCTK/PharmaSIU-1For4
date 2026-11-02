package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.*;
/** Offline category artwork: illustrations only, never verified product photography. */
final class CatalogArtwork extends View {
    private final String category;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int background, tint;

    CatalogArtwork(Activity a, DemoState.Product product) {
        super(a); category=product.category;
        switch (category) {
            case "Mỹ phẩm": background=0xFFF9DFE5;tint=0xFFBB7491;break;
            case "Thực phẩm": background=0xFFFFE5BD;tint=0xFFAB8046;break;
            case "Thực phẩm bổ sung": background=0xFFE2EBCD;tint=0xFF819E6B;break;
            case "Thiết bị": background=0xFFDFE9F7;tint=0xFF7195B8;break;
            case "Chăm sóc":background=0xFFD8EDF3;tint=0xFF76B0A5;break;
            default: background=0xFFE2EFE5;tint=0xFF84A999;break;
        }
        setContentDescription("Hình minh họa trừu tượng cho nhóm " + category + ", không phải ảnh sản phẩm thật");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(), h=getHeight();if(w<=0||h<=0)return;
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0,0,w,h,background,0xFFFFFCF6,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
        paint.setColor(0x80FFFFFF);canvas.drawCircle(w*.90f,h*.14f,w*.42f,paint);
        paint.setColor(alpha(tint,.12f));canvas.drawCircle(w*.04f,h*.87f,w*.27f,paint);
        paint.setColor(alpha(tint,.15f));canvas.drawOval(w*.21f,h*.78f,w*.84f,h*.87f,paint);
        if ("Thực phẩm".equals(category)) {
            paint.setColor(alpha(tint,.30f));canvas.drawOval(w*.18f,h*.36f,w*.83f,h*.77f,paint);
            paint.setColor(0xFFFFFCF4);canvas.drawOval(w*.18f,h*.28f,w*.82f,h*.72f,paint);
            paint.setColor(0xFFDFBD88);canvas.drawOval(w*.25f,h*.37f,w*.75f,h*.65f,paint);
            paint.setColor(0xFFF9E6C1);
            for(int i=0;i<12;i++)canvas.drawCircle(w*(.30f+.13f*(i%4)),h*(.40f+.08f*(i/4)),w*.034f,paint);
        } else if ("Vật tư y tế".equals(category)) {
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(w*.018f);
            paint.setColor(alpha(tint,.55f));
            canvas.drawOval(w*.11f,h*.39f,w*.47f,h*.69f,paint);
            canvas.drawOval(w*.57f,h*.39f,w*.88f,h*.69f,paint);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xFFFFFFFF);
            canvas.drawRoundRect(new RectF(w*.22f,h*.36f,w*.79f,h*.72f),w*.07f,w*.07f,paint);
            paint.setColor(alpha(tint,.50f));paint.setStrokeWidth(w*.012f);
            for(int line=0;line<3;line++)canvas.drawRoundRect(new RectF(w*.32f,h*(.46f+.075f*line),
                w*.68f,h*(.47f+.075f*line)),w*.01f,w*.01f,paint);
        } else {
            float left=w*.28f, top=h*.23f, pw=w*.44f, ph=h*.55f;
            paint.setColor(alpha(tint,.70f));
            if (!"Thiết bị".equals(category)) canvas.drawRoundRect(
                new RectF(left+pw*.20f,top-h*.08f,left+pw*.80f,top+h*.03f),w*.03f,w*.03f,paint);
            paint.setColor(alpha(tint,.17f));canvas.drawRoundRect(
                new RectF(left+w*.025f,top+h*.025f,left+pw+w*.025f,top+ph+h*.025f),w*.07f,w*.07f,paint);
            paint.setColor(Color.WHITE);canvas.drawRoundRect(
                new RectF(left,top,left+pw,top+ph),w*.07f,w*.07f,paint);
            paint.setColor(alpha(tint,.72f));canvas.drawRoundRect(
                new RectF(left+pw*.14f,top+ph*.40f,left+pw*.86f,top+ph*.46f),w*.015f,w*.015f,paint);
            paint.setColor(alpha(tint,.13f));canvas.drawRoundRect(
                new RectF(left+pw*.12f,top+ph*.52f,left+pw*.88f,top+ph*.74f),w*.03f,w*.03f,paint);
            paint.setColor(0xB7FFFFFF);canvas.drawRoundRect(
                new RectF(left+pw*.08f,top+ph*.12f,left+pw*.18f,top+ph*.82f),w*.02f,w*.02f,paint);
        }
        paint.setColor(0xDFFFFFFF);
        float badgeWidth=Math.min(w*.72f, getResources().getDisplayMetrics().density*108);
        canvas.drawRoundRect(new RectF((w-badgeWidth)/2,h-26*getResources().getDisplayMetrics().density,
            (w+badgeWidth)/2,h-7*getResources().getDisplayMetrics().density),6,6,paint);
        paint.setColor(MUTED);paint.setTextSize(Math.max(9,w*.045f));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("HÌNH MINH HỌA",w*.5f,h-12*getResources().getDisplayMetrics().density,paint);
    }

    private int alpha(int color,float opacity) {
        return (Math.max(0, Math.min(255,(int)(opacity*255)))<<24)|(color&0xFFFFFF);
    }
}

