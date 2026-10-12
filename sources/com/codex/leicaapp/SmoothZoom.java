package com.codex.leicaapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Looper;
import android.util.Log;
import android.util.Range;
import android.view.animation.LinearInterpolator;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class SmoothZoom {
    private static ValueAnimator animator;
    private static boolean applying;
    private static Object currentOwner;
    private static float destination;

    public static void cancel() {
        ValueAnimator valueAnimator = animator;
        if (valueAnimator != null) {
            animator = null;
            valueAnimator.cancel();
        }
        currentOwner = null;
    }

    public static boolean intercept(final Object obj, float f, int i) {
        if (applying || Looper.myLooper() != Looper.getMainLooper()) {
            return false;
        }
        if (i == 3) {
            cancel();
            return false;
        }
        ValueAnimator valueAnimator = animator;
        if (valueAnimator != null && valueAnimator.isRunning() && obj == currentOwner && f == destination) {
            return true;
        }
        try {
            Class<?> cls = Class.forName("V6.d");
            Method method = cls.getMethod("a1", new Class[0]);
            final Method method2 = cls.getMethod("v0", Float.TYPE, Integer.TYPE);
            final float fFloatValue = ((Number) method.invoke(obj, new Object[0])).floatValue();
            if (Float.isFinite(fFloatValue) && Float.isFinite(f) && fFloatValue > 0.0f && f > 0.0f) {
                final float fFloatValue2 = ((Float) ((Range) cls.getMethod("E0", new Class[0]).invoke(obj, new Object[0])).clamp(Float.valueOf(f))).floatValue();
                cancel();
                if (Math.max(fFloatValue, fFloatValue2) / Math.min(fFloatValue, fFloatValue2) < 1.5f) {
                    return false;
                }
                currentOwner = obj;
                destination = fFloatValue2;
                ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
                animator = valueAnimatorOfFloat;
                valueAnimatorOfFloat.setDuration(Math.min(1000L, Math.max(350L, Math.round(Math.abs(Math.log(fFloatValue2 / fFloatValue)) * 300.0d))));
                valueAnimatorOfFloat.setInterpolator(new LinearInterpolator());
                final int[] iArr = {0};
                valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.codex.leicaapp.SmoothZoom$$ExternalSyntheticLambda0
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                        SmoothZoom.lambda$intercept$0(iArr, method2, obj, fFloatValue, fFloatValue2, valueAnimator2);
                    }
                });
                valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.codex.leicaapp.SmoothZoom.1
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator2) {
                        if (SmoothZoom.animator == animator2) {
                            ValueAnimator unused = SmoothZoom.animator = null;
                            Object unused2 = SmoothZoom.currentOwner = null;
                            Log.i("LeicaZoom", "Transition complete target=" + fFloatValue2 + " updates=" + iArr[0]);
                        }
                    }
                });
                Log.i("LeicaZoom", "Transition " + fFloatValue + " -> " + fFloatValue2 + " durationMs=" + valueAnimatorOfFloat.getDuration());
                valueAnimatorOfFloat.start();
                return true;
            }
            cancel();
            return false;
        } catch (Exception e) {
            cancel();
            Log.w("LeicaZoom", "Use original zoom", e);
            return false;
        }
    }

    static /* synthetic */ void lambda$intercept$0(int[] iArr, Method method, Object obj, float f, float f2, ValueAnimator valueAnimator) {
        if (animator != valueAnimator) {
            return;
        }
        iArr[0] = iArr[0] + 1;
        applying = true;
        try {
            try {
                method.invoke(obj, Float.valueOf(B43Tone.smoothZoom(f, f2, ((Float) valueAnimator.getAnimatedValue()).floatValue())), 0);
            } catch (Exception e) {
                Log.e("LeicaZoom", "Transition failed", e);
                cancel();
            }
        } finally {
            applying = false;
        }
    }
}
