package org.apache.isis.applib.metamodel;

public enum ManagedObjectSort { 
    VIEW_MODEL,
    ENTITY,
    DOMAIN_SERVICE, //TODO [2033] this includes BEANS, shall we add 'BEAN' as separate option, or rename? 
    MIXIN,
    VALUE,
    COLLECTION,
    UNKNOWN;

    public boolean isDomainService() {
        return this == DOMAIN_SERVICE;
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