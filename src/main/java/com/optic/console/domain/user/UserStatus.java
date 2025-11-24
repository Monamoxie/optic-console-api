package com.optic.console.domain.user;


public enum UserStatus {

    PENDING,

    ACTIVE,

    INACTIVE,

    SUSPENDED;

    public boolean isActive() {
        return this == ACTIVE;
    }

   static UserStatus getDefault() {
        return PENDING;
    }
}
