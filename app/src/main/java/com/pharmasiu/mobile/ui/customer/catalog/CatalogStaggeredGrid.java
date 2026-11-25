package com.pharmasiu.mobile.ui.customer.catalog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.pharmasiu.mobile.R;

import java.util.ArrayList;

/** Natural-height cards, fixed equal columns, and logical left-to-right child order. */
final class CatalogStaggeredGrid extends ViewGroup {
    private final AccessibilityManager accessibility;
    private final AccessibilityManager.TouchExplorationStateChangeListener explorationChanged = enabled -> requestLayout();
    private final int gutter;
    private final int rowGap;
    private final int stagger;
    private int columns = 1;
    private int columnWidth;

    CatalogStaggeredGrid(Context context) {
        super(context);
        accessibility = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        gutter = dp(12);
        rowGap = dp(18);
        stagger = dp(18);
        setClipToPadding(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private int dp(float value) { return Math.round(getResources().getDisplayMetrics().density * value); }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getMode(widthSpec) == MeasureSpec.UNSPECIFIED
            ? dp(320) : MeasureSpec.getSize(widthSpec);
        int available = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        boolean singleColumn = getResources().getConfiguration().screenWidthDp < 360
            || getResources().getConfiguration().fontScale >= 1.3f
            || (accessibility != null && accessibility.isTouchExplorationEnabled());
        columns = singleColumn || (available - gutter) / 2 < dp(144) ? 1 : 2;
        columnWidth = Math.max(0, (available - (columns - 1) * gutter) / columns);
        int[] bottoms = {getPaddingTop(), getPaddingTop() + (columns == 2 ? stagger : 0)};
        int visible = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int column = visible++ % columns;
            View media = child.findViewById(R.id.catalog_v2_card_media);
            if (media != null) {
                // Match media proportions to the measured column, never bitmap dimensions.
                media.getLayoutParams().height = columns == 1 ? dp(220)
                    : Math.min(dp(232), Math.max(dp(140), columnWidth));
            }
            child.measure(MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            bottoms[column] += child.getMeasuredHeight() + rowGap;
        }
        int content = visible == 0 ? getPaddingTop()
            : Math.max(bottoms[0], columns == 2 ? bottoms[1] : 0) - rowGap;
        setMeasuredDimension(resolveSize(width, widthSpec),
            resolveSize(content + getPaddingBottom(), heightSpec));
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int[] nextY = {getPaddingTop(), getPaddingTop() + (columns == 2 ? stagger : 0)};
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int visible = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int column = visible++ % columns;
            int x = rtl ? getWidth() - getPaddingRight() - columnWidth - column * (columnWidth + gutter)
                : getPaddingLeft() + column * (columnWidth + gutter);
            child.layout(x, nextY[column], x + columnWidth, nextY[column] + child.getMeasuredHeight());
            nextY[column] += child.getMeasuredHeight() + rowGap;
        }
    }

    @Override public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child.getId() == NO_ID) child.setId(View.generateViewId());
        int index = indexOfChild(child);
        if (index > 0) child.setAccessibilityTraversalAfter(getChildAt(index - 1).getId());
        child.setAccessibilityDelegate(new AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                int current = indexOfChild(host);
                if (current >= 0) info.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(
                    current / columns, 1, current % columns, 1, false));
            }
        });
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(
            (getChildCount() + columns - 1) / columns, columns, false));
    }

    @Override public void addChildrenForAccessibility(ArrayList<View> children) {
        // Keep the data's row-major order instead of grouping all left-column cards first.
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE) children.add(child);
        }
    }

    @Override public View focusSearch(View focused, int direction) {
        if (direction == FOCUS_FORWARD || direction == FOCUS_BACKWARD) {
            ArrayList<View> order = new ArrayList<>();
            for (int i = 0; i < getChildCount(); i++) collectFocusables(getChildAt(i), order);
            int current = order.indexOf(focused);
            int next = current + (direction == FOCUS_FORWARD ? 1 : -1);
            if (current >= 0 && next >= 0 && next < order.size()) return order.get(next);
        }
        return super.focusSearch(focused, direction);
    }

    private void collectFocusables(View view, ArrayList<View> order) {
        if (view.getVisibility() != VISIBLE) return;
        if (view.isEnabled() && view.isFocusable()) order.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) collectFocusables(group.getChildAt(i), order);
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (accessibility != null) accessibility.addTouchExplorationStateChangeListener(explorationChanged);
        requestLayout();
    }

    @Override protected void onDetachedFromWindow() {
        if (accessibility != null) accessibility.removeTouchExplorationStateChangeListener(explorationChanged);
        super.onDetachedFromWindow();
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
