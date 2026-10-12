package ai.onnxruntime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
final class OnnxRuntime {
    static final String ONNXRUNTIME_JNI_LIBRARY_NAME = "onnxruntime4j_jni";
    static final String ONNXRUNTIME_LIBRARY_CUDA_NAME = "onnxruntime_providers_cuda";
    static final String ONNXRUNTIME_LIBRARY_DNNL_NAME = "onnxruntime_providers_dnnl";
    static final String ONNXRUNTIME_LIBRARY_NAME = "onnxruntime";
    static final String ONNXRUNTIME_LIBRARY_OPENVINO_NAME = "onnxruntime_providers_openvino";
    static final String ONNXRUNTIME_LIBRARY_QNN_NAME = "onnxruntime_providers_qnn";
    static final String ONNXRUNTIME_LIBRARY_ROCM_NAME = "onnxruntime_providers_rocm";
    static final String ONNXRUNTIME_LIBRARY_SHARED_NAME = "onnxruntime_providers_shared";
    static final String ONNXRUNTIME_LIBRARY_TENSORRT_NAME = "onnxruntime_providers_tensorrt";
    static final String ONNXRUNTIME_NATIVE_PATH = "onnxruntime.native.path";
    private static final int ORT_API_VERSION_1 = 1;
    private static final int ORT_API_VERSION_11 = 11;
    private static final int ORT_API_VERSION_13 = 13;
    private static final int ORT_API_VERSION_14 = 14;
    private static final int ORT_API_VERSION_2 = 2;
    private static final int ORT_API_VERSION_3 = 3;
    private static final int ORT_API_VERSION_7 = 7;
    private static final int ORT_API_VERSION_8 = 8;
    private static final int ORT_TRAINING_API_VERSION_1 = 1;
    private static String libraryDirPathProperty;
    static long ortApiHandle;
    static long ortTrainingApiHandle;
    static EnumSet<OrtProvider> providers;
    private static Path tempDirectory;
    static boolean trainingEnabled;
    private static String version;
    private static final Logger logger = Logger.getLogger(OnnxRuntime.class.getName());
    private static final String OS_ARCH_STR = initOsArch();
    private static boolean loaded = false;
    private static final Set<String> extractedSharedProviders = new HashSet();

    private static native String[] getAvailableProviders(long j);

    private static native long initialiseAPIBase(int i);

    private static native long initialiseTrainingAPIBase(long j, int i);

    private static native String initialiseVersion();

    private OnnxRuntime() {
    }

