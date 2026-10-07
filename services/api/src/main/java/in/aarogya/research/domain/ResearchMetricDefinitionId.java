package in.aarogya.research.domain;

import java.io.Serializable;
import java.util.Objects;

public class ResearchMetricDefinitionId implements Serializable {

    private String metricCode;
    private int metricVersion;

    public ResearchMetricDefinitionId() {
    }

    public ResearchMetricDefinitionId(
        String metricCode,
        int metricVersion
    ) {
        this.metricCode = metricCode;
        this.metricVersion = metricVersion;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ResearchMetricDefinitionId that)) {
            return false;
        }
        return metricVersion == that.metricVersion
            && Objects.equals(metricCode, that.metricCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricCode, metricVersion);
    }
}
