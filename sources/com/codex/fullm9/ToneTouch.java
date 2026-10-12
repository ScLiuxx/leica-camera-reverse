package com.codex.fullm9;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.codex.lmport.MomentBridge;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class ToneTouch {
    private static final WeakHashMap<Activity, Gesture> ACTIVE = new WeakHashMap<>();

    private static final class Gesture {
        View button;
        boolean cancelled;
        float x;
        float y;

        private Gesture() {
        }

        /* synthetic */ Gesture(Gesture gesture) {
            this();
        }
    }

    private static RectF bounds(View view) {
        RectF rectF = new RectF(0.0f, 0.0f, view.getWidth(), view.getHeight());
        Matrix matrix = new Matrix();
        view.transformMatrixToGlobal(matrix);
        matrix.mapRect(rectF);
        int[] iArr = new int[2];
        View rootView = view.getRootView();
        rootView.getLocationOnScreen(iArr);
        int[] iArr2 = new int[2];
        rootView.getLocationInWindow(iArr2);
        rectF.offset(iArr[0] - iArr2[0], iArr[1] - iArr2[1]);
        float f = view.getResources().getDisplayMetrics().density * 48.0f;
        rectF.inset((-Math.max(0.0f, f - rectF.width())) / 2.0f, (-Math.max(0.0f, f - rectF.height())) / 2.0f);
        return rectF;
    }

    /* JADX WARN: Code duplicated, block: B:46:0x00c5 A[Catch: RuntimeException -> 0x00f1, TryCatch #0 {RuntimeException -> 0x00f1, blocks: (B:3:0x0001, B:5:0x0010, B:8:0x001c, B:10:0x0025, B:12:0x002b, B:14:0x0036, B:16:0x003c, B:22:0x0053, B:25:0x005a, B:28:0x0063, B:29:0x006c, B:31:0x0072, B:34:0x0079, B:40:0x0098, B:42:0x00a6, B:44:0x00b6, B:48:0x00c9, B:50:0x00d2, B:52:0x00da, B:54:0x00e2, B:57:0x00eb, B:46:0x00c5), top: B:62:0x0001 }] */
    public static boolean dispatch(Activity activity, MotionEvent motionEvent) {
        View viewFindViewById;
        try {
            int actionMasked = motionEvent.getActionMasked();
            Gesture gesture = ACTIVE.get(activity);
            if (actionMasked == 0) {
                ACTIVE.remove(activity);
                if (MomentBridge.leicaMode() && (viewFindViewById = activity.findViewById(2131430433)) != null && viewFindViewById.isShown() && viewFindViewById.getAlpha() >= 0.2f && viewFindViewById.getWidth() != 0 && bounds(viewFindViewById).contains(motionEvent.getRawX(), motionEvent.getRawY())) {
                    for (int i = 0; i < 4 && !viewFindViewById.hasOnClickListeners(); i++) {
                        if (!(viewFindViewById.getParent() instanceof View)) {
                            return false;
                        }
                        viewFindViewById = (View) viewFindViewById.getParent();
                    }
                    if (viewFindViewById.hasOnClickListeners() && viewFindViewById.isEnabled()) {
                        Gesture gesture2 = new Gesture(null);
                        gesture2.button = viewFindViewById;
                        gesture2.x = motionEvent.getRawX();
                        gesture2.y = motionEvent.getRawY();
                        ACTIVE.put(activity, gesture2);
                        return true;
                    }
                    return false;
                }
                return false;
            }
            if (gesture == null) {
                return false;
            }
            int scaledTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
            if (motionEvent.getPointerCount() == 1) {
                float f = scaledTouchSlop;
                if (Math.abs(motionEvent.getRawX() - gesture.x) > f || Math.abs(motionEvent.getRawY() - gesture.y) > f) {
                    gesture.cancelled = true;
                }
            } else {
                gesture.cancelled = true;
            }
            if (actionMasked == 1) {
                ACTIVE.remove(activity);
                if (!gesture.cancelled && gesture.button.isShown() && gesture.button.isEnabled()) {
                    gesture.button.performClick();
                }
            } else if (actionMasked == 3) {
                ACTIVE.remove(activity);
            }
            return true;
        } catch (RuntimeException e) {
            ACTIVE.remove(activity);
            Log.w("ToneTouch", "Native touch fallback", e);
            return false;
        }
    }
}
