package org.apache.isis.applib.metamodel;

public enum ManagedObjectSort {
    
    /**
     * Stateful object, with a state that can be marshaled and unmarshaled.  
     */
    VIEW_MODEL,
    
    /**
     * Persistable object, associated with a persistence layer/context. 
     */
    ENTITY,
    
    /**
     * Injectable object, associated with a lifecycle context 
     * (application-scoped, request-scoped, ...).
     */
    BEAN, 
    
    /**
     * Object associated with an 'entity' or 'bean' to act as contributer of 
     * domain actions or properties.  
     */
    MIXIN,
    
    /**
     * Immutable, serializable object.
     */
    VALUE,
    
    /**
     * Container of objects.
     */
    COLLECTION,
    
    UNKNOWN;

    public boolean isBean() {
        return this == BEAN;
    }

    public boolean isMixin() {
        return this == MIXIN;
    }

    public boolean isViewModel() {
        return this == VIEW_MODEL;
    }

    public boolean isValue() {
        return this == VALUE;
    }

    public boolean isCollection() {
        return this == COLLECTION;
    }

    public boolean isEntity() {
        return this == ENTITY;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

}