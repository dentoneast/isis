package org.apache.isis.core.metamodel.specloader.validator;

import java.io.Serializable;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
public class MetaModelDeficiencies implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Getter private final Set<String> validationErrors;
    
    public String getValidationErrorsAsString() {
        return concatenate(validationErrors);
    }

    // //////////////////////////////////////

    private static String concatenate(Set<String> messages) {
        final StringBuilder buf = new StringBuilder();
        int i=0;
        for (String message : messages) {
            buf.append(++i).append(": ").append(message).append("\n");
        }
        return buf.toString();
    }
}
