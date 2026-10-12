package com.codex.leicaapp;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.SizeF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes14.dex */
public final class AdaptiveLens {
    private static final String TAG = "LeicaLogicalZoom";
    private static volatile String activeId;
    private static volatile boolean discovered;
    private static volatile String primary;
    private static volatile Map<String, Profile> profiles = Collections.emptyMap();
    private static final Map<CaptureRequest.Builder, Profile> builders = Collections.synchronizedMap(new WeakHashMap());
    private static final Map<String, Float> professionalZooms = new ConcurrentHashMap();

    private AdaptiveLens() {
    }

    private static final class Profile {
        final Rect active;
        final double area;
        final String id;
        final Set<String> keys;
        final boolean ratioControl;
        final boolean rear;
        final Float[] stops;
        final Range<Float> zoom;

        Profile(String str, boolean z, boolean z2, Range<Float> range, Rect rect, Set<String> set, Float[] fArr, double d) {
            this.id = str;
            this.rear = z;
            this.ratioControl = z2;
            this.zoom = range;
            this.active = rect;
            this.keys = set;
            this.stops = fArr;
            this.area = d;
        }
    }

    private static boolean finite(float f) {
        return (Float.isNaN(f) || Float.isInfinite(f)) ? false : true;
    }

    private static boolean positive(float f) {
        return finite(f) && f > 0.0f;
    }

