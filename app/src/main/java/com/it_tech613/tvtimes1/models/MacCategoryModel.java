package com.it_tech613.tvtimes1.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MacCategoryModel {

    @Expose
    @SerializedName("censored")
    private int censored;
    @Expose
    @SerializedName("alias")
    private String alias;
    @Expose
    @SerializedName("number")
    private int number;
    @Expose
    @SerializedName("modified")
    private String modified;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("id")
    private String id;

    public int getCensored() {
        return censored;
    }

    public String getAlias() {
        return alias;
    }

    public int getNumber() {
        return number;
    }

    public String getModified() {
        return modified;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
