package com.projecty.projectyweb.misc;

public enum RedirectMessageTypes {
    SUCCESS("alert-success"),
    FAILED("alert-danger");
    private final String type;

    RedirectMessageTypes(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
