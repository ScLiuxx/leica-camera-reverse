package com.codex.leicaapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class LegendExperimentActivity extends Activity {
    private volatile Process process;
    private TextView status;
    private Button test;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        int iRound = Math.round(getResources().getDisplayMetrics().density * 24.0f);
        linearLayout.setPadding(iRound, iRound * 2, iRound, iRound);
        TextView textView = new TextView(this);
        textView.setText("传奇一瞬 · 原厂模型实验");
        textView.setTextSize(23.0f);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this);
        this.status = textView2;
        textView2.setText("V85 → V81\n当前试验仍未通过 DSP 图加载，暂不能用于拍照。\n独立检查 M9 原厂模型加载结果，不会生成备用效果照片。");
        linearLayout.addView(this.status);
        Button button = new Button(this);
        this.test = button;
        button.setText("检查本机模型兼容性");
        linearLayout.addView(this.test);
        this.test.setOnClickListener(new View.OnClickListener() { // from class: com.codex.leicaapp.LegendExperimentActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m10lambda$onCreate$0$comcodexleicaappLegendExperimentActivity(view);
            }
        });
        Button button2 = new Button(this);
        button2.setText("返回拍摄");
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.codex.leicaapp.LegendExperimentActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m11lambda$onCreate$1$comcodexleicaappLegendExperimentActivity(view);
            }
        });
        linearLayout.addView(button2);
        setContentView(linearLayout);
    }

    /* JADX INFO: renamed from: lambda$onCreate$0$com-codex-leicaapp-LegendExperimentActivity, reason: not valid java name */
    /* synthetic */ void m10lambda$onCreate$0$comcodexleicaappLegendExperimentActivity(View view) {
        this.test.setEnabled(false);
        this.status.setText("正在检查…");
        new Thread(new Runnable() { // from class: com.codex.leicaapp.LegendExperimentActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.check();
            }
        }, "legend85-check").start();
    }

    /* JADX INFO: renamed from: lambda$onCreate$1$com-codex-leicaapp-LegendExperimentActivity, reason: not valid java name */
    /* synthetic */ void m11lambda$onCreate$1$comcodexleicaappLegendExperimentActivity(View view) {
        finish();
    }

    /* JADX WARN: Code duplicated, block: B:36:0x005f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private void copy(String str, File file) throws IOException {
        File parentFile = file.getParentFile();
        if (!parentFile.isDirectory() && !parentFile.mkdirs()) {
            throw new IOException("Cannot create model directory");
        }
        InputStream inputStreamOpen = getAssets().open("legend85-experiment/" + str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                byte[] bArr = new byte[65536];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i == -1) {
                        break;
                    } else {
                        fileOutputStream.write(bArr, 0, i);
                    }
                    if (inputStreamOpen != null) {
                        try {
                            inputStreamOpen.close();
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    }
                    throw th;
                }
                fileOutputStream.close();
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
            } catch (Throwable th2) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th3) {
                    th2.addSuppressed(th3);
                }
                throw th2;
            }
        } catch (Throwable th4) {
            if (inputStreamOpen != null) {
                inputStreamOpen.close();
            }
            throw th4;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void check() {
        final String str;
        try {
            try {
                File file = new File(getFilesDir(), "legend85-experiment");
                String[] strArr = {"m9-v81.minn", "libQnnSystem.so", "libQnnHtp.so", "libQnnHtpV81Stub.so", "libQnnHtpV81Skel.so"};
                for (int i = 0; i < 5; i++) {
                    String str2 = strArr[i];
                    copy(str2, new File(file, str2));
                }
                File file2 = new File(getCacheDir(), "legend85-last.log");
                ProcessBuilder processBuilder = new ProcessBuilder(getApplicationInfo().nativeLibraryDir + "/liblegend85_probe.so", new File(file, "libQnnSystem.so").getPath(), new File(file, "m9-v81.minn").getPath(), new File(file, "libQnnHtp.so").getPath());
                processBuilder.environment().put("LD_LIBRARY_PATH", file.getPath() + ":" + getApplicationInfo().nativeLibraryDir);
                processBuilder.environment().put("ADSP_LIBRARY_PATH", file.getPath() + ";/vendor/lib/rfsa/adsp;/vendor/dsp/cdsp");
                processBuilder.redirectErrorStream(true);
                processBuilder.redirectOutput(file2);
                this.process = processBuilder.start();
                if (!this.process.waitFor(30L, TimeUnit.SECONDS)) {
                    this.process.destroyForcibly();
                    throw new IOException("模型检查超时");
                }
                StringBuilder sb = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file2));
                while (true) {
                    try {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.startsWith("getBinaryInfo=") || line.startsWith("HTP context") || line.startsWith("backendCreate=") || line.startsWith("deviceCreate=") || line.startsWith("contextCreateFromBinary=") || line.contains("unsupported") || line.contains("contextFromBin Failed")) {
                            sb.append(line).append('\n');
                        }
                    } catch (Throwable th) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                        throw th;
                    }
                }
                bufferedReader.close();
                if (sb.length() == 0) {
                    sb = new StringBuilder().append("模型检查失败，进程返回 ").append(this.process.exitValue());
                }
                str = sb.toString() + "\n这只检查图加载；即使加载成功，也需要继续验证推理输出。";
                this.process = null;
                runOnUiThread(new Runnable() { // from class: com.codex.leicaapp.LegendExperimentActivity$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m9lambda$check$2$comcodexleicaappLegendExperimentActivity(str);
                    }
                });
            } catch (Exception e) {
                str = "模型检查失败：" + e.getMessage();
            }
        } catch (Throwable th3) {
            this.process = null;
            throw th3;
        }
    }

    /* JADX INFO: renamed from: lambda$check$2$com-codex-leicaapp-LegendExperimentActivity, reason: not valid java name */
    /* synthetic */ void m9lambda$check$2$comcodexleicaappLegendExperimentActivity(String str) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        this.status.setText(str);
        this.test.setEnabled(true);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Process process = this.process;
        if (process != null) {
            process.destroyForcibly();
        }
        super.onDestroy();
    }
}