    private static String initOsArch() {
        String detectedOS;
        String detectedArch;
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")) {
            detectedOS = "osx";
        } else if (os.contains("win")) {
            detectedOS = "win";
        } else if (os.contains("nux")) {
            detectedOS = "linux";
        } else if (isAndroid()) {
            detectedOS = "android";
        } else {
            throw new IllegalStateException("Unsupported os:" + os);
        }
        String arch = System.getProperty("os.arch", "generic").toLowerCase(Locale.ENGLISH);
        if (arch.startsWith("amd64") || arch.startsWith("x86_64")) {
            detectedArch = "x64";
        } else if (arch.startsWith("x86")) {
            detectedArch = "x86";
        } else if (arch.startsWith("aarch64")) {
            detectedArch = "aarch64";
        } else if (arch.startsWith("ppc64")) {
            detectedArch = "ppc64";
        } else if (isAndroid()) {
            detectedArch = arch;
        } else {
            throw new IllegalStateException("Unsupported arch:" + arch);
        }
        return detectedOS + '-' + detectedArch;
    }

    static synchronized void init() throws IOException {
        if (loaded) {
            return;
        }
        tempDirectory = isAndroid() ? null : Files.createTempDirectory("onnxruntime-java", new FileAttribute[0]);
        try {
            libraryDirPathProperty = System.getProperty(ONNXRUNTIME_NATIVE_PATH);
            extractProviderLibrary(ONNXRUNTIME_LIBRARY_SHARED_NAME);
            if (!isAndroid()) {
                load(ONNXRUNTIME_LIBRARY_NAME);
            }
            load(ONNXRUNTIME_JNI_LIBRARY_NAME);
            long jInitialiseAPIBase = initialiseAPIBase(ORT_API_VERSION_14);
            ortApiHandle = jInitialiseAPIBase;
            if (jInitialiseAPIBase == 0) {
                throw new IllegalStateException("There is a mismatch between the ORT class files and the ORT native library, and the native library could not be loaded");
            }
            long jInitialiseTrainingAPIBase = initialiseTrainingAPIBase(jInitialiseAPIBase, ORT_API_VERSION_14);
            ortTrainingApiHandle = jInitialiseTrainingAPIBase;
            trainingEnabled = jInitialiseTrainingAPIBase != 0;
            providers = initialiseProviders(ortApiHandle);
            version = initialiseVersion();
            loaded = true;
            Path path = tempDirectory;
            if (path != null) {
                cleanUp(path.toFile());
            }
        } catch (Throwable th) {
            Path path2 = tempDirectory;
            if (path2 != null) {
                cleanUp(path2.toFile());
            }
            throw th;
        }
    }

    private static void cleanUp(File file) {
        if (!file.exists()) {
            return;
        }
        logger.log(Level.FINE, "Deleting " + file + " on exit");
        file.deleteOnExit();
    }

    static String version() {
        return version;
    }

    static boolean extractCUDA() {
        return extractProviderLibrary(ONNXRUNTIME_LIBRARY_CUDA_NAME);
    }

    static boolean extractROCM() {
        return extractProviderLibrary(ONNXRUNTIME_LIBRARY_ROCM_NAME);
    }

    static boolean extractDNNL() {
        return extractProviderLibrary(ONNXRUNTIME_LIBRARY_DNNL_NAME);
    }

    static boolean extractOpenVINO() {
        return extractProviderLibrary(ONNXRUNTIME_LIBRARY_OPENVINO_NAME);
    }

    static boolean extractTensorRT() {
        return extractProviderLibrary(ONNXRUNTIME_LIBRARY_TENSORRT_NAME);
    }

    static boolean extractQNN() {
        return extractProviderLibrary(ONNXRUNTIME_LIBRARY_QNN_NAME);
    }

    static synchronized boolean extractProviderLibrary(String libraryName) {
        if (isAndroid()) {
            return false;
        }
        Set<String> set = extractedSharedProviders;
        if (set.contains(libraryName)) {
            return true;
        }
        Optional<File> file = extractFromResources(libraryName);
        if (file.isPresent()) {
            set.add(libraryName);
            return true;
        }
        if (libraryDirPathProperty == null) {
            return false;
        }
        String libraryFileName = mapLibraryName(libraryName);
        File libraryFile = Paths.get(libraryDirPathProperty, libraryFileName).toFile();
        if (!libraryFile.exists()) {
            return false;
        }
        set.add(libraryName);
        return true;
    }

    static boolean isAndroid() {
        return System.getProperty("java.vendor", "generic").equals("The Android Project");
    }

    private static void load(String library) throws IOException {
        if (isAndroid()) {
            System.loadLibrary(library);
            return;
        }
        String skip = System.getProperty("onnxruntime.native." + library + ".skip");
        if (Boolean.TRUE.toString().equalsIgnoreCase(skip)) {
            logger.log(Level.FINE, "Skipping load of native library '" + library + "'");
            return;
        }
        String libraryFileName = mapLibraryName(library);
        if (libraryDirPathProperty != null) {
            Logger logger2 = logger;
            logger2.log(Level.FINE, "Attempting to load native library '" + library + "' from specified path: " + libraryDirPathProperty);
            File libraryFile = Paths.get(libraryDirPathProperty, libraryFileName).toFile();
            String libraryFilePath = libraryFile.getAbsolutePath();
            if (!libraryFile.exists()) {
                throw new IOException("Native library '" + library + "' not found at " + libraryFilePath);
            }
            System.load(libraryFilePath);
            logger2.log(Level.FINE, "Loaded native library '" + library + "' from specified path");
            return;
        }
        String libraryPathProperty = System.getProperty("onnxruntime.native." + library + ".path");
        if (libraryPathProperty != null) {
            Logger logger3 = logger;
            logger3.log(Level.FINE, "Attempting to load native library '" + library + "' from specified path: " + libraryPathProperty);
            File libraryFile2 = new File(libraryPathProperty);
            String libraryFilePath2 = libraryFile2.getAbsolutePath();
            if (!libraryFile2.exists()) {
                throw new IOException("Native library '" + library + "' not found at " + libraryFilePath2);
            }
            System.load(libraryFilePath2);
            logger3.log(Level.FINE, "Loaded native library '" + library + "' from specified path");
            return;
        }
        Optional<File> extractedPath = extractFromResources(library);
        if (extractedPath.isPresent()) {
            System.load(extractedPath.get().getAbsolutePath());
            logger.log(Level.FINE, "Loaded native library '" + library + "' from resource path");
        } else {
            Logger logger4 = logger;
            logger4.log(Level.FINE, "Attempting to load native library '" + library + "' from library path");
            System.loadLibrary(library);
            logger4.log(Level.FINE, "Loaded native library '" + library + "' from library path");
        }
    }

    /* JADX WARN: Code duplicated, block: B:44:0x00c3 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private static Optional<File> extractFromResources(String library) {
        String libraryFileName = mapLibraryName(library);
        String resourcePath = "/ai/onnxruntime/native/" + OS_ARCH_STR + '/' + libraryFileName;
        File tempFile = tempDirectory.resolve(libraryFileName).toFile();
        try {
            try {
                InputStream is = OnnxRuntime.class.getResourceAsStream(resourcePath);
                try {
                    if (is == null) {
                        Optional<File> optionalEmpty = Optional.empty();
                        if (is != null) {
                            is.close();
                        }
                        cleanUp(tempFile);
                        return optionalEmpty;
                    }
                    logger.log(Level.FINE, "Attempting to load native library '" + library + "' from resource path " + resourcePath + " copying to " + tempFile);
                    byte[] buffer = new byte[4096];
                    FileOutputStream os = new FileOutputStream(tempFile);
                    while (true) {
                        try {
                            int readBytes = is.read(buffer);
                            if (readBytes == -1) {
                                break;
                            }
                            os.write(buffer, 0, readBytes);
                        } catch (Throwable th) {
                            try {
                                os.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                            throw th;
                        }
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                        }
                        throw th;
                    }
                    os.close();
                    logger.log(Level.FINE, "Extracted native library '" + library + "' from resource path");
                    Optional<File> optionalOf = Optional.of(tempFile);
                    if (is != null) {
                        is.close();
                    }
                    cleanUp(tempFile);
                    return optionalOf;
                } catch (Throwable th4) {
                    if (is != null) {
                        is.close();
                    }
                    throw th4;
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to extract library '" + library + "' from the resources", (Throwable) e);
                Optional<File> optionalEmpty2 = Optional.empty();
                cleanUp(tempFile);
                return optionalEmpty2;
            }
        } catch (Throwable th5) {
            cleanUp(tempFile);
            throw th5;
        }
    }

    private static String mapLibraryName(String library) {
        return System.mapLibraryName(library).replace("jnilib", "dylib");
    }

    private static EnumSet<OrtProvider> initialiseProviders(long ortApiHandle2) {
        String[] providersArray = getAvailableProviders(ortApiHandle2);
        EnumSet<OrtProvider> providers2 = EnumSet.noneOf(OrtProvider.class);
        for (String provider : providersArray) {
            providers2.add(OrtProvider.mapFromName(provider));
        }
        return providers2;
    }
}
