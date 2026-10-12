package com.codex.fullm9;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.codex.lmport.MomentBridge;
import com.codex.lmport.PreviewTone;
import com.codex.lmport.ToneBadge;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class FullM9Ui {
    public static final String NAME = "完美移植m9";
    private static final String PREF = "lmoment_v101";
    private static Bitmap poster;

    public static int[] choice(Context context) {
        if (context == null) {
            return new int[2];
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF, 0);
        int i = 8;
        if (!sharedPreferences.getBoolean("fullm9_defaults_v1", false)) {
            sharedPreferences.edit().putInt("style", 8).putBoolean("fullm9_defaults_v1", true).commit();
        }
        int i2 = sharedPreferences.getInt("style", 8);
        int[] iArr = new int[2];
        if (i2 >= 0 && i2 <= 8) {
            i = i2;
        }
        iArr[0] = i;
        return iArr;
    }

    public static String modelName(int i) {
        return i == 8 ? NAME : MomentBridge.modelNameLegacy(i);
    }

    public static String[] galleryNames() {
        String[] strArrGalleryNamesLegacy = MomentBridge.galleryNamesLegacy();
        String[] strArr = new String[strArrGalleryNamesLegacy.length + 1];
        strArr[0] = NAME;
        System.arraycopy(strArrGalleryNamesLegacy, 0, strArr, 1, strArrGalleryNamesLegacy.length);
        return strArr;
    }

    public static int galleryModelIndex(int i) {
        if (i == 0) {
            return 8;
        }
        return MomentBridge.galleryModelIndexLegacy(i - 1);
    }

    public static int gallerySelection(Context context, int i) {
        int i2 = choice(context)[0];
        for (int i3 = 0; i3 < 9; i3++) {
            if (galleryModelIndex(i3) == i2) {
                return i3;
            }
        }
        return 0;
    }

    public static synchronized Bitmap gallerySample(Context context, int i) throws IOException {
        if (i != 0) {
            return MomentBridge.gallerySampleLegacy(context, i - 1);
        }
        if (poster == null || poster.isRecycled()) {
            IOException iOException = null;
            try {
                InputStream inputStreamOpen = context.getAssets().open("fullm9/poster.webp");
                try {
                    poster = BitmapFactory.decodeStream(inputStreamOpen);
                    if (poster == null) {
                        iOException = new IOException("Missing M9 introduction image");
                        throw iOException;
                    }
                    if (inputStreamOpen != null) {
                        inputStreamOpen.close();
                    }
                } catch (Throwable th) {
                    if (inputStreamOpen != null) {
                        inputStreamOpen.close();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                if (iOException == null) {
                    throw th2;
                }
                if (iOException != th2) {
                    iOException.addSuppressed(th2);
                }
                throw iOException;
            }
        }
        return poster;
    }

    public static Bitmap galleryLabel(int i) {
        if (i != 0) {
            return MomentBridge.galleryLabelLegacy(i - 1);
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(640, 72, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint(3);
        paint.setColor(-1);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(46.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        new Canvas(bitmapCreateBitmap).drawText(NAME, 320.0f, 53.0f, paint);
        return bitmapCreateBitmap;
    }

    public static String galleryIntro(String str) {
        if ("apply".equals(str)) {
            return "确认使用";
        }
        return "完美移植m9_intro".equals(str) ? "完美移植m9\n小米Ultra徕卡版徕卡一瞬\n独立M9影调 · 智能降噪 · 清晰输出" : MomentBridge.galleryIntroLegacy(str);
    }

    public static void galleryApply(final Activity activity, Object obj, int i) {
        if (i != 0) {
            MomentBridge.galleryApplyLegacy(activity, obj, i - 1);
            return;
        }
        if (activity == null) {
            return;
        }
        activity.getSharedPreferences(PREF, 0).edit().putInt("style", 8).putBoolean("fullm9_defaults_v1", true).apply();
        PreviewTone.select(8);
        if (obj != null) {
            try {
                Method declaredMethod = obj.getClass().getDeclaredMethod("v43Select", String.class);
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(obj, "M9");
            } catch (Exception e) {
                Log.w("FullM9", "Native selection", e);
            }
        }
        activity.getWindow().getDecorView().post(new Runnable() { // from class: com.codex.fullm9.FullM9Ui.1
            @Override // java.lang.Runnable
            public void run() {
                FullM9Ui.updateBadge(activity.getWindow().getDecorView());
            }
        });
    }

    public static void updateBadge(View view) {
        Bitmap bitmapGallerySample;
        if (choice(view.getContext())[0] != 8) {
            MomentBridge.updateBadgeLegacy(view);
            return;
        }
        View viewFindViewById = view.findViewById(2131430434);
        if (viewFindViewById instanceof ImageView) {
            ImageView imageView = (ImageView) viewFindViewById;
            try {
                bitmapGallerySample = gallerySample(view.getContext(), 0);
            } catch (IOException e) {
                Log.w("FullM9", "Badge image", e);
                bitmapGallerySample = null;
            }
            imageView.clearColorFilter();
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageDrawable(new ToneBadge(view.getContext(), 8, NAME, bitmapGallerySample));
            imageView.setContentDescription("完美移植m9 影调，点击切换");
            View viewFindViewById2 = view.findViewById(2131430433);
            if (viewFindViewById2 != null) {
                viewFindViewById2.setContentDescription("完美移植m9 影调，点击切换，长按查看后台成片状态");
                BackgroundStatus.bind(viewFindViewById2);
            }
            BackgroundStatus.bind(imageView);
        }
    }

    public static boolean activityResult(Activity activity, int i, int i2, Intent intent) {
        if (i != 19533 || choice(activity)[0] != 8) {
            return MomentBridge.activityResultLegacy(activity, i, i2, intent);
        }
        if (i2 != -1 || intent == null || intent.getData() == null) {
            return true;
        }
        FullM9Import.start(activity, intent.getData());
        return true;
    }
}
