package com.codex.fullm9;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class ManifestHashes {
    ManifestHashes() {
    }

    static String hash(String str) {
        if ("low".equals(str)) {
            return "89f7e4e515467a6f47c9d74538a08d25ed7e148474fca2e3713db5e9259cf406";
        }
        if ("high".equals(str)) {
            return "61eedf9d72141c0f5262a2b2d7791ea27d36b77cfb63a8312b007e447b14da73";
        }
        if ("colorfix".equals(str)) {
            return "381e0c48c241b3a1eaf09843ab9cb43610a96eae7708c0316b17a8661620b74f";
        }
        throw new IllegalArgumentException(str);
    }
}
