package com.codex.leicaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class CameraLauncher extends Activity {
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            startActivity(new Intent("android.intent.action.MAIN").setClassName(this, "com.android.camera.Camera").addFlags(603979776));
        } finally {
            finish();
        }
    }
}
