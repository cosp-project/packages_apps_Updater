
package com.updates.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Update {

    @SerializedName("update")
    @Expose
    private Boolean update;
    @SerializedName("download")
    @Expose
    private String download;
    @SerializedName("changeLog")
    @Expose
    private String changeLog;

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

}