    private static Object invoke(Object obj, String str, Object... objArr) throws Exception {
        for (Method method : (obj instanceof Class ? (Class) obj : obj.getClass()).getMethods()) {
            if (method.getName().equals(str) && method.getParameterTypes().length == objArr.length) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                boolean z = true;
                for (int i = 0; i < objArr.length; i++) {
                    Class<?> cls = parameterTypes[i];
                    if (cls == Integer.TYPE) {
                        cls = Integer.class;
                    }
                    if (objArr[i] != null && !cls.isInstance(objArr[i])) {
                        z = false;
                    }
                }
                if (z) {
                    method.setAccessible(true);
                    if (obj instanceof Class) {
                        obj = null;
                    }
                    return method.invoke(obj, objArr);
                }
            }
        }
        throw new NoSuchMethodException(str);
    }

    public static int currentMode() {
        try {
            return Class.forName("com.android.camera.module.Z").getField("a").getInt(null);
        } catch (Exception e) {
            return -1;
        } catch (LinkageError e2) {
            return -1;
        }
    }

    public static boolean mode(int i) {
        return i == 167 || i == 256;
    }

    private static boolean back() {
        try {
            return ((Number) invoke(invoke(Class.forName("g2.a"), "g", new Object[0]), "B", new Object[0])).intValue() == 0;
        } catch (Exception e) {
            return false;
        } catch (LinkageError e2) {
            return false;
        }
    }

    private static Context context() {
        try {
            return (Context) invoke(Class.forName("com.xiaomi.camera.basic.Global"), "getApplication", new Object[0]);
        } catch (Exception e) {
            return null;
        } catch (LinkageError e2) {
            return null;
        }
    }

    private static void ensure() {
        Context context;
        if (discovered || (context = context()) == null) {
            return;
        }
        Object systemService = context.getSystemService("camera");
        if (systemService instanceof CameraManager) {
            enumerate((CameraManager) systemService, null, 0);
        }
    }

    private static <T> T get(CameraCharacteristics cameraCharacteristics, CameraCharacteristics.Key<T> key) {
        try {
            return (T) cameraCharacteristics.get(key);
        } catch (LinkageError e) {
            return null;
        } catch (RuntimeException e2) {
            return null;
        }
    }

    private static CameraCharacteristics caps(CameraManager cameraManager, String str) {
        try {
            return cameraManager.getCameraCharacteristics(str);
        } catch (Exception e) {
            return null;
        } catch (LinkageError e2) {
            return null;
        }
    }

    private static double area(CameraCharacteristics cameraCharacteristics) {
        SizeF sizeF = (SizeF) get(cameraCharacteristics, CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        if (sizeF != null && positive(sizeF.getWidth()) && positive(sizeF.getHeight())) {
            return ((double) sizeF.getWidth()) * ((double) sizeF.getHeight());
        }
        return 0.0d;
    }

    private static double fov(CameraCharacteristics cameraCharacteristics) {
        float[] fArr = (float[]) get(cameraCharacteristics, CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        SizeF sizeF = (SizeF) get(cameraCharacteristics, CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        if (fArr == null || fArr.length == 0 || !positive(fArr[0]) || sizeF == null || !positive(sizeF.getWidth()) || !positive(sizeF.getHeight())) {
            return Double.NaN;
        }
        return ((double) fArr[0]) / Math.hypot(sizeF.getWidth(), sizeF.getHeight());
    }

    private static boolean numeric(String str) {
        int i;
        if (str == null) {
            return false;
        }
        try {
            return str.length() != 0 && (i = Integer.parseInt(str)) >= 0 && Integer.toString(i).equals(str);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static Profile build(CameraManager cameraManager, String str, CameraCharacteristics cameraCharacteristics) {
        Integer num = (Integer) get(cameraCharacteristics, CameraCharacteristics.LENS_FACING);
        if (num == null) {
            return null;
        }
        boolean z = num.intValue() == 1;
        HashSet hashSet = new HashSet();
        try {
            List<CaptureRequest.Key<?>> availableCaptureRequestKeys = cameraCharacteristics.getAvailableCaptureRequestKeys();
            if (availableCaptureRequestKeys != null) {
                for (CaptureRequest.Key<?> key : availableCaptureRequestKeys) {
                    if (key != null) {
                        hashSet.add(key.getName());
                    }
                }
            }
        } catch (RuntimeException e) {
        }
        Range range = Build.VERSION.SDK_INT >= 30 ? (Range) get(cameraCharacteristics, CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE) : null;
        boolean z2 = range != null && hashSet.contains(CaptureRequest.CONTROL_ZOOM_RATIO.getName());
        if (range != null && (!positive(((Float) range.getLower()).floatValue()) || !positive(((Float) range.getUpper()).floatValue()) || ((Float) range.getLower()).floatValue() > 1.0f || ((Float) range.getUpper()).floatValue() < 1.0f)) {
            range = null;
        }
        if (range == null || !z2) {
            Float f = (Float) get(cameraCharacteristics, CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            range = new Range(Float.valueOf(1.0f), Float.valueOf((f == null || !finite(f.floatValue()) || f.floatValue() < 1.0f) ? 1.0f : f.floatValue()));
            z2 = false;
        }
        TreeSet treeSet = new TreeSet();
        treeSet.add(Float.valueOf(1.0f));
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        if (z && z2 && Build.VERSION.SDK_INT >= 29) {
            try {
                Set<String> physicalCameraIds = cameraCharacteristics.getPhysicalCameraIds();
                if (physicalCameraIds != null) {
                    for (String str2 : physicalCameraIds) {
                        CameraCharacteristics cameraCharacteristicsCaps = caps(cameraManager, str2);
                        Integer num2 = cameraCharacteristicsCaps == null ? null : (Integer) get(cameraCharacteristicsCaps, CameraCharacteristics.LENS_FACING);
                        if (num2 != null && num2.intValue() == 1) {
                            linkedHashMap.put(str2, cameraCharacteristicsCaps);
                        }
                    }
                }
            } catch (LinkageError e2) {
            } catch (RuntimeException e3) {
            }
        }
        double dFov = fov(cameraCharacteristics);
        if (!Double.isFinite(dFov) || dFov <= 0.0d) {
            CameraCharacteristics cameraCharacteristics2 = null;
            for (CameraCharacteristics cameraCharacteristics3 : linkedHashMap.values()) {
                if (cameraCharacteristics2 != null && area(cameraCharacteristics3) <= area(cameraCharacteristics2)) {
                    cameraCharacteristics3 = cameraCharacteristics2;
                }
                cameraCharacteristics2 = cameraCharacteristics3;
            }
            if (cameraCharacteristics2 != null) {
                dFov = fov(cameraCharacteristics2);
            }
        }
        if (Double.isFinite(dFov) && dFov > 0.0d) {
            Iterator it = linkedHashMap.values().iterator();
            while (it.hasNext()) {
                double dFov2 = fov((CameraCharacteristics) it.next()) / dFov;
                if (Double.isFinite(dFov2) && dFov2 > 0.0d) {
                    float fRound = Math.round(dFov2 * 10.0d) / 10.0f;
                    if (positive(fRound) && range.contains(Float.valueOf(fRound))) {
                        treeSet.add(Float.valueOf(fRound));
                    }
                }
            }
        }
        if (z && z2 && ((Float) range.getLower()).floatValue() < 1.0f) {
            treeSet.add(range.getLower());
        }
        Rect rect = (Rect) get(cameraCharacteristics, CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        return new Profile(str, z, z2, range, rect != null ? new Rect(rect) : rect, Collections.unmodifiableSet(hashSet), (Float[]) treeSet.toArray(new Float[0]), area(cameraCharacteristics));
    }

    public static synchronized String[] enumerate(CameraManager cameraManager, String[] strArr, int i) {
        if (cameraManager != null) {
            try {
                String[] cameraIdList = cameraManager.getCameraIdList();
                if (cameraIdList != null) {
                    LinkedHashSet linkedHashSet = new LinkedHashSet();
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    for (String str : cameraIdList) {
                        if (numeric(str)) {
                            linkedHashSet.add(str);
                            CameraCharacteristics cameraCharacteristicsCaps = caps(cameraManager, str);
                            if (cameraCharacteristicsCaps != null) {
                                try {
                                    Profile profileBuild = build(cameraManager, str, cameraCharacteristicsCaps);
                                    if (profileBuild != null) {
                                        linkedHashMap.put(str, profileBuild);
                                    }
                                } catch (LinkageError e) {
                                } catch (RuntimeException e2) {
                                    Log.w(TAG, "Camera zoom capabilities unavailable: " + str, e2);
                                }
                            }
                        }
                    }
                    Profile profile = (Profile) linkedHashMap.get("0");
                    Profile profile2 = (profile == null || profile.rear) ? profile : null;
                    if (profile2 == null) {
                        for (Profile profile3 : linkedHashMap.values()) {
                            if (!profile3.rear || (profile2 != null && profile3.area <= profile2.area)) {
                                profile3 = profile2;
                            }
                            profile2 = profile3;
                        }
                    }
                    profiles = Collections.unmodifiableMap(linkedHashMap);
                    primary = profile2 == null ? null : profile2.id;
                    if (!linkedHashMap.containsKey(activeId)) {
                        activeId = null;
                    }
                    discovered = !linkedHashMap.isEmpty();
                    if (profile2 != null) {
                        Log.i(TAG, "public logical camera=" + profile2.id + " range=" + profile2.zoom + " focal stops=" + Arrays.toString(profile2.stops) + "; optical selection is controlled by the camera driver");
                    }
                    strArr = (String[]) linkedHashSet.toArray(new String[0]);
                } else if (strArr == null) {
                    strArr = new String[0];
                }
            } catch (Exception e3) {
                if (strArr == null) {
                    strArr = new String[0];
                }
            } catch (LinkageError e4) {
                if (strArr == null) {
                    strArr = new String[0];
                }
            }
        } else if (strArr == null) {
            strArr = new String[0];
        }
        return strArr;
    }

    private static Profile profile() {
        ensure();
        Profile profile = profiles.get(activeId);
        return (profile == null || !profile.rear) ? profiles.get(primary) : profile;
    }

    public static boolean enabled(int i) {
        return mode(i) && back() && profile() != null;
    }

    public static ArrayList<String> cameraIds() {
        ensure();
        return new ArrayList<>(profiles.keySet());
    }

    public static Float[] markers() {
        Profile profile = profile();
        return profile == null ? new Float[0] : (Float[]) profile.stops.clone();
    }

    public static float[] markerFloats() {
        Float[] fArrMarkers = markers();
        float[] fArr = new float[fArrMarkers.length];
        for (int i = 0; i < fArr.length; i++) {
            fArr[i] = fArrMarkers[i].floatValue();
        }
        return fArr;
    }

    public static Float[] modeMarkers(int i, Float[] fArr) {
        Float[] fArrMarkers = enabled(i) ? markers() : new Float[0];
        return fArrMarkers.length == 0 ? fArr : fArrMarkers;
    }

    public static Range<Float> range() {
        Profile profile = profile();
        return profile == null ? new Range<>(Float.valueOf(1.0f), Float.valueOf(1.0f)) : profile.zoom;
    }

    private static Object module(Object obj) throws Exception {
        Object obj2 = obj.getClass().getField("b").get(obj);
        if (obj2 instanceof WeakReference) {
            return ((WeakReference) obj2).get();
        }
        return null;
    }

    public static Range<Float> managerRange(Object obj) {
        Object objModule;
        try {
            if (!mode(obj.getClass().getField("c").getInt(obj)) || (objModule = module(obj)) == null) {
                return null;
            }
            Object objInvoke = invoke(objModule, "getCameraManager", new Object[0]);
            if (Boolean.TRUE.equals(invoke(objInvoke, "b0", new Object[0]))) {
                return null;
            }
            ensure();
            Profile profile = profiles.get(String.valueOf(((Number) invoke(objInvoke, "getActualCameraId", new Object[0])).intValue()));
            return (profile == null || !profile.rear) ? null : profile.zoom;
        } catch (Exception e) {
            return null;
        } catch (LinkageError e2) {
            return null;
        }
    }

    private static float storedZoom(int i) {
        try {
            float fFloatValue = ((Number) invoke(Class.forName("com.android.camera.data.data.k"), "O", Integer.valueOf(i))).floatValue();
            if (!positive(fFloatValue)) {
                fFloatValue = 1.0f;
            }
            return fFloatValue;
        } catch (Exception e) {
            return 1.0f;
        } catch (LinkageError e2) {
            return 1.0f;
        }
    }

    private static float clamp(Range<Float> range, float f) {
        if (!positive(f)) {
            f = 1.0f;
        }
        return ((Float) range.clamp(Float.valueOf(f))).floatValue();
    }

    public static float initialZoom(Object obj) {
        Range<Float> rangeManagerRange = managerRange(obj);
        if (rangeManagerRange == null) {
            return Float.NaN;
        }
        try {
            int i = obj.getClass().getField("c").getInt(obj);
            float fClamp = clamp(rangeManagerRange, storedZoom(i));
            if (i == 167) {
                professionalZoom(obj, fClamp);
            }
            return fClamp;
        } catch (Exception e) {
            return Float.NaN;
        }
    }

    public static int selectForMode(int i, int i2) {
        Profile profile;
        if (i != 0 || !mode(i2) || (profile = profile()) == null) {
            return -1;
        }
        activeId = profile.id;
        if (i2 == 167) {
            professionalZooms.remove(profile.id);
        }
        return Integer.parseInt(profile.id);
    }

    public static void professionalZoom(Object obj, float f) {
        Object objModule;
        if (obj != null && positive(f)) {
            try {
                if (obj.getClass().getField("c").getInt(obj) == 167 && (objModule = module(obj)) != null && ((Number) invoke(objModule, "getModuleIndex", new Object[0])).intValue() == 167) {
                    Object objInvoke = invoke(objModule, "getCameraManager", new Object[0]);
                    if (!Boolean.TRUE.equals(invoke(objInvoke, "b0", new Object[0]))) {
                        ensure();
                        String strValueOf = String.valueOf(((Number) invoke(objInvoke, "getActualCameraId", new Object[0])).intValue());
                        Profile profile = profiles.get(strValueOf);
                        if (profile == null || !profile.rear) {
                            return;
                        }
                        professionalZooms.put(strValueOf, Float.valueOf(clamp(profile.zoom, f)));
                    }
                }
            } catch (Exception e) {
            } catch (LinkageError e2) {
            }
        }
    }

    public static int switchLens(Object obj, float f) {
        if (obj == null) {
            return -1;
        }
        try {
            if (!mode(((Number) invoke(obj, "getModuleIndex", new Object[0])).intValue())) {
                return -1;
            }
            Object objInvoke = invoke(obj, "getCameraManager", new Object[0]);
            if (Boolean.TRUE.equals(invoke(objInvoke, "b0", new Object[0]))) {
                return -1;
            }
            ensure();
            String strValueOf = String.valueOf(((Number) invoke(objInvoke, "getActualCameraId", new Object[0])).intValue());
            Profile profile = profiles.get(strValueOf);
            if (profile == null || !profile.rear) {
                return -1;
            }
            activeId = strValueOf;
            return 0;
        } catch (Exception e) {
            return -1;
        } catch (LinkageError e2) {
            return -1;
        }
    }

    public static float managerZoom(Object obj, float f) {
        Object objModule;
        if (obj == null || !positive(f)) {
            return Float.NaN;
        }
        try {
            if (mode(obj.getClass().getField("c").getInt(obj)) && (objModule = module(obj)) != null) {
                return deviceZoom(f, ((Number) invoke(invoke(objModule, "getCameraManager", new Object[0]), "getActualCameraId", new Object[0])).intValue());
            }
            return Float.NaN;
        } catch (Exception e) {
            return Float.NaN;
        } catch (LinkageError e2) {
            return Float.NaN;
        }
    }

    public static float deviceZoom(float f, int i) {
        if (!mode(currentMode())) {
            return Float.NaN;
        }
        ensure();
        Profile profile = profiles.get(Integer.toString(i));
        return (profile == null || !profile.rear) ? Float.NaN : clamp(profile.zoom, f);
    }

    public static SessionConfiguration session(CameraDevice cameraDevice, SessionConfiguration sessionConfiguration) {
        if (cameraDevice != null && mode(currentMode())) {
            ensure();
            Profile profile = profiles.get(cameraDevice.getId());
            if (profile != null && profile.rear) {
                activeId = profile.id;
            }
        }
        return sessionConfiguration;
    }

    public static void attach(CaptureRequest.Builder builder, String str) {
        if (builder == null || !mode(currentMode())) {
            return;
        }
        ensure();
        Profile profile = profiles.get(str);
        if (profile == null || !profile.rear) {
            return;
        }
        builders.put(builder, profile);
    }

    public static boolean hasCamera(int i) {
        ensure();
        Profile profile = profiles.get(Integer.toString(i));
        return profile != null && profile.rear;
    }

    public static void apply(CaptureRequest.Builder builder) {
        Profile profile;
        Rect rect;
        Float f;
        if (builder != null && mode(currentMode()) && (profile = builders.get(builder)) != null) {
            try {
                CaptureRequest captureRequestBuild = builder.build();
                if (!captureRequestBuild.isReprocess()) {
                    if (!profile.keys.isEmpty()) {
                        for (CaptureRequest.Key<?> key : captureRequestBuild.getKeys()) {
                            if (key != null && !key.getName().startsWith("android.") && !profile.keys.contains(key.getName())) {
                                remove(builder, key);
                            }
                        }
                    }
                    if (currentMode() != 167 || (f = professionalZooms.get(profile.id)) == null || !applyProfessionalZoom(builder, profile, f.floatValue())) {
                        if (profile.ratioControl) {
                            Float f2 = (Float) captureRequestBuild.get(CaptureRequest.CONTROL_ZOOM_RATIO);
                            if (f2 != null) {
                                builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, Float.valueOf(clamp(profile.zoom, f2.floatValue())));
                                return;
                            }
                            return;
                        }
                        if (profile.active != null && profile.active.width() > 0 && profile.active.height() > 0) {
                            if (Build.VERSION.SDK_INT >= 30 && profile.keys.contains(CaptureRequest.CONTROL_ZOOM_RATIO.getName())) {
                                builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, Float.valueOf(1.0f));
                            }
                            Rect rect2 = (Rect) captureRequestBuild.get(CaptureRequest.SCALER_CROP_REGION);
                            if (rect2 != null) {
                                Rect rect3 = new Rect(rect2);
                                if (!rect3.intersect(profile.active) || rect3.width() < 1 || rect3.height() < 1) {
                                    rect3 = new Rect(profile.active);
                                }
                                int iMax = Math.max(1, Math.round(profile.active.width() / ((Float) profile.zoom.getUpper()).floatValue()));
                                int iMax2 = Math.max(1, Math.round(profile.active.height() / ((Float) profile.zoom.getUpper()).floatValue()));
                                if (rect3.width() < iMax || rect3.height() < iMax2) {
                                    int iMax3 = Math.max(rect3.width(), iMax);
                                    int iMax4 = Math.max(rect3.height(), iMax2);
                                    int iMax5 = Math.max(profile.active.left, Math.min(rect3.centerX() - (iMax3 / 2), profile.active.right - iMax3));
                                    int iMax6 = Math.max(profile.active.top, Math.min(rect3.centerY() - (iMax4 / 2), profile.active.bottom - iMax4));
                                    rect = new Rect(iMax5, iMax6, iMax3 + iMax5, iMax4 + iMax6);
                                } else {
                                    rect = rect3;
                                }
                                builder.set(CaptureRequest.SCALER_CROP_REGION, rect);
                            }
                        }
                    }
                }
            } catch (LinkageError e) {
                Log.w(TAG, "Public zoom control unavailable", e);
            } catch (RuntimeException e2) {
                Log.w(TAG, "Public zoom request left to the camera", e2);
            }
        }
    }

    private static boolean applyProfessionalZoom(CaptureRequest.Builder builder, Profile profile, float f) {
        float fClamp = clamp(profile.zoom, f);
        if (profile.ratioControl) {
            builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, Float.valueOf(fClamp));
            builder.set(CaptureRequest.SCALER_CROP_REGION, null);
            syncProfessionalVendorZoom(builder, profile, fClamp);
            return true;
        }
        if (profile.active == null || profile.active.width() < 1 || profile.active.height() < 1) {
            return false;
        }
        int iMax = Math.max(1, Math.round(profile.active.width() / fClamp));
        int iMax2 = Math.max(1, Math.round(profile.active.height() / fClamp));
        int iWidth = profile.active.left + ((profile.active.width() - iMax) / 2);
        int iHeight = profile.active.top + ((profile.active.height() - iMax2) / 2);
        if (Build.VERSION.SDK_INT >= 30 && profile.keys.contains(CaptureRequest.CONTROL_ZOOM_RATIO.getName())) {
            builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, Float.valueOf(1.0f));
        }
        builder.set(CaptureRequest.SCALER_CROP_REGION, new Rect(iWidth, iHeight, iMax + iWidth, iMax2 + iHeight));
        return true;
    }

    private static void syncProfessionalVendorZoom(CaptureRequest.Builder builder, Profile profile, float f) {
        for (CaptureRequest.Key<?> key : builder.build().getKeys()) {
            String name = key.getName();
            if (profile.keys.contains(name) && ("xiaomi.sat.targetzoom".equals(name) || "com.xiaomi.camera.userZoomRatio.userZoomRatio".equals(name))) {
                if (builder.get(key) instanceof Float) {
                    try {
                        builder.set(key, Float.valueOf(f));
                    } catch (RuntimeException e) {
                        Log.w(TAG, "Professional zoom hint retained", e);
                    }
                }
            }
        }
    }

    private static void remove(CaptureRequest.Builder builder, CaptureRequest.Key key) {
        try {
            builder.set(key, null);
        } catch (RuntimeException e) {
        }
    }

    private static String tag(int i, Float[] fArr) {
        int i2 = 0;
        float fFloatValue = fArr[i].floatValue();
        if (Math.abs(fFloatValue - 1.0f) < 0.001f) {
            return "wide";
        }
        if (fFloatValue < 1.0f) {
            for (int i3 = 0; i3 <= i; i3++) {
                if (fArr[i3].floatValue() < 1.0f) {
                    i2++;
                }
            }
            return i2 == 1 ? "ultra" : "ultra" + i2;
        }
        for (int i4 = 0; i4 <= i; i4++) {
            if (fArr[i4].floatValue() > 1.0f) {
                i2++;
            }
        }
        return i2 == 1 ? "tele" : "tele" + i2;
    }

    public static String[][] specs() {
        Profile profile = profile();
        if (profile == null) {
            return (String[][]) Array.newInstance((Class<?>) String.class, 0, 0);
        }
        String[][] strArr = (String[][]) Array.newInstance((Class<?>) String.class, profile.stops.length, 3);
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = new String[]{tag(i, profile.stops), profile.id, Float.toString(profile.stops[i].floatValue())};
        }
        return strArr;
    }

    public static float tagRatio(String str) {
        Float[] fArrMarkers = markers();
        for (int i = 0; i < fArrMarkers.length; i++) {
            if (tag(i, fArrMarkers).equals(str)) {
                return fArrMarkers[i].floatValue();
            }
        }
        return Float.NaN;
    }

    public static String tagForZoom(float f) {
        Float[] fArrMarkers = markers();
        if (fArrMarkers.length == 0) {
            return null;
        }
        int i = 0;
        for (int i2 = 0; i2 < fArrMarkers.length; i2++) {
            if (positive(f) && 1.0E-4f + f >= fArrMarkers[i2].floatValue()) {
                i = i2;
            }
        }
        return tag(i, fArrMarkers);
    }

    public static String label(String str) {
        if (!enabled(currentMode())) {
            return null;
        }
        float fTagRatio = tagRatio(str);
        if (positive(fTagRatio)) {
            return String.format(Locale.US, "%.1f×", Float.valueOf(fTagRatio));
        }
        return null;
    }

    public static ArrayList<Float> scale(int i) {
        TreeSet treeSet = new TreeSet();
        if (!enabled(i)) {
            return new ArrayList<>();
        }
        Range<Float> range = range();
        Collections.addAll(treeSet, markers());
        for (float f : new float[]{1.0f, 1.2f, 1.5f, 2.0f, 5.0f, 10.0f, 20.0f, 30.0f, 60.0f, 120.0f}) {
            if (range.contains(Float.valueOf(f))) {
                treeSet.add(Float.valueOf(f));
            }
        }
        treeSet.add(range.getLower());
        treeSet.add(range.getUpper());
        return new ArrayList<>(treeSet);
    }

    public static float wheelZoom(float[] fArr, String str, float f) {
        int i = 0;
        Range<Float> range = range();
        float fTagRatio = tagRatio(str);
        if (positive(fTagRatio)) {
            return clamp(range, fTagRatio);
        }
        if ("MIN".equals(str)) {
            return ((Float) range.getLower()).floatValue();
        }
        if ("MAX".equals(str)) {
            return ((Float) range.getUpper()).floatValue();
        }
        if ("MAIN".equals(str)) {
            return clamp(range, 1.0f);
        }
        if ("TELE".equals(str)) {
            Float[] fArrMarkers = markers();
            int length = fArrMarkers.length;
            while (i < length) {
                Float f2 = fArrMarkers[i];
                if (f2.floatValue() > 1.0f) {
                    return f2.floatValue();
                }
                i++;
            }
            return clamp(range, f);
        }
        if (!"UP".equals(str) && !"DOWN".equals(str)) {
            try {
                return clamp(range, Float.parseFloat(str));
            } catch (RuntimeException e) {
                return clamp(range, f);
            }
        }
        TreeSet treeSet = new TreeSet();
        if (fArr != null) {
            int length2 = fArr.length;
            while (i < length2) {
                float f3 = fArr[i];
                if (positive(f3) && range.contains(Float.valueOf(f3))) {
                    treeSet.add(Float.valueOf(f3));
                }
                i++;
            }
        }
        Collections.addAll(treeSet, markers());
        treeSet.add(range.getLower());
        treeSet.add(range.getUpper());
        Float f4 = "UP".equals(str) ? (Float) treeSet.higher(Float.valueOf(clamp(range, f) + 1.0E-4f)) : (Float) treeSet.lower(Float.valueOf(clamp(range, f) - 1.0E-4f));
        return f4 == null ? clamp(range, f) : f4.floatValue();
    }

    public static boolean allowWheel() {
        return enabled(currentMode()) && ((Float) range().getUpper()).floatValue() > ((Float) range().getLower()).floatValue();
    }

    public static void touch(View view, MotionEvent motionEvent) {
        ViewParent parent;
        if (!mode(currentMode()) || view == null || motionEvent == null || (parent = view.getParent()) == null) {
            return;
        }
        int actionMasked = motionEvent.getActionMasked();
        parent.requestDisallowInterceptTouchEvent((actionMasked == 1 || actionMasked == 3) ? false : true);
    }

    public static SparseArray<Float[]> overlayStops(SparseArray<Float[]> sparseArray) {
        if (!back() || profile() == null) {
            return sparseArray;
        }
        SparseArray<Float[]> sparseArray2 = sparseArray == null ? new SparseArray<>() : sparseArray.clone();
        sparseArray2.put(167, markers());
        sparseArray2.put(256, markers());
        return sparseArray2;
    }

    public static SparseArray<SparseArray<Float[]>> overlayRanges(SparseArray<SparseArray<Float[]>> sparseArray) {
        if (!back() || profile() == null) {
            return sparseArray;
        }
        SparseArray<SparseArray<Float[]>> sparseArray2 = sparseArray == null ? new SparseArray<>() : sparseArray.clone();
        for (int i : new int[]{167, 256}) {
            SparseArray<Float[]> sparseArray3 = sparseArray2.get(i);
            SparseArray<Float[]> sparseArray4 = sparseArray3 == null ? new SparseArray<>() : sparseArray3.clone();
            sparseArray4.put(0, markers());
            sparseArray2.put(i, sparseArray4);
        }
        return sparseArray2;
    }

    public static Map<Integer, SparseArray<String[]>> overlayNames(Map<Integer, SparseArray<String[]>> map) {
        if (!back() || profile() == null) {
            return map;
        }
        HashMap map2 = map == null ? new HashMap() : new HashMap(map);
        for (int i : new int[]{167, 256}) {
            SparseArray sparseArray = new SparseArray();
            Float[] fArrMarkers = markers();
            for (int i2 = 0; i2 < fArrMarkers.length; i2++) {
                sparseArray.put(i2, new String[]{Float.toString(fArrMarkers[i2].floatValue())});
            }
            map2.put(Integer.valueOf(i), sparseArray);
        }
        return map2;
    }
}
