package com.codex.leicaapp;

import android.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.codex.lmport.MomentBridge;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class LeicaV43Ui {
    private static final String[] STYLES = MomentBridge.galleryNames();
    private final Activity activity;
    private final Bitmap[] cameras;
    private final float density;
    private final Dialog dialog;
    private final Object owner;
    private final Bitmap[] samples;
    private int selected;

    /* JADX INFO: renamed from: lambda$open$4$com-codex-leicaapp-LeicaV43Ui, reason: not valid java name */
    /* synthetic */ void m14lambda$open$4$comcodexleicaappLeicaV43Ui(View view) {
        MomentBridge.galleryApply(this.activity, this.owner, this.selected);
        this.dialog.dismiss();
    }

    private LeicaV43Ui(Activity activity, Object obj) {
        int length = MomentBridge.galleryNames().length;
        this.samples = new Bitmap[length];
        this.cameras = new Bitmap[length];
        this.activity = activity;
        this.owner = obj;
        this.density = activity.getResources().getDisplayMetrics().density;
        this.selected = MomentBridge.gallerySelection(activity, "M3".equals(call("v43Value", new Object[0])) ? 1 : 0);
        this.dialog = new Dialog(activity, R.style.Theme.Material.NoActionBar);
    }

    public static Dialog show(Activity activity, Object obj) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return null;
        }
        LeicaV43Ui leicaV43Ui = new LeicaV43Ui(activity, obj);
        try {
            leicaV43Ui.open();
            return leicaV43Ui.dialog;
        } catch (Exception e) {
            Log.e("LeicaV43Ui", "Cannot open source UI", e);
            leicaV43Ui.dialog.dismiss();
            leicaV43Ui.call("v43Close", new Object[0]);
            return null;
        }
    }

    private Object call(String str, Object... objArr) {
        try {
            return this.owner.getClass().getMethod(str, objArr.length == 0 ? new Class[0] : new Class[]{String.class}).invoke(this.owner, objArr);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(str, e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int dp(float f) {
        return Math.round(f * this.density);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String text(String str) {
        String strGalleryIntro = MomentBridge.galleryIntro(str);
        if (strGalleryIntro != null) {
            return strGalleryIntro;
        }
        return this.activity.getString(this.activity.getResources().getIdentifier("leica_v43_" + str, "string", this.activity.getPackageName()));
    }

    private Bitmap asset(String str) throws IOException {
        InputStream inputStreamOpen = this.activity.getAssets().open("leica-v43-ui/" + str + ".webp");
        try {
            Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(inputStreamOpen);
            if (bitmapDecodeStream == null) {
                throw new IOException(str);
            }
            if (inputStreamOpen != null) {
                inputStreamOpen.close();
            }
            return bitmapDecodeStream;
        } catch (Throwable th) {
            if (inputStreamOpen != null) {
                try {
                    inputStreamOpen.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public GradientDrawable background(int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(8.0f));
        if (i2 != 0) {
            gradientDrawable.setStroke(dp(3.0f), i2);
        }
        return gradientDrawable;
    }

    private LinearLayout.LayoutParams lp(int i, int i2, int i3, int i4, int i5, int i6) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i, i2);
        layoutParams.setMargins(dp(i3), dp(i4), dp(i5), dp(i6));
        return layoutParams;
    }

    private TextView label(String str, float f, int i) {
        TextView textView = new TextView(this.activity);
        textView.setText(str);
        textView.setTextSize(f);
        textView.setTextColor(i);
        textView.setIncludeFontPadding(false);
        return textView;
    }

    private void open() throws IOException {
        for (int i = 0; i < STYLES.length; i++) {
            this.samples[i] = MomentBridge.gallerySample(this.activity, i);
            this.cameras[i] = MomentBridge.galleryLabel(i);
        }
        LinearLayout linearLayout = new LinearLayout(this.activity);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(-16777216);
        linearLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda0
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return LeicaV43Ui.lambda$open$0(view, windowInsets);
            }
        });
        View view = new View(this.activity) { // from class: com.codex.leicaapp.LeicaV43Ui.1
            final Paint paint = new Paint(1);

            @Override // android.view.View
            protected void onDraw(Canvas canvas) {
                this.paint.setColor(-1);
                this.paint.setStrokeWidth(LeicaV43Ui.this.dp(1.6f));
                this.paint.setStrokeCap(Paint.Cap.ROUND);
                float width = getWidth() / 2.0f;
                float height = getHeight() / 2.0f;
                canvas.drawLine(width + LeicaV43Ui.this.dp(4.0f), height - LeicaV43Ui.this.dp(9.0f), width - LeicaV43Ui.this.dp(5.0f), height, this.paint);
                canvas.drawLine(width - LeicaV43Ui.this.dp(5.0f), height, width + LeicaV43Ui.this.dp(4.0f), height + LeicaV43Ui.this.dp(9.0f), this.paint);
            }
        };
        view.setContentDescription(text("back"));
        view.setOnClickListener(new View.OnClickListener() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m12lambda$open$1$comcodexleicaappLeicaV43Ui(view2);
            }
        });
        linearLayout.addView(view, lp(dp(52.0f), dp(44.0f), 12, 6, 0, 0));
        linearLayout.addView(label(text("title"), 32.0f, -1), lp(-1, dp(64.0f), 28, 14, 20, 0));
        Button button = new Button(this.activity);
        button.setText("传奇一瞬 · 原厂模型实验");
        button.setOnClickListener(new View.OnClickListener() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m13lambda$open$2$comcodexleicaappLeicaV43Ui(view2);
            }
        });
        linearLayout.addView(button, lp(-1, dp(48.0f), 28, 0, 28, 0));
        final Gallery gallery = new Gallery(this.activity);
        gallery.setSpacing(dp(20.0f));
        gallery.setGravity(16);
        gallery.setUnselectedAlpha(1.0f);
        gallery.setAnimationDuration(250);
        gallery.setClipToPadding(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
        layoutParams.topMargin = dp(2.0f);
        layoutParams.bottomMargin = dp(12.0f);
        linearLayout.addView(gallery, layoutParams);
        final ImageView imageView = new ImageView(this.activity);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        linearLayout.addView(imageView, lp(-1, dp(31.0f), 0, 0, 0, 10));
        final ScrollView scrollView = new ScrollView(this.activity);
        scrollView.setFillViewport(false);
        final TextView textViewLabel = label("", 14.0f, -6710887);
        textViewLabel.setLineSpacing(dp(2.0f), 1.0f);
        scrollView.addView(textViewLabel, new FrameLayout.LayoutParams(-1, -2));
        linearLayout.addView(scrollView, lp(-1, dp(70.0f), 40, 8, 40, 0));
        TextView textViewLabel2 = label(text("apply"), 17.0f, -1);
        textViewLabel2.setGravity(17);
        GradientDrawable gradientDrawableBackground = background(-14342875, 0);
        gradientDrawableBackground.setCornerRadius(dp(16.0f));
        textViewLabel2.setBackground(gradientDrawableBackground);
        textViewLabel2.setClickable(true);
        linearLayout.addView(textViewLabel2, lp(-1, dp(50.0f), 30, 24, 30, 28));
        final BaseAdapter baseAdapter = new BaseAdapter() { // from class: com.codex.leicaapp.LeicaV43Ui.2
            @Override // android.widget.Adapter
            public int getCount() {
                return LeicaV43Ui.STYLES.length;
            }

            @Override // android.widget.Adapter
            public Object getItem(int i2) {
                return LeicaV43Ui.STYLES[i2];
            }

            @Override // android.widget.Adapter
            public long getItemId(int i2) {
                return i2;
            }

            @Override // android.widget.Adapter
            public View getView(int i2, View view2, ViewGroup viewGroup) {
                int iMin = Math.min(LeicaV43Ui.this.activity.getResources().getDisplayMetrics().widthPixels - LeicaV43Ui.this.dp(100.0f), (((gallery.getHeight() > 0 ? gallery.getHeight() : LeicaV43Ui.this.dp(450.0f)) - LeicaV43Ui.this.dp(12.0f)) * LeicaV43Ui.this.samples[i2].getWidth()) / LeicaV43Ui.this.samples[i2].getHeight());
                int height = (LeicaV43Ui.this.samples[i2].getHeight() * iMin) / LeicaV43Ui.this.samples[i2].getWidth();
                FrameLayout frameLayout = new FrameLayout(LeicaV43Ui.this.activity);
                frameLayout.setPadding(LeicaV43Ui.this.dp(3.0f), LeicaV43Ui.this.dp(3.0f), LeicaV43Ui.this.dp(3.0f), LeicaV43Ui.this.dp(3.0f));
                LeicaV43Ui leicaV43Ui = LeicaV43Ui.this;
                frameLayout.setBackground(leicaV43Ui.background(-16777216, i2 == leicaV43Ui.selected ? -1 : 0));
                frameLayout.setLayoutParams(new Gallery.LayoutParams(iMin + LeicaV43Ui.this.dp(6.0f), height + LeicaV43Ui.this.dp(6.0f)));
                ImageView imageView2 = new ImageView(LeicaV43Ui.this.activity);
                imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView2.setImageBitmap(LeicaV43Ui.this.samples[i2]);
                imageView2.setBackground(LeicaV43Ui.this.background(-16777216, 0));
                imageView2.setClipToOutline(true);
                frameLayout.addView(imageView2, new FrameLayout.LayoutParams(-1, -1));
                frameLayout.setContentDescription("LEICA " + LeicaV43Ui.STYLES[i2]);
                frameLayout.setSelected(i2 == LeicaV43Ui.this.selected);
                return frameLayout;
            }
        };
        gallery.setAdapter((SpinnerAdapter) baseAdapter);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: com.codex.leicaapp.LeicaV43Ui.3
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view2, int i2, long j) {
                LeicaV43Ui.this.selected = i2;
                imageView.setImageBitmap(LeicaV43Ui.this.cameras[i2]);
                imageView.setContentDescription("LEICA " + LeicaV43Ui.STYLES[i2]);
                textViewLabel.setText(LeicaV43Ui.this.text(LeicaV43Ui.STYLES[i2].toLowerCase() + "_intro"));
                scrollView.scrollTo(0, 0);
                for (int i3 = 0; i3 < gallery.getChildCount(); i3++) {
                    View childAt = gallery.getChildAt(i3);
                    boolean z = gallery.getPositionForView(childAt) == LeicaV43Ui.this.selected;
                    childAt.setBackground(LeicaV43Ui.this.background(-16777216, z ? -1 : 0));
                    childAt.setSelected(z);
                }
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda3
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view2, int i2, long j) {
                gallery.setSelection(i2, true);
            }
        });
        gallery.setSelection(this.selected);
        textViewLabel2.setOnClickListener(new View.OnClickListener() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m14lambda$open$4$comcodexleicaappLeicaV43Ui(view2);
            }
        });
        this.dialog.setContentView(linearLayout);
        this.dialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda5
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                this.f$0.m15lambda$open$5$comcodexleicaappLeicaV43Ui(dialogInterface);
            }
        });
        Window window = this.dialog.getWindow();
        window.setBackgroundDrawableResource(R.color.black);
        window.clearFlags(1026);
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(-16777216);
        window.setNavigationBarColor(-16777216);
        window.getDecorView().setSystemUiVisibility(0);
        this.dialog.show();
        window.setLayout(-1, -1);
        gallery.post(new Runnable() { // from class: com.codex.leicaapp.LeicaV43Ui$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                baseAdapter.notifyDataSetChanged();
            }
        });
    }

    static /* synthetic */ WindowInsets lambda$open$0(View view, WindowInsets windowInsets) {
        view.setPadding(windowInsets.getSystemWindowInsetLeft(), windowInsets.getSystemWindowInsetTop(), windowInsets.getSystemWindowInsetRight(), windowInsets.getSystemWindowInsetBottom());
        return windowInsets.consumeSystemWindowInsets();
    }

    /* JADX INFO: renamed from: lambda$open$1$com-codex-leicaapp-LeicaV43Ui, reason: not valid java name */
    /* synthetic */ void m12lambda$open$1$comcodexleicaappLeicaV43Ui(View view) {
        this.dialog.dismiss();
    }

    /* JADX INFO: renamed from: lambda$open$2$com-codex-leicaapp-LeicaV43Ui, reason: not valid java name */
    /* synthetic */ void m13lambda$open$2$comcodexleicaappLeicaV43Ui(View view) {
        this.activity.startActivity(new Intent(this.activity, (Class<?>) LegendExperimentActivity.class));
    }

    /* JADX INFO: renamed from: lambda$open$5$com-codex-leicaapp-LeicaV43Ui, reason: not valid java name */
    /* synthetic */ void m15lambda$open$5$comcodexleicaappLeicaV43Ui(DialogInterface dialogInterface) {
        call("v43Close", new Object[0]);
    }
}
