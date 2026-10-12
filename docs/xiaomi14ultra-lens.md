# 小米 14 Ultra（nezha）镜头深度适配

## 1. 设备信息

| 项目 | 值 |
| --- | --- |
| 设备代号 | nezha |
| 型号 | 小米 14 Ultra (2405CPX3DC / 2405CPX3DG) |
| 后置 | 四摄：超广 12mm + 主摄 23mm(LYT-900 1") + 浮动长焦 75mm + 潜望长焦 120mm |
| 变焦档位 | 0.6× / 1× / 2× / 3.2× / 5× / 10× / 30× / 60× / 120× |

## 2. 代码中的设备判定

文件：`sources/com/codex/leicaapp/CameraZoomCalibration.java`

```java
static boolean stockOpticalDevice(String str) { return "nezha".equals(str); }

public static boolean stockOptical() {
    if (AdaptiveLens.mode(AdaptiveLens.currentMode())) return false;
    return stockOpticalDevice(Build.DEVICE);   // nezha
}
```

含义：
- `Build.DEVICE == "nezha"` 时，`stockOptical()` 为真，启用**原生光学变焦**路径；
- 非逻辑变焦模式（mode 非 167/256）下，镜头切换交给相机驱动（`AdaptiveLens` 日志：
  "optical selection is controlled by the camera driver"）。

## 3. 焦段配置

文件：`sources/com/codex/leicaapp/NezhaLensConfig.java`

该配置为小米 14 Ultra 提供各拍摄模式的变焦档位、镜头命名与刻度。关键方法：

- `X0()`：模式 → 变焦档位（Float[]）
- `c1()`：模式 → 档位命名（如 "3.2-75mm"、"8.6-200mm" 等效焦段）
- `d1()`：模式 → 变焦范围
- `scaleStops()`：模式 → 完整刻度

示例（专业模式 167）：

```
档位   {0.6, 1.0, 3.2, 4.3}
命名   {2: "3.2-75mm", 3: "3.7-85mm", 4: "3.9-90mm"}
```

## 4. 光学切换点自动探测

`AdaptiveLens.build()` 对小米 14 Ultra 的逻辑后置相机：

1. 主摄 FOV = `focalLength / hypot(sensorW, sensorH)`；
2. 遍历 `getPhysicalCameraIds()`（超广 / 主摄 / 长焦 / 潜望）；
3. 子相机 FOV / 主摄 FOV 得到相对倍率，取一位小数；
4. 落在 `CONTROL_ZOOM_RATIO_RANGE` 内的即为光学节点。

在小米 14 Ultra 上预期得到约：`0.6, 1.0, 3.2, 5.0`（长焦/潜望可能因厂商裁剪略有差异，
如 `4.3` 出现在混合档位配置中）。

## 5. 变焦请求下发

`AdaptiveLens.apply()`：

- 现代设备（SDK≥30 且相机支持）走 `CONTROL_ZOOM_RATIO`；
- 回退路径用 `SCALER_CROP_REGION` 裁剪；
- 专业模式同步 `xiaomi.sat.targetzoom` 与 `com.xiaomi.camera.userZoomRatio.userZoomRatio`。

## 6. 适配建议（针对 nezha）

1. **优先使用原生光学**：小米 14 Ultra 驱动已做多摄切换，`stockOptical()` 返回 true 时不应强制裁剪，
   避免与驱动抢控制权导致画质下降；
2. **变焦档位对齐**：UI 刻度应与 `NezhaLensConfig` 的 0.6/1/3.2/5× 一致，避免出现 4.3× 这类
   非光学节点被误当作光学点；
3. **潜望长焦（5×）触发**：逻辑变焦超过约 4.3–5× 时由驱动切至潜望，若自定义 `SCALER_CROP_REGION`
   需保证不越过潜望的最小可用区域；
4. **专业模式（167）**：使用 `applyProfessionalZoom` 路径，同时设置 ratio 与厂商 zoom 键；
5. **平滑过渡**：`SmoothZoom` 的对数时长在 3.2→5× 这类跨镜头跳变时体验较好，建议保留。

## 7. 风险与注意

- 直接操作 `SCALER_CROP_REGION` 与厂商键可能触发相机 HAL 的合法性校验（`AdaptiveLens` 已用
  try/catch 与键集合过滤兜底）；
- 跨镜头切换瞬间的曝光/白平衡跳变依赖 `OemExtensionSession` 的 EV 自适应；
- 本适配基于对 `c版.apk` 的静态分析，未在真机运行验证，需在小米 14 Ultra 上实测校准。
