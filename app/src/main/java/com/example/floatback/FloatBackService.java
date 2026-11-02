package com.example.floatback;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

/** Floating round button shown above every app. Tap = Back, long-press = Home, drag = move. */
public class FloatBackService extends AccessibilityService {
    private static final String PREFS = "pos";
    private static final long LONG_PRESS_MS = 700;

    private WindowManager wm;
    private TextView button;
    private WindowManager.LayoutParams lp;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean longFired;
    private boolean moving;
    private float downX, downY;
    private int startX, startY;

    private final Runnable longPress = new Runnable() {
        @Override public void run() {
            longFired = true;
            performGlobalAction(GLOBAL_ACTION_HOME);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        addButton();
    }

    private void addButton() {
        if (button != null || wm == null) return;
        final float d = getResources().getDisplayMetrics().density;
        final int size = (int) (56 * d);

        button = new TextView(this);
        button.setText("\u25C0");
        button.setTextColor(Color.WHITE);
        button.setTextSize(22);
        button.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xB0202020);
        bg.setStroke((int) (2 * d), 0xCCFFFFFF);
        button.setBackground(bg);
        button.setAlpha(0.75f);

        DisplayMetrics m = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(m);
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        int defX = m.widthPixels - size - (int) (16 * d);
        int defY = m.heightPixels / 2;

        lp = new WindowManager.LayoutParams(
                size, size,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = clamp(sp.getInt("x", defX), 0, Math.max(0, m.widthPixels - size));
        lp.y = clamp(sp.getInt("y", defY), 0, Math.max(0, m.heightPixels - size));

        final int slop = ViewConfiguration.get(this).getScaledTouchSlop();
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = e.getRawX(); downY = e.getRawY();
                        startX = lp.x; startY = lp.y;
                        moving = false; longFired = false;
                        handler.postDelayed(longPress, LONG_PRESS_MS);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - downX, dy = e.getRawY() - downY;
                        if (!moving && (Math.abs(dx) > slop || Math.abs(dy) > slop)) {
                            moving = true;
                            handler.removeCallbacks(longPress);
                        }
                        if (moving) {
                            DisplayMetrics mm = new DisplayMetrics();
                            wm.getDefaultDisplay().getRealMetrics(mm);
                            lp.x = clamp(startX + (int) dx, 0, Math.max(0, mm.widthPixels - lp.width));
                            lp.y = clamp(startY + (int) dy, 0, Math.max(0, mm.heightPixels - lp.height));
                            try { wm.updateViewLayout(button, lp); } catch (Exception ignored) { }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(longPress);
                        if (moving) {
                            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                                    .putInt("x", lp.x).putInt("y", lp.y).apply();
                        } else if (!longFired) {
                            performGlobalAction(GLOBAL_ACTION_BACK);
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPress);
                        return true;
                }
                return false;
            }
        });

        try {
            wm.addView(button, lp);
        } catch (Exception ex) {
            button = null;
        }
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    private void removeButton() {
        handler.removeCallbacks(longPress);
        if (button != null && wm != null) {
            try { wm.removeView(button); } catch (Exception ignored) { }
        }
        button = null;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override public boolean onUnbind(android.content.Intent intent) { removeButton(); return super.onUnbind(intent); }
    @Override public void onDestroy() { removeButton(); super.onDestroy(); }
}
