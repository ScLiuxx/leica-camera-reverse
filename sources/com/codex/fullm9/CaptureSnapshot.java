package com.codex.fullm9;

import android.hardware.camera2.CaptureResult;
import android.location.Location;
import android.util.Size;
import java.io.IOException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class CaptureSnapshot {
    private float aperture;
    private int cct;
    private long exposure;
    private short focal;
    private int iso;
    private int lux;
    private boolean metered;
    private String provenance;
    private float zoom;
    public final Geometry a = new Geometry();
    public final Parameters b = new Parameters();
    public final Processing d = new Processing();
    public final Metadata e = new Metadata();
    public final Watermark l = new Watermark();

    public static final class Action {
        public String d;
    }

    public static final class Geometry {
        public int a;
        public int b;
        public int c;
        public int d;
        public int e;
        public long g;
        public int j;
    }

    public static final class Parameters {
        public Size b;
        public long e;
    }

    public static final class Processing {
        public final Action k = new Action();
    }

    public static final class Watermark {
        public boolean e;
        public boolean m;
        public String w;
    }

    public static final class Metadata {
        private String address;
        private String latlng;
        private Location location;

        public Location getLocation() {
            if (this.location == null) {
                return null;
            }
            return new Location(this.location);
        }

        public String getLocationAddress() {
            return this.address;
        }

        public String getLatlngStringCache() {
            return this.latlng;
        }
    }

    static int integer(Object obj, String str) {
        Object objField = Reflect.field(obj, str);
        if (objField instanceof Number) {
            return ((Number) objField).intValue();
        }
        return 0;
    }

    static long number(Object obj, String str) {
        Object objField = Reflect.field(obj, str);
        if (objField instanceof Number) {
            return ((Number) objField).longValue();
        }
        return 0L;
    }

    static String string(Object obj, String str) {
        Object objField = Reflect.field(obj, str);
        if (objField instanceof String) {
            return (String) objField;
        }
        return null;
    }

    static boolean bool(Object obj, String str) {
        return Boolean.TRUE.equals(Reflect.field(obj, str));
    }

    static Object optional(Object obj, String str) throws Exception {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getMethod(str, new Class[0]).invoke(obj, new Object[0]);
    }

    static CaptureSnapshot freeze(Object obj, byte[] bArr) throws Exception {
        CaptureSnapshot captureSnapshot = new CaptureSnapshot();
        Object objField = Reflect.field(obj, "a");
        Object objField2 = Reflect.field(obj, "b");
        Object objField3 = Reflect.field(obj, "l");
        Object objField4 = Reflect.field(obj, "e");
        captureSnapshot.a.a = integer(objField, "a");
        captureSnapshot.a.b = integer(objField, "b");
        captureSnapshot.a.c = integer(objField, "c");
        captureSnapshot.a.d = integer(objField, "d");
        captureSnapshot.a.e = integer(objField, "e");
        captureSnapshot.a.j = integer(objField, "j");
        captureSnapshot.a.g = number(objField, "g");
        Object objField5 = Reflect.field(objField2, "b");
        captureSnapshot.b.b = objField5 instanceof Size ? (Size) objField5 : new Size(Math.max(1, captureSnapshot.a.a), Math.max(1, captureSnapshot.a.b));
        captureSnapshot.b.e = number(objField2, "e");
        captureSnapshot.d.k.d = string(Reflect.field(Reflect.field(obj, "d"), "k"), "d");
        captureSnapshot.l.e = bool(objField3, "e");
        captureSnapshot.l.m = bool(objField3, "m");
        captureSnapshot.l.w = string(objField3, "w");
        if (captureSnapshot.l.m) {
            Object objOptional = optional(objField4, "getLocation");
            if (objOptional instanceof Location) {
                captureSnapshot.e.location = new Location((Location) objOptional);
            }
            captureSnapshot.e.address = (String) optional(objField4, "getLocationAddress");
            captureSnapshot.e.latlng = (String) optional(objField4, "getLatlngStringCache");
        }
        FullM9Engine.Settings settings = FullM9Engine.settings(bArr, obj);
        captureSnapshot.lux = settings.lux;
        captureSnapshot.cct = settings.cct;
        captureSnapshot.zoom = settings.zoom;
        captureSnapshot.provenance = settings.provenance;
        Object objCaptureResult = FullM9Engine.captureResult(obj);
        if (objCaptureResult instanceof CaptureResult) {
            Object objCall = Reflect.call(Class.forName("U8.e"), "c", new Class[]{CaptureResult.class}, objCaptureResult);
            captureSnapshot.focal = (short) integer(objCall, "c");
            Object objField6 = Reflect.field(objCall, "d");
            captureSnapshot.aperture = objField6 instanceof Number ? ((Number) objField6).floatValue() : 0.0f;
            captureSnapshot.exposure = captureSnapshot.b.e != 0 ? captureSnapshot.b.e : number(objCall, "a");
            captureSnapshot.iso = integer(objCall, "b");
            captureSnapshot.metered = true;
        }
        return captureSnapshot;
    }

    FullM9Engine.Settings settings() {
        FullM9Engine.Settings settings = new FullM9Engine.Settings();
        settings.lux = this.lux;
        settings.cct = this.cct;
        settings.zoom = this.zoom;
        settings.provenance = this.provenance;
        return settings;
    }

    public static void watermarkInputs(Object obj, Object obj2) {
        if (obj2 instanceof CaptureSnapshot) {
            CaptureSnapshot captureSnapshot = (CaptureSnapshot) obj2;
            if (captureSnapshot.metered) {
                if (captureSnapshot.focal > 0) {
                    Reflect.put(obj, "f", Short.valueOf(captureSnapshot.focal));
                }
                if (captureSnapshot.aperture > 0.0f) {
                    Reflect.put(obj, "g", Float.valueOf(captureSnapshot.aperture));
                }
                if (captureSnapshot.exposure > 0) {
                    Reflect.put(obj, "h", Long.valueOf(captureSnapshot.exposure));
                }
                if (captureSnapshot.iso > 0) {
                    Reflect.put(obj, "i", Integer.valueOf(captureSnapshot.iso));
                }
            }
        }
    }

    String encode() throws Exception {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("schema", 1);
        jSONObject.put("pipeline", "p7-cpu");
        jSONObject.put("lux", this.lux);
        jSONObject.put("cct", this.cct);
        jSONObject.put("zoom", this.zoom);
        jSONObject.put("provenance", this.provenance);
        jSONObject.put("width", this.a.a);
        jSONObject.put("height", this.a.b);
        jSONObject.put("orientation", this.a.c);
        jSONObject.put("gd", this.a.d);
        jSONObject.put("ge", this.a.e);
        jSONObject.put("format", this.a.j);
        jSONObject.put("taken", this.a.g);
        jSONObject.put("sw", this.b.b.getWidth());
        jSONObject.put("sh", this.b.b.getHeight());
        jSONObject.put("shotExposure", this.b.e);
        jSONObject.put("action", this.d.k.d);
        jSONObject.put("wm", this.l.e);
        jSONObject.put("wmLocation", this.l.m);
        jSONObject.put("wmId", this.l.w);
        jSONObject.put("address", this.e.address);
        jSONObject.put("latlng", this.e.latlng);
        jSONObject.put("metered", this.metered);
        jSONObject.put("focal", (int) this.focal);
        jSONObject.put("aperture", this.aperture);
        jSONObject.put("exposure", this.exposure);
        jSONObject.put("iso", this.iso);
        if (this.e.location != null) {
            JSONObject jSONObject2 = new JSONObject();
            Location location = this.e.location;
            jSONObject2.put("provider", location.getProvider());
            jSONObject2.put("lat", location.getLatitude());
            jSONObject2.put("lon", location.getLongitude());
            jSONObject2.put("time", location.getTime());
            if (location.hasAltitude()) {
                jSONObject2.put("alt", location.getAltitude());
            }
            if (location.hasAccuracy()) {
                jSONObject2.put("accuracy", location.getAccuracy());
            }
            jSONObject.put("location", jSONObject2);
        }
        return jSONObject.toString();
    }

    static String nullable(JSONObject jSONObject, String str) throws Exception {
        if (!jSONObject.has(str) || jSONObject.isNull(str)) {
            return null;
        }
        return jSONObject.getString(str);
    }

    static CaptureSnapshot decode(String str) throws Exception {
        JSONObject jSONObject = new JSONObject(str);
        if (jSONObject.getInt("schema") == 1 && "p7-cpu".equals(jSONObject.getString("pipeline"))) {
            CaptureSnapshot captureSnapshot = new CaptureSnapshot();
            captureSnapshot.lux = jSONObject.getInt("lux");
            captureSnapshot.cct = jSONObject.getInt("cct");
            captureSnapshot.zoom = (float) jSONObject.getDouble("zoom");
            captureSnapshot.provenance = jSONObject.getString("provenance");
            captureSnapshot.a.a = jSONObject.getInt("width");
            captureSnapshot.a.b = jSONObject.getInt("height");
            captureSnapshot.a.c = jSONObject.getInt("orientation");
            captureSnapshot.a.d = jSONObject.getInt("gd");
            captureSnapshot.a.e = jSONObject.getInt("ge");
            captureSnapshot.a.j = jSONObject.getInt("format");
            captureSnapshot.a.g = jSONObject.getLong("taken");
            captureSnapshot.b.b = new Size(jSONObject.getInt("sw"), jSONObject.getInt("sh"));
            captureSnapshot.b.e = jSONObject.getLong("shotExposure");
            captureSnapshot.d.k.d = nullable(jSONObject, "action");
            captureSnapshot.l.e = jSONObject.getBoolean("wm");
            captureSnapshot.l.m = jSONObject.getBoolean("wmLocation");
            captureSnapshot.l.w = nullable(jSONObject, "wmId");
            captureSnapshot.e.address = nullable(jSONObject, "address");
            captureSnapshot.e.latlng = nullable(jSONObject, "latlng");
            captureSnapshot.metered = jSONObject.getBoolean("metered");
            captureSnapshot.focal = (short) jSONObject.getInt("focal");
            captureSnapshot.aperture = (float) jSONObject.getDouble("aperture");
            captureSnapshot.exposure = jSONObject.getLong("exposure");
            captureSnapshot.iso = jSONObject.getInt("iso");
            if (jSONObject.has("location")) {
                JSONObject jSONObject2 = jSONObject.getJSONObject("location");
                Location location = new Location(jSONObject2.getString("provider"));
                location.setLatitude(jSONObject2.getDouble("lat"));
                location.setLongitude(jSONObject2.getDouble("lon"));
                location.setTime(jSONObject2.getLong("time"));
                if (jSONObject2.has("alt")) {
                    location.setAltitude(jSONObject2.getDouble("alt"));
                }
                if (jSONObject2.has("accuracy")) {
                    location.setAccuracy((float) jSONObject2.getDouble("accuracy"));
                }
                captureSnapshot.e.location = location;
            }
            return captureSnapshot;
        }
        throw new IOException("Unsupported capture snapshot");
    }
}
