package com.study.mobliesafe.db;

/**
 * <pre>
 *     author:Msp
 *     time:2018/6/714:35
 *     desc:
 *     version:1.0
 * </pre>
 */
public class Update {

    private String versionCode;
    private String versionName;
    private String versionDes;
    private String downloadUrl;

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionDes() {
        return versionDes;
    }

    public void setVersionDes(String versionDes) {
        this.versionDes = versionDes;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
