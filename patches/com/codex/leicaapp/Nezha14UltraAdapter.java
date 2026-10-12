package com.codex.leicaapp;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.SizeF;

/**
 * 小米 14 Ultra（nezha）镜头调用深度适配。
 *
 * 基于对 c版.apk 注入层（AdaptiveLens / NezhaLensConfig / CameraZoomCalibration）的逆向分析，
 * 提供面向 nezha 的镜头枚举、光学切换点探测与变焦档位校正。
 *
 * 设计原则：
 *  - 优先尊重厂商驱动的原生多摄切换（stockOptical）；
 *  - 仅在需要时（逻辑变焦/专业模式）注入 zoom/crop；
 *  - 光学节点以物理子相机 FOV 实测为准，避免硬编码漂移。
 *
 * 注意：本类为适配示例/补丁，需结合实际 HAL 行为在真机校准。
 */
public final class Nezha14UltraAdapter {

    private static final String TAG = "Nezha14U";
    private static final String DEVICE_NEZHA = "nezha";

    // 小米 14 Ultra 四摄等效焦段（mm）与变焦倍率
    public static final float ZOOM_UW = 0.6f;     // 超广 12mm
    public static final float ZOOM_WIDE = 1.0f;   // 主摄 23mm
    public static final float ZOOM_TELE = 3.2f;   // 浮动长焦 75mm
    public static final float ZOOM_PERISCOPE = 5.0f; // 潜望 120mm

    private Nezha14UltraAdapter() {
    }

    /** 是否为小米 14 Ultra。 */
    public static boolean isNezha() {
        return DEVICE_NEZHA.equalsIgnoreCase(Build.DEVICE);
    }

    /**
     * 探测后置逻辑相机的真实光学切换倍率。
     *
     * @param manager CameraManager
     * @param logicalId 逻辑相机 id（通常 "0"）
     * @return 升序光学倍率数组；探测失败返回 {0.6,1.0,3.2,5.0} 的静态回退
     */
    public static float[] opticalStops(CameraManager manager, String logicalId) {
        try {
            CameraCharacteristics logical = manager.getCameraCharacteristics(logicalId);
            double mainFov = fov(logical);
            java.util.TreeSet<Float> stops = new java.util.TreeSet<>();
            stops.add(ZOOM_WIDE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mainFov > 0) {
                for (String phys : logical.getPhysicalCameraIds()) {
                    CameraCharacteristics pc = manager.getCameraCharacteristics(phys);
                    double f = fov(pc);
                    if (f > 0) {
                        float ratio = Math.round((f / mainFov) * 10f) / 10f;
                        if (ratio > 0) {
                            stops.add(ratio);
                        }
                    }
                }
            }
            if (stops.size() <= 1) {
                // 探测失败：使用 nezha 静态回退
                return new float[]{ZOOM_UW, ZOOM_WIDE, ZOOM_TELE, ZOOM_PERISCOPE};
            }
            float[] out = new float[stops.size()];
            int i = 0;
            for (float v : stops) {
                out[i++] = v;
            }
            return out;
        } catch (Exception | LinkageError e) {
            Log.w(TAG, "opticalStops failed, using fallback", e);
            return new float[]{ZOOM_UW, ZOOM_WIDE, ZOOM_TELE, ZOOM_PERISCOPE};
        }
    }

    /**
     * 将连续变焦倍率吸附到最近的光学节点（用于 UI 刻度/切镜决策）。
     */
    public static float nearestOptical(float zoom, float[] stops) {
        if (stops == null || stops.length == 0) {
            return zoom;
        }
        float best = stops[0];
        float bestDiff = Math.abs(zoom - best);
        for (float s : stops) {
            float d = Math.abs(zoom - s);
            if (d < bestDiff) {
                bestDiff = d;
                best = s;
            }
        }
        return best;
    }

    /**
     * 判断某倍率应走原生光学（交给驱动切镜）还是逻辑变焦（应用侧裁剪/ratio）。
     *
     * 对 nezha：非常接近光学节点时走原生；其余走逻辑变焦。
     */
    public static boolean preferNativeOptical(float zoom, float[] stops) {
        float nearest = nearestOptical(zoom, stops);
        return Math.abs(zoom - nearest) < 0.05f;
    }

    /** 计算相机 FOV（焦距 / 传感器对角线）。 */
    private static double fov(CameraCharacteristics c) {
        try {
            float[] focal = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            SizeF size = c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            if (focal == null || focal.length == 0 || focal[0] <= 0
                    || size == null || size.getWidth() <= 0 || size.getHeight() <= 0) {
                return Double.NaN;
            }
            return focal[0] / Math.hypot(size.getWidth(), size.getHeight());
        } catch (Exception | LinkageError e) {
            return Double.NaN;
        }
    }

    /**
     * 将 zoom 合法化到相机支持范围。
     */
    public static float clampZoom(CameraCharacteristics c, float zoom) {
        try {
            Range<Float> r = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    ? c.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE) : null;
            if (r != null) {
                return r.clamp(zoom);
            }
            Float max = c.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            float hi = (max == null || max < 1f) ? 1f : max;
            return Math.max(1f, Math.min(hi, zoom));
        } catch (Exception | LinkageError e) {
            return zoom;
        }
    }
}
