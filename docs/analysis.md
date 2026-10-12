# 逆向分析报告

## 1. 方法与工具链

- 解包：`apktool d c版.apk --no-src`（AndroidManifest、资源、assets、lib）
- 类扫描：`androguard`（统计 dex 类分布、定位注入点）
- 反编译：`jadx`（关键 dex 全量；大 dex 按需）
- 交叉引用：androguard 字节码扫描，定位 `com.codex.*` 被调用的宿主方法

## 2. DEX 类分布

| dex | 类数 | 说明 |
| --- | --- | --- |
| classes.dex | 8776 | 主程序 `com.android.camera.*`，注入点最多 |
| classes2.dex | 7657 | UI/渲染（`Tu/Uu`、`Kr/i`） |
| classes3.dex | 10642 | 相机链路 `l9.*`、`ma.*`、FaceUnity |
| classes4.dex | 868 | 拍摄/缩略图 |
| classes5.dex | 646 | 水印、影调 |
| classes8.dex | 2427 | 小米相机框架（zoom/panel） |
| classes10.dex | 1710 | 其它 |
| classes11.dex | 157 | **注入层：NezhaLensConfig、CameraZoomCalibration、SmoothZoom、M9 系列** |
| classes14.dex | 4 | **注入层：AdaptiveLens、AdaptiveLensBridge、CameraCompat** |
| classes16.dex | 4 | **注入层：lmport.HdrMath** |
| classes17.dex | 78 | **注入层：com.codex.fullm9** |
| classes15.dex | 1095 | leicamoments（图片处理） |

## 3. 注入层全景

注入层通过**直接改写宿主类字节码**的方式接入，而非运行期 hook（无 Xposed/LSPosed 依赖）。
典型改写形态：

- 在 `com.android.camera.Camera.onCreate/onResume` 中插入 `AiCompositionController.attach(this)`；
- 在 `v6.b.M()`（相机枚举）中插入 `AdaptiveLens.enumerate(...)`，把系统返回的相机 ID 列表替换为
  逻辑相机列表；
- 在 `l9.*::m`（构建 CaptureRequest）与 `onCaptureCompleted` 中插入
  `MomentCaptureChain.buildRequest/remember`；
- 在 `j9.*::P5`、`i9.f::J9` 中插入 `AdaptiveLens.managerRange/switchLens`，接管变焦范围与切镜；
- 在 `Tu/Uu.*::g` 中插入 `PreviewTone.draw`，接管预览影调；
- 在 `com.xiaomi.gl.ShaderManager.b` 中插入 `PreviewTone.uploadShader`，注入徕卡 LUT shader。

## 4. 关键机制

### 4.1 逻辑相机枚举与物理相机探测（AdaptiveLens）

```
enumerate(CameraManager, ids, mode)
  ├─ getCameraIdList()                // 系统逻辑相机
  ├─ build(id)                        // 为每个 id 构建 Profile
  │    ├─ LENS_FACING / SENSOR_INFO_PHYSICAL_SIZE / FOCAL_LENGTHS → FOV
  │    ├─ getPhysicalCameraIds()      // 后置物理子相机
  │    ├─ fovRatio = fov(child)/fov(main)  → stops（光学切换点）
  │    └─ CONTROL_ZOOM_RATIO_RANGE / SCALER_AVAILABLE_MAX_DIGITAL_ZOOM → zoom 范围
  └─ primary = id "0" 或面积最大的后置逻辑相机
```

### 4.2 变焦请求注入（apply）

- 支持 `CONTROL_ZOOM_RATIO`：`builder.set(CONTROL_ZOOM_RATIO, clamp(zoom))`；
- 否则用 `SCALER_CROP_REGION`：以 sensor active array 为基准按倍率裁剪，并保证不小于最大变焦所需最小区域；
- 过滤宿主设置的非 `android.*` 且不在当前相机可用键集合内的键（避免下发不支持的厂商键）。

### 4.3 平滑变焦（SmoothZoom）

- 拦截变焦设置，若新旧倍率比值 ≥ 1.5 则用 `ValueAnimator` 做对数时长过渡（350–1000ms）；
- 逐帧调用宿主 `v0(float,int)` 完成动画。

### 4.4 OEM 扩展会话（OemExtensionSession）

- 包装 `CameraExtensionSession`，实现 `CameraCaptureSession` 接口；
- 通过 `PixelCopy` 每 500ms 采样预览 64×64 像素，计算 EV 偏差并自适应调整
  `CONTROL_AE_EXPOSURE_COMPENSATION`（步进 ±0.1667）；
- 用于「徕卡影调」的曝光一致性。

## 5. 色彩/影调管线

- `LegendaryPostProcessor`（1018 行）：M9/M3 影调、白平衡（`org.codeaurora.qcamera3.manualWB.*`）、
  预览/拍摄元数据；
- `LeicaQuality` / `SceneColorTransfer` / `LeicaDynamicRange`：色彩迁移与动态范围；
- `lmport.PreviewTone`：GL shader 预览影调；
- `fullm9.*`：Full M9 RAW/DNG、HDR 增益图（`HdrGainmaps`、`HdrMath`）。

## 6. 免责声明

本项目仅用于安全研究与逆向学习。原始 APK 及其资源版权归小米 / 徕卡所有。
