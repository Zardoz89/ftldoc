/**
 * VariableInfo.java
 */
package freemarker.tools.ftldoc;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Bean that represents all info about a variable, global or external
 */
public class VariableInfo implements Comparable<VariableInfo> {

    private final String name;
    private final VariableType type;
    private final TemplateElementModel node;
    private final String filename;
    private final Map<String, Object> commentText;

    public VariableInfo(String name, VariableType type, TemplateElementModel node, String filename,
            Map<String, Object> commentText) {
        this.name = name;
        this.type = type;
        this.node = node;
        this.filename = filename;
        this.commentText = commentText;
    }

    public String getName() {
        return this.name;
    }

    public VariableType getType() {
        return this.type;
    }

    public TemplateElementModel getNode() {
        return this.node;
    }

    public String getFilename() {
        return this.filename;
    }

    public Map<String, Object> getCommentText() {
        return Collections.unmodifiableMap(this.commentText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.filename, this.name, this.type);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        VariableInfo other = (VariableInfo) obj;
        return Objects.equals(this.filename, other.filename) && Objects.equals(this.name, other.name)
                && this.type == other.type;
    }

    @Override
    public int compareTo(VariableInfo other) {
        CompareToBuilder builder = new CompareToBuilder();
        builder.append(this.type, other.type).append(this.name, other.name).append(this.filename, other.filename);

        return builder.toComparison();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("name", this.name).append("type", this.type).append("node", this.node).append("filename", this.filename);
        return builder.build();
    }

    /**
     * Type of variable
     */
    public static enum VariableType {
        GLOBAL,
        EXTERNAL;
    }
}
