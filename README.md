# Leica Camera 逆向工程 / 小米 14 Ultra 镜头适配

对安装包 `c版.apk`（小米徕卡相机「徕卡一瞬」风味版，包名 `com.kobbin.leica.compatccc`）的逆向分析，
并针对 **小米 14 Ultra（设备代号 `nezha`）** 的镜头调用进行深度适配。

> 免责声明：本项目仅用于安全研究、逆向工程学习与个人设备适配研究。所有原始应用、商标、图像资源版权归
> 小米 / 徕卡（Leica Camera AG）所有。请勿用于商业分发。

---

## 1. 安装包概览

| 项目 | 值 |
| --- | --- |
| 文件名 | c版.apk |
| 大小 | 约 500 MB |
| 包名 | com.kobbin.leica.compatccc |
| 应用名 | 徕卡一瞬（Leica Moments） |
| compileSdkVersion | 36 (Android 16) |
| DEX 数量 | 18 个（classes.dex ~ classes18.dex） |
| 类总数 | 约 36,438 |
| 原生库 | lib/arm64-v8a/*.so（CameraUtils、QnnHtp、mialgo、heif、yuv 等） |
| 关键 assets | leica-v4/、fullm9/、leica_filter_param_*.bin、cacnet.onnx 等 |

这是一个被**重打包 + 代码注入**的小米相机 APK：
- 原始包名被改为 `com.kobbin.leica.compatccc`；
- 注入了 `com.codex.*` 命名空间的适配层（约 117 个类），直接织入小米相机原有类（`com.android.camera.*`、
  `r2.*`、`v6.*`、`l9.*`、`Tu/Uu` 等）之中，用于开启「徕卡风味」并接管镜头/变焦/色彩管线。

---

## 2. 注入的适配层结构（com.codex.*）

| 包 | 作用 |
| --- | --- |
| com.codex.leicaapp | 核心适配层：镜头/变焦接管、徕卡色彩、M9 影调、AI 构图、水印等 |
| com.codex.fullm9 | Full M9 引擎：DNG/RAW、HDR 增益图、后台拍摄、EXIF 处理 |
| com.codex.lmport | 预览影调（PreviewTone）、HDR、降噪流程（DenoiseFlow） |
| com.codex.lmoments | 徕卡时刻图像处理（EXIF 复制、影调） |
| ai.onnxruntime | ONNX Runtime（本地 AI 推理，如 cacnet.onnx 人像分割/风格迁移） |

### 镜头调用相关核心类（重点）

| 类 | 文件 | 职责 |
| --- | --- | --- |
| AdaptiveLens | sources/com/codex/leicaapp/AdaptiveLens.java | **逻辑变焦/多摄接管核心**。枚举后置逻辑相机、探测物理子相机、计算光学焦段切换点、注入 SCALER_CROP_REGION / CONTROL_ZOOM_RATIO、专业模式变焦 |
| AdaptiveLensBridge | AdaptiveLensBridge.java | 桥接到小米相机 r2.B0 变焦 UI（镜片映射 lensMap、滚轮 wheel） |
| NezhaLensConfig | NezhaLensConfig.java | **小米 14 Ultra（nezha）专用焦段配置**：0.6/1.0/2.0/3.2/4.3/8.6×、17.2/30/60/120× 等档位与命名 |
| CameraZoomCalibration | CameraZoomCalibration.java | 变焦校准、机型判定（stockOpticalDevice("nezha")、baselineDevice("pandora","25098PN5AC")）、原生镜头标签 |
| CameraZoomCalibrationBaseline | CameraZoomCalibrationBaseline.java | 通过物理相机 focalLength/sensorWidth 推算长焦倍率 |
| CameraCompat | CameraCompat.java | CaptureRequest 创建/构建/会话的兼容包装，统一接入 AdaptiveLens |
| SmoothZoom | SmoothZoom.java | 变焦平滑过渡动画（对数时长，350–1000ms） |
| OemExtensionSession | OemExtensionSession.java | Camera2 OEM 扩展会话，接管 EV/WB，实现自适应曝光 |
| MomentCaptureChain | MomentCaptureChain.java | 拍摄链路接管：createRequest/buildRequest/createSession/remember |

### 注入点（hook 位置）

com.codex.* 被直接调用的小米相机原始类（部分）：

```
com.android.camera.Camera                      -> AiCompositionController.attach
com.android.camera.data.data.k::W              -> CameraZoomCalibration.markers
com.android.camera.module.Camera2Module        -> MadridBridge.snapshot
r2.B0::n                                        -> AdaptiveLens.currentMode
r2.A0::apply                                    -> AdaptiveLens.label
v6.b::M                                         -> AdaptiveLens.enumerate   (相机枚举)
B2.c::c                                         -> AdaptiveLens.selectForMode
i9.f::J9 / j9.*::L8                             -> AdaptiveLens.switchLens   (切镜)
j9.D/G/l/x::P5                                  -> AdaptiveLens.managerRange
l9.h::S0                                        -> CameraZoomCalibration.nativeLensTag
ma.S::h                                         -> MomentCaptureChain.createSession
l9.*::m / onCaptureCompleted                    -> MomentCaptureChain.buildRequest / remember
Tu/Uu.*::g                                      -> lmport.PreviewTone.draw
com.xiaomi.gl.ShaderManager::b                  -> lmport.PreviewTone.uploadShader
```

---

## 3. 小米 14 Ultra（nezha）镜头适配

### 3.1 设备识别

CameraZoomCalibration 中：

```java
static boolean stockOpticalDevice(String str) { return "nezha".equals(str); }   // 小米 14 Ultra
static boolean baselineDevice(String d, String m) { return "pandora".equals(d) && "25098PN5AC".equals(m); } // 小米 15 Pro
```

- nezha = 小米 14 Ultra 设备代号。
- 当 Build.DEVICE == "nezha" 且当前模式不属于逻辑变焦模式时，走 stockOptical() 原生光学路径。

### 3.2 四摄焦段（NezhaLensConfig）

小米 14 Ultra 后置四摄（超广 / 主摄 / 浮动长焦 / 潜望长焦），对应变焦档位：

| 镜头 | 等效焦段 | 变焦倍率 |
| --- | --- | --- |
| 超广角 (UW) | 12mm | 0.6× |
| 主摄 (Wide, 1 英寸 LYT-900) | 23mm | 1.0× |
| 浮动长焦 (Tele) | 75mm | 3.2× |
| 潜望长焦 (Super Tele) | 120mm | 5.0×（可至 8.6× 光学/混合） |

NezhaLensConfig.X0() 返回各模式下的变焦档位：

```
256 -> {0.6, 1.0, 2.0, 3.2, 4.3}
163 -> {0.6, 1.0, 2.0, 3.2, 4.3, 8.6}
167 -> {0.6, 1.0, 3.2, 4.3}          (专业模式 PRO)
171 -> {1.0, 2.0, 3.2, 4.3}
...
```

scaleStops() 给出完整刻度（含 1.2/1.5/3.7/3.9/17.2/30/60/120× 等混合变焦档）。

### 3.3 光学切换点自动探测（AdaptiveLens.build）

对每个后置逻辑相机：
1. 读取 LENS_INFO_AVAILABLE_FOCAL_LENGTHS 与 SENSOR_INFO_PHYSICAL_SIZE 计算 FOV；
2. 遍历 getPhysicalCameraIds() 获取物理子相机 FOV；
3. 计算各子相机相对主摄的 FOV 比值 round(fovRatio*10)/10，落在变焦范围内的即为**光学切换倍率**；
4. 结果写入 Profile.stops，作为 UI 刻度（wide/ultra/tele）与切换阈值。

这样在小米 14 Ultra 上可自动识别 0.6× / 1× / 3.2× / 5× 的真实光学节点，而非依赖硬编码。

### 3.4 变焦请求注入（AdaptiveLens.apply）

对每个 CaptureRequest.Builder：
- 若支持 CONTROL_ZOOM_RATIO：直接 clamp 到合法范围下发；
- 否则回退到 SCALER_CROP_REGION 裁剪计算（保证不超过 sensor active array，并对齐到最大变焦的最小区域）；
- 专业模式（mode 167）额外同步厂商键 xiaomi.sat.targetzoom、com.xiaomi.camera.userZoomRatio.userZoomRatio。

---

## 4. 目录结构

```
LeicaReverse/
├── README.md                    本文件
├── docs/
│   ├── analysis.md              逆向分析详细报告
│   └── xiaomi14ultra-lens.md    小米 14 Ultra 镜头适配说明
├── sources/                     反编译得到的 Java 源码
│   ├── com/codex/leicaapp/      核心适配层（镜头/变焦/影调）
│   ├── com/codex/fullm9/        Full M9 / RAW 引擎
│   ├── com/codex/lmport/        预览影调 / HDR / 降噪
│   └── ai/onnxruntime/          ONNX Runtime
└── patches/                     面向小米 14 Ultra 的适配补丁/示例
```

---

## 5. 复现步骤

```bash
# 1. 解包
apktool d c版.apk -o apktool_out --no-src

# 2. 反编译关键 dex（小体积、含注入层）
jadx -d jadx_small classes11.dex classes12.dex classes13.dex \
     classes14.dex classes16.dex classes17.dex classes18.dex

# 3. 全量反编译（需较大内存）
jadx -d jadx_out --show-bad-code -j 4 classes*.dex
```

---

## 6. 说明

- 源码由 JADX 反编译得到，混淆严重（类名如 r2.B0、l9.*、Tu/Uu），变量名可能不精确，但逻辑完整。
- 反编译产物仅供研究，不保证可重新编译运行。
