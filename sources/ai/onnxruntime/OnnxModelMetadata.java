package ai.onnxruntime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OnnxModelMetadata {
    private final Map<String, String> customMetadata;
    private final String description;
    private final String domain;
    private final String graphDescription;
    private final String graphName;
    private final String producerName;
    private final long version;

    OnnxModelMetadata(String producerName, String graphName, String graphDescription, String domain, String description, long version, String[] customMetadataArray) {
        this.producerName = producerName == null ? "" : producerName;
        this.graphName = graphName == null ? "" : graphName;
        this.graphDescription = graphDescription == null ? "" : graphDescription;
        this.domain = domain == null ? "" : domain;
        this.description = description != null ? description : "";
        this.version = version;
        if (customMetadataArray != null && customMetadataArray.length > 0) {
            this.customMetadata = new HashMap();
            if (customMetadataArray.length % 2 == 1) {
                throw new IllegalStateException("Asked for keys and values, but received an odd number of elements.");
            }
            for (int i = 0; i < customMetadataArray.length; i += 2) {
                this.customMetadata.put(customMetadataArray[i], customMetadataArray[i + 1]);
            }
            return;
        }
        this.customMetadata = Collections.emptyMap();
    }

    OnnxModelMetadata(String producerName, String graphName, String graphDescription, String domain, String description, long version, Map<String, String> customMetadata) {
        this.producerName = producerName == null ? "" : producerName;
        this.graphName = graphName == null ? "" : graphName;
        this.graphDescription = graphDescription == null ? "" : graphDescription;
        this.domain = domain == null ? "" : domain;
        this.description = description != null ? description : "";
        this.version = version;
        this.customMetadata = customMetadata == null ? Collections.emptyMap() : customMetadata;
    }

    public OnnxModelMetadata(OnnxModelMetadata other) {
        Map<String, String> map;
        this.producerName = other.producerName;
        this.graphName = other.graphName;
        this.graphDescription = other.graphDescription;
        this.domain = other.domain;
        this.description = other.description;
        this.version = other.version;
        if (other.customMetadata.isEmpty()) {
            map = Collections.emptyMap();
        } else {
            map = new HashMap<>(getCustomMetadata());
        }
        this.customMetadata = map;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnnxModelMetadata that = (OnnxModelMetadata) o;
        if (this.version == that.version && this.producerName.equals(that.producerName) && this.graphName.equals(that.graphName) && this.graphDescription.equals(that.graphDescription) && this.domain.equals(that.domain) && this.description.equals(that.description) && this.customMetadata.equals(that.customMetadata)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.producerName, this.graphName, this.graphDescription, this.domain, this.description, Long.valueOf(this.version), this.customMetadata);
    }

    public String getProducerName() {
        return this.producerName;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public String getGraphDescription() {
        return this.graphDescription;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getDescription() {
        return this.description;
    }

    public long getVersion() {
        return this.version;
    }

    public Map<String, String> getCustomMetadata() {
        return Collections.unmodifiableMap(this.customMetadata);
    }

    public Optional<String> getCustomMetadataValue(String key) {
        return Optional.ofNullable(this.customMetadata.get(key));
    }

    public String toString() {
        return "OnnxModelMetadata{producerName='" + this.producerName + "', graphName='" + this.graphName + "', graphDescription='" + this.graphDescription + "', domain='" + this.domain + "', description='" + this.description + "', version=" + this.version + ", customMetadata=" + this.customMetadata + '}';
    }
}