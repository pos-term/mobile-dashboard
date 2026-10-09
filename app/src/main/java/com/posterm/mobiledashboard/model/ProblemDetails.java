package com.posterm.mobiledashboard.model;

public final class ProblemDetails {
    private String type;
    private String title;
    private int status;
    private String detail;
    private ProblemCode code;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public ProblemCode getCode() {
        return code;
    }
}
