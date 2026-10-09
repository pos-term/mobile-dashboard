package com.posterm.mobiledashboard.model;

import com.google.gson.annotations.SerializedName;

public enum ProblemCode {
    @SerializedName("invalid_parameter")
    INVALID_PARAMETER,
    @SerializedName("unauthorized")
    UNAUTHORIZED,
    @SerializedName("not_found")
    NOT_FOUND,
    @SerializedName("internal_error")
    INTERNAL_ERROR
}
