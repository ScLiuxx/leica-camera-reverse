package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class FilmInput {
    static final String FRAGMENT = "precision highp float; varying vec2 uv; uniform sampler2D image; uniform vec2 texel; uniform float mono;uniform vec3 weights;uniform vec3 wb;uniform float warmInput;uniform float gamma;uniform float whitePoint;uniform float detailStrength;uniform float denoise;uniform vec2 tileOrigin;uniform vec2 tileSize;uniform vec2 fullSize;vec3 linearize(vec3 c){return mix(c/12.92,pow((c+.055)/1.055,vec3(2.4)),step(vec3(.04045),c));}vec3 encode(vec3 c){return mix(c*12.92,1.055*pow(max(c,vec3(0.)),vec3(1./2.4))-.055,step(vec3(.0031308),c));}void main(){vec4 pixel=texture2D(image,uv); vec3 c=pixel.rgb;float bilateralRange=.0144;float bilateralMix=.55;if(warmInput>.5){bilateralRange=.0256;bilateralMix=.75;}vec3 sum=vec3(0.);float total=0.;for(int y=-2;y<=2;y++){for(int x=-2;x<=2;x++){vec3 n=texture2D(image,uv+vec2(float(x),float(y))*texel).rgb;vec3 d=n-c;float w=exp(-float(x*x+y*y)/2.88-dot(d,d)/bilateralRange);sum+=n*w;total+=w;}}c=mix(c,sum/total,bilateralMix);if(denoise>.001){vec3 accum=vec3(0.);float count=0.;vec3 guide=vec3(.25,.65,.10);for(int j=-4;j<=4;j++){for(int i=-4;i<=4;i++){vec3 n=texture2D(image,uv+vec2(float(i),float(j))*texel).rgb;vec3 diff=n-c;float dy=dot(diff,guide);vec3 chroma=diff-vec3(dy);float w=exp(-float(i*i+j*j)/10.-dy*dy/.0036-dot(chroma,chroma)/.08);accum+=n*w;count+=w;}}vec3 filtered=accum/max(count,.000001);float cy=dot(c,guide),fy=dot(filtered,guide);c=vec3(mix(cy,fy,.85*denoise))+mix(c-vec3(cy),filtered-vec3(fy),.98*denoise);}vec3 rgb=linearize(c);float y=dot(rgb,weights);float l=encode(vec3(y)).r;float detail=encode(vec3(dot(linearize(pixel.rgb),weights))).r-l;float stretched=.65+(l-.65)*.35/max(.20,whitePoint-.65);l=mix(l,min(1.,stretched),smoothstep(.65,.85,l));float mask=smoothstep(.06,.25,l)*(1.-smoothstep(.65,.94,l));vec2 p=((tileOrigin+uv*tileSize)/fullSize-.5)*2.;float center=pow(max(0.,1.-dot(p,p)),2.);l*=1.+.12*center*mask;mask=smoothstep(.06,.25,l)*(1.-smoothstep(.65,.94,l));float base=mix(pow(l,1.12),l,smoothstep(.035,.16,l));float t=base+(pow(l,1./gamma)-l)*mask;t+=clamp(detail,-.02,.02)*detailStrength*mask;if(warmInput>.5)t=encode(vec3(y)).r;float gain=min(linearize(vec3(clamp(t,0.,1.))).r/max(y,.000001),1./max(max(rgb.r,max(rgb.g,rgb.b)),.000001));rgb*=gain;float before=dot(rgb,weights);float neutral=1.-smoothstep(.015,.12,max(rgb.r,max(rgb.g,rgb.b))-min(rgb.r,min(rgb.g,rgb.b)));float rg=rgb.g/max(rgb.r,.000001);float skin=smoothstep(.3,.55,rg)*(1.-smoothstep(.9,1.,rg))*step(rgb.b,rgb.g)*step(rgb.g,rgb.r);vec3 balanced=rgb*mix(vec3(1.),mix(wb,vec3(1.),max(.65*neutral,.45*skin)),smoothstep(.01,.08,before)*(1.-smoothstep(.75,.98,before)));balanced*=before/max(dot(balanced,weights),.000001);balanced/=max(1.,max(balanced.r,max(balanced.g,balanced.b)));c=encode(balanced);if(mono>.5)c=vec3(dot(c,vec3(.46,.48,.06)));gl_FragColor=vec4(clamp(c,0.,1.),pixel.a);}";
    private static final int HALO = 4;
    private static final int TILE = 1024;

    private static int[] histogram(Bitmap bitmap) {
        float fMin = Math.min(1.0f, 128.0f / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth() * fMin)), Math.max(1, Math.round(bitmap.getHeight() * fMin)), false);
        try {
            ByteBuffer byteBufferAllocate = ByteBuffer.allocate(bitmapCreateScaledBitmap.getByteCount());
            bitmapCreateScaledBitmap.copyPixelsToBuffer(byteBufferAllocate);
            boolean zEquals = ColorSpace.get(ColorSpace.Named.DISPLAY_P3).equals(bitmapCreateScaledBitmap.getColorSpace());
            int[] iArr = new int[256];
            for (int i = 0; i < bitmapCreateScaledBitmap.getHeight(); i++) {
                for (int i2 = 0; i2 < bitmapCreateScaledBitmap.getWidth(); i2++) {
                    int rowBytes = (bitmapCreateScaledBitmap.getRowBytes() * i) + (i2 * HALO);
                    int iMax = Math.max(0, Math.min(255, Math.round(FilmTone.luma((byteBufferAllocate.get(rowBytes) & 255) / 255.0f, (byteBufferAllocate.get(rowBytes + 1) & 255) / 255.0f, (byteBufferAllocate.get(rowBytes + 2) & 255) / 255.0f, zEquals) * 255.0f)));
                    iArr[iMax] = iArr[iMax] + 1;
                }
            }
            if (bitmapCreateScaledBitmap != bitmap) {
                bitmapCreateScaledBitmap.recycle();
            }
            return iArr;
        } catch (Throwable th) {
            if (bitmapCreateScaledBitmap != bitmap) {
                bitmapCreateScaledBitmap.recycle();
            }
            throw th;
        }
    }

    private static int shader(int i, String str) {
        int iGlCreateShader = GLES20.glCreateShader(i);
        GLES20.glShaderSource(iGlCreateShader, str);
        GLES20.glCompileShader(iGlCreateShader);
        int[] iArr = {0};
        GLES20.glGetShaderiv(iGlCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return iGlCreateShader;
        }
        String strGlGetShaderInfoLog = GLES20.glGetShaderInfoLog(iGlCreateShader);
        GLES20.glDeleteShader(iGlCreateShader);
        throw new IllegalStateException(strGlGetShaderInfoLog);
    }

    public static Bitmap apply(Bitmap bitmap, boolean z) {
        return apply(bitmap, z, Float.NaN);
    }

    public static Bitmap apply(Bitmap bitmap, boolean z, float f) {
        return apply(bitmap, z, f, new float[]{1.0f, 1.0f, 1.0f});
    }

    public static Bitmap apply(Bitmap bitmap, boolean z, float f, float[] fArr) {
        int[] iArrHistogram = histogram(bitmap);
        float fGamma = FilmTone.gamma(iArrHistogram, f);
        Log.i("LegendaryPost", "Midtone gamma=" + fGamma + " ISO=" + f + " median=" + FilmTone.percentile(iArrHistogram, 0.5f));
        float fWhitePoint = FilmTone.whitePoint(iArrHistogram);
        Log.i("LegendaryPost", "Input whitePoint=" + fWhitePoint);
        float fDetailStrength = FilmTone.detailStrength(iArrHistogram, f);
        if (z) {
            fArr = new float[]{1.0f, 1.0f, 1.0f};
        }
        return render(bitmap, z, fGamma, fWhitePoint, fDetailStrength, fArr, FilmTone.denoise(f), false);
    }

    public static Bitmap applyWarmGray(Bitmap bitmap, float f) {
        Bitmap bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (bitmapCopy == null) {
            throw new IllegalStateException("M9 input copy failed");
        }
        return bitmapCopy;
    }

    /* JADX WARN: Code duplicated, block: B:121:0x0355  */
    /* JADX WARN: Code duplicated, block: B:124:0x035c  */
    /* JADX WARN: Code duplicated, block: B:126:0x0369  */
    /* JADX WARN: Code duplicated, block: B:129:0x0370  */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r10v0, types: [android.opengl.EGLDisplay] */
    /* JADX WARN: Type inference failed for: r10v1 */
    /* JADX WARN: Type inference failed for: r10v10 */
    /* JADX WARN: Type inference failed for: r10v11 */
    /* JADX WARN: Type inference failed for: r10v2 */
    /* JADX WARN: Type inference failed for: r10v3 */
    /* JADX WARN: Type inference failed for: r10v8 */
    /* JADX WARN: Type inference failed for: r10v9 */
    /* JADX WARN: Type inference failed for: r11v0, types: [android.opengl.EGLContext] */
    /* JADX WARN: Type inference failed for: r11v11 */
    /* JADX WARN: Type inference failed for: r11v2 */
    /* JADX WARN: Type inference failed for: r11v21 */
    /* JADX WARN: Type inference failed for: r11v22 */
    /* JADX WARN: Type inference failed for: r11v23 */
    /* JADX WARN: Type inference failed for: r11v24 */
    /* JADX WARN: Type inference failed for: r11v25 */
    /* JADX WARN: Type inference failed for: r11v26 */
    /* JADX WARN: Type inference failed for: r11v27 */
    /* JADX WARN: Type inference failed for: r11v3 */
    /* JADX WARN: Type inference failed for: r11v4, types: [android.opengl.EGLContext] */
    /* JADX WARN: Type inference failed for: r11v6 */
    /* JADX WARN: Type inference failed for: r12v0, types: [android.opengl.EGLSurface] */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r12v20 */
    /* JADX WARN: Type inference failed for: r12v21 */
    /* JADX WARN: Type inference failed for: r12v22 */
    /* JADX WARN: Type inference failed for: r12v23 */
    /* JADX WARN: Type inference failed for: r12v24 */
    /* JADX WARN: Type inference failed for: r12v25 */
    /* JADX WARN: Type inference failed for: r12v26 */
    /* JADX WARN: Type inference failed for: r12v27 */
    /* JADX WARN: Type inference failed for: r12v3, types: [android.opengl.EGLSurface] */
    /* JADX WARN: Type inference failed for: r12v6 */
    /* JADX WARN: Type inference failed for: r13v0 */
    /* JADX WARN: Type inference failed for: r13v1 */
    /* JADX WARN: Type inference failed for: r13v12 */
    /* JADX WARN: Type inference failed for: r13v2 */
    /* JADX WARN: Type inference failed for: r13v5 */
    /* JADX WARN: Type inference failed for: r13v8 */
    /* JADX WARN: Type inference failed for: r14v0 */
    /* JADX WARN: Type inference failed for: r14v1, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r14v2 */
    /* JADX WARN: Type inference failed for: r14v3 */
    /* JADX WARN: Type inference failed for: r14v8 */
    /* JADX WARN: Type inference failed for: r17v1 */
    /* JADX WARN: Type inference failed for: r17v10 */
    /* JADX WARN: Type inference failed for: r17v2 */
    /* JADX WARN: Type inference failed for: r17v3, types: [android.opengl.EGLDisplay] */
    /* JADX WARN: Type inference failed for: r17v4 */
    /* JADX WARN: Type inference failed for: r17v5 */
    /* JADX WARN: Type inference failed for: r17v6 */
    /* JADX WARN: Type inference failed for: r17v7 */
    /* JADX WARN: Type inference failed for: r17v8 */
    /* JADX WARN: Type inference failed for: r17v9 */
    /* JADX WARN: Type inference failed for: r1v0, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r1v10 */
    /* JADX WARN: Type inference failed for: r1v13 */
    /* JADX WARN: Type inference failed for: r1v14 */
    /* JADX WARN: Type inference failed for: r1v16 */
    /* JADX WARN: Type inference failed for: r1v17 */
    /* JADX WARN: Type inference failed for: r1v18 */
    /* JADX WARN: Type inference failed for: r1v19 */
    /* JADX WARN: Type inference failed for: r1v20 */
    /* JADX WARN: Type inference failed for: r1v21 */
    /* JADX WARN: Type inference failed for: r1v22 */
    /* JADX WARN: Type inference failed for: r1v23 */
    /* JADX WARN: Type inference failed for: r1v24 */
    /* JADX WARN: Type inference failed for: r1v3 */
    /* JADX WARN: Type inference failed for: r1v4, types: [android.opengl.EGLDisplay] */
    /* JADX WARN: Type inference failed for: r3v15 */
    /* JADX WARN: Type inference failed for: r3v4, types: [int[]] */
    /* JADX WARN: Type inference failed for: r3v6 */
    /* JADX WARN: Type inference failed for: r5v12 */
    /* JADX WARN: Type inference failed for: r5v4 */
    /* JADX WARN: Type inference failed for: r5v6 */
    private static Bitmap render(Bitmap bitmap, boolean z, float f, float f2, float f3, float[] fArr, float f4, boolean z2) throws Throwable {
        ?? r14;
        ?? r12;
        ?? r11;
        ?? r1;
        Bitmap bitmap2;
        ?? r2;
        EGLSurface eGLSurface;
        ?? r3;
        ?? EglGetDisplay = bitmap;
        int i = 0;
        EglGetDisplay = EGL14.eglGetDisplay(0);
        ?? r13 = EGL14.EGL_NO_CONTEXT;
        ?? r15 = EGL14.EGL_NO_SURFACE;
        ?? r16 = 2;
        r16 = 2;
        try {
            int[] iArr = new int[2];
            try {
                if (!EGL14.eglInitialize(EglGetDisplay, iArr, 0, iArr, 1)) {
                    throw new IllegalStateException("EGL init");
                }
                EGLConfig[] eGLConfigArr = new EGLConfig[1];
                int[] iArr2 = new int[1];
                try {
                    if (!EGL14.eglChooseConfig(EglGetDisplay, new int[]{12352, HALO, 12339, 1, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12344}, 0, eGLConfigArr, 0, 1, iArr2, 0) || iArr2[0] == 0) {
                        throw new IllegalStateException("EGL config");
                    }
                    EGLContext eGLContextEglCreateContext = EGL14.eglCreateContext(EglGetDisplay, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
                    try {
                        ?? r5 = 12375;
                        ?? r4 = {12375, 1032, 12374, 1032, 12344};
                        EGLSurface eGLSurfaceEglCreatePbufferSurface = EGL14.eglCreatePbufferSurface(EglGetDisplay, eGLConfigArr[0], r4, 0);
                        try {
                            try {
                                if (!EGL14.eglMakeCurrent(EglGetDisplay, eGLSurfaceEglCreatePbufferSurface, eGLSurfaceEglCreatePbufferSurface, eGLContextEglCreateContext)) {
                                    throw new IllegalStateException("EGL current");
                                }
                                int iShader = shader(35633, "attribute vec2 position; varying vec2 uv; void main(){uv=(position+1.)*.5;gl_Position=vec4(position,0.,1.);}");
                                int iShader2 = shader(35632, FRAGMENT);
                                int iGlCreateProgram = GLES20.glCreateProgram();
                                GLES20.glAttachShader(iGlCreateProgram, iShader);
                                GLES20.glAttachShader(iGlCreateProgram, iShader2);
                                GLES20.glLinkProgram(iGlCreateProgram);
                                int[] iArr3 = {0};
                                GLES20.glGetProgramiv(iGlCreateProgram, 35714, iArr3, 0);
                                if (iArr3[0] == 0) {
                                    throw new IllegalStateException(GLES20.glGetProgramInfoLog(iGlCreateProgram));
                                }
                                GLES20.glDeleteShader(iShader);
                                GLES20.glDeleteShader(iShader2);
                                GLES20.glUseProgram(iGlCreateProgram);
                                GLES20.glUniform3f(GLES20.glGetUniformLocation(iGlCreateProgram, "wb"), fArr[0], fArr[1], fArr[2]);
                                GLES20.glUniform1f(GLES20.glGetUniformLocation(iGlCreateProgram, "denoise"), f4);
                                GLES20.glUniform1f(GLES20.glGetUniformLocation(iGlCreateProgram, "warmInput"), z2 ? 1.0f : 0.0f);
                                FloatBuffer floatBufferAsFloatBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
                                try {
                                    floatBufferAsFloatBuffer.put(new float[]{-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f}).position(0);
                                    int iGlGetAttribLocation = GLES20.glGetAttribLocation(iGlCreateProgram, "position");
                                    GLES20.glEnableVertexAttribArray(iGlGetAttribLocation);
                                    ?? r17 = 2;
                                    GLES20.glVertexAttribPointer(iGlGetAttribLocation, 2, 5126, false, 0, (Buffer) floatBufferAsFloatBuffer);
                                    GLES20.glUniform1i(GLES20.glGetUniformLocation(iGlCreateProgram, "image"), 0);
                                    GLES20.glUniform1f(GLES20.glGetUniformLocation(iGlCreateProgram, "mono"), z ? 1.0f : 0.0f);
                                    boolean zEquals = ColorSpace.get(ColorSpace.Named.DISPLAY_P3).equals(bitmap.getColorSpace());
                                    GLES20.glUniform3f(GLES20.glGetUniformLocation(iGlCreateProgram, "weights"), zEquals ? 0.22897457f : 0.2126f, zEquals ? 0.69173855f : 0.7152f, zEquals ? 0.07928691f : 0.0722f);
                                    GLES20.glUniform1f(GLES20.glGetUniformLocation(iGlCreateProgram, "gamma"), f);
                                    GLES20.glUniform1f(GLES20.glGetUniformLocation(iGlCreateProgram, "whitePoint"), f2);
                                    GLES20.glUniform1f(GLES20.glGetUniformLocation(iGlCreateProgram, "detailStrength"), f3);
                                    GLES20.glUniform2f(GLES20.glGetUniformLocation(iGlCreateProgram, "fullSize"), bitmap.getWidth(), bitmap.getHeight());
                                    int[] iArr4 = {0};
                                    GLES20.glGenTextures(1, iArr4, 0);
                                    GLES20.glBindTexture(3553, iArr4[0]);
                                    GLES20.glTexParameteri(3553, 10241, 9728);
                                    GLES20.glTexParameteri(3553, 10240, 9728);
                                    GLES20.glTexParameteri(3553, 10242, 33071);
                                    GLES20.glTexParameteri(3553, 10243, 33071);
                                    GLES20.glDisable(3024);
                                    Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888, false, bitmap.getColorSpace());
                                    try {
                                        ByteBuffer byteBufferAllocateDirect = ByteBuffer.allocateDirect(4260096);
                                        int i2 = 0;
                                        EglGetDisplay = EglGetDisplay;
                                        eGLContextEglCreateContext = eGLContextEglCreateContext;
                                        eGLSurfaceEglCreatePbufferSurface = eGLSurfaceEglCreatePbufferSurface;
                                        while (i2 < bitmap.getHeight()) {
                                            int i3 = i;
                                            ?? r10 = EglGetDisplay;
                                            EGLContext eGLContext = eGLContextEglCreateContext;
                                            EGLSurface eGLSurface2 = eGLSurfaceEglCreatePbufferSurface;
                                            ?? r18 = r17;
                                            while (i3 < bitmap.getWidth()) {
                                                try {
                                                    int iMin = Math.min(TILE, bitmap.getWidth() - i3);
                                                    int iMin2 = Math.min(TILE, bitmap.getHeight() - i2);
                                                    int iMax = Math.max(i, i3 - 4);
                                                    int iMax2 = Math.max(i, i2 - 4);
                                                    int iMin3 = Math.min(bitmap.getWidth(), (i3 + iMin) + HALO) - iMax;
                                                    EGLContext eGLContext2 = eGLContext;
                                                    try {
                                                        int iMin4 = Math.min(bitmap.getHeight(), (i2 + iMin2) + HALO) - iMax2;
                                                        Bitmap bitmapCreateBitmap2 = Bitmap.createBitmap((Bitmap) EglGetDisplay, iMax, iMax2, iMin3, iMin4);
                                                        EGLSurface eGLSurface3 = eGLSurface2;
                                                        try {
                                                            GLUtils.texImage2D(3553, 0, bitmapCreateBitmap2, 0);
                                                            if (bitmapCreateBitmap2 != EglGetDisplay) {
                                                                try {
                                                                    bitmapCreateBitmap2.recycle();
                                                                } catch (Throwable th) {
                                                                    th = th;
                                                                    r3 = r10;
                                                                }
                                                            }
                                                            GLES20.glViewport(0, 0, iMin3, iMin4);
                                                            float f5 = iMin3;
                                                            float f6 = iMin4;
                                                            r18 = r10;
                                                            try {
                                                                GLES20.glUniform2f(GLES20.glGetUniformLocation(iGlCreateProgram, "texel"), 1.0f / f5, 1.0f / f6);
                                                                GLES20.glUniform2f(GLES20.glGetUniformLocation(iGlCreateProgram, "tileOrigin"), iMax, iMax2);
                                                                GLES20.glUniform2f(GLES20.glGetUniformLocation(iGlCreateProgram, "tileSize"), f5, f6);
                                                                GLES20.glDrawArrays(5, 0, HALO);
                                                                byteBufferAllocateDirect.clear();
                                                                GLES20.glReadPixels(i3 - iMax, i2 - iMax2, iMin, iMin2, 6408, 5121, byteBufferAllocateDirect);
                                                                if (GLES20.glGetError() != 0) {
                                                                    throw new IllegalStateException("Film input GPU pass");
                                                                }
                                                                Bitmap bitmapCreateBitmap3 = Bitmap.createBitmap(iMin, iMin2, Bitmap.Config.ARGB_8888, false, bitmap.getColorSpace());
                                                                try {
                                                                    byteBufferAllocateDirect.position(0);
                                                                    bitmapCreateBitmap3.copyPixelsFromBuffer(byteBufferAllocateDirect);
                                                                    new Canvas(bitmapCreateBitmap).drawBitmap(bitmapCreateBitmap3, i3, i2, (Paint) null);
                                                                    bitmapCreateBitmap3.recycle();
                                                                    i3 += TILE;
                                                                    i = 0;
                                                                    r10 = r18;
                                                                    eGLSurface2 = eGLSurface3;
                                                                    eGLContext = eGLContext2;
                                                                    r18 = r18;
                                                                } catch (Throwable th2) {
                                                                    bitmapCreateBitmap3.recycle();
                                                                    throw th2;
                                                                }
                                                            } catch (Throwable th3) {
                                                                th = th3;
                                                                r3 = r18;
                                                            }
                                                        } catch (Throwable th4) {
                                                            if (bitmapCreateBitmap2 == EglGetDisplay) {
                                                                throw th4;
                                                            }
                                                            bitmapCreateBitmap2.recycle();
                                                            throw th4;
                                                        }
                                                        eGLSurface = eGLSurface3;
                                                        r2 = r3;
                                                        bitmap2 = bitmapCreateBitmap;
                                                    } catch (Throwable th5) {
                                                        th = th5;
                                                        bitmap2 = bitmapCreateBitmap;
                                                        r2 = r10;
                                                        eGLSurface = eGLSurface2;
                                                    }
                                                    r11 = eGLContext2;
                                                    r1 = r2;
                                                    r12 = eGLSurface;
                                                    r14 = bitmap2;
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    r14 = bitmapCreateBitmap;
                                                    r1 = r10;
                                                    r11 = eGLContext;
                                                    r12 = eGLSurface2;
                                                }
                                                if (r14 != 0) {
                                                    r14.recycle();
                                                }
                                                if (r1 != EGL14.EGL_NO_DISPLAY) {
                                                    EGL14.eglMakeCurrent(r1, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                                                    if (r12 != EGL14.EGL_NO_SURFACE) {
                                                        EGL14.eglDestroySurface(r1, r12);
                                                    }
                                                    if (r11 != EGL14.EGL_NO_CONTEXT) {
                                                        EGL14.eglDestroyContext(r1, r11);
                                                    }
                                                    EGL14.eglReleaseThread();
                                                }
                                                throw th;
                                            }
                                            r17 = r10;
                                            i2 += TILE;
                                            eGLSurfaceEglCreatePbufferSurface = eGLSurface2;
                                            EglGetDisplay = r10;
                                            eGLContextEglCreateContext = eGLContext;
                                        }
                                        ?? r19 = EglGetDisplay;
                                        EGLContext eGLContext3 = eGLContextEglCreateContext;
                                        EGLSurface eGLSurface4 = eGLSurfaceEglCreatePbufferSurface;
                                        if (r19 != EGL14.EGL_NO_DISPLAY) {
                                            EGL14.eglMakeCurrent(r19, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                                            if (eGLSurface4 != EGL14.EGL_NO_SURFACE) {
                                                EGL14.eglDestroySurface(r19, eGLSurface4);
                                            }
                                            if (eGLContext3 != EGL14.EGL_NO_CONTEXT) {
                                                EGL14.eglDestroyContext(r19, eGLContext3);
                                            }
                                            EGL14.eglReleaseThread();
                                        }
                                        return bitmapCreateBitmap;
                                    } catch (Throwable th7) {
                                        th = th7;
                                        r1 = EglGetDisplay;
                                        r14 = bitmapCreateBitmap;
                                        r11 = eGLContextEglCreateContext;
                                        r12 = eGLSurfaceEglCreatePbufferSurface;
                                    }
                                } catch (Throwable th8) {
                                    th = th8;
                                    EglGetDisplay = EglGetDisplay;
                                    r5 = eGLContextEglCreateContext;
                                    r4 = eGLSurfaceEglCreatePbufferSurface;
                                    r16 = 0;
                                    r15 = r4;
                                    r13 = r5;
                                    r14 = r16;
                                    r1 = EglGetDisplay;
                                    r11 = r13;
                                    r12 = r15;
                                }
                            } catch (Throwable th9) {
                                th = th9;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            r13 = eGLContextEglCreateContext;
                            r15 = eGLSurfaceEglCreatePbufferSurface;
                            r16 = 0;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        r13 = eGLContextEglCreateContext;
                        r15 = r15;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    r13 = r13;
                    r15 = r15;
                }
            } catch (Throwable th13) {
                th = th13;
            }
        } catch (Throwable th14) {
            th = th14;
            r13 = r13;
            r15 = r15;
        }
        r16 = 0;
        r14 = r16;
        r1 = EglGetDisplay;
        r11 = r13;
        r12 = r15;
        if (r14 != 0) {
            r14.recycle();
        }
        if (r1 != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(r1, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            if (r12 != EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(r1, r12);
            }
            if (r11 != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(r1, r11);
            }
            EGL14.eglReleaseThread();
        }
        throw th;
    }
}
