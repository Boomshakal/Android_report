package com.megmeet.megmeetreport.bean;

import java.io.Serializable;

public class SQLBean implements Serializable {
    //获取看板信息
    private String url;            //加载看板的链接
    private String location;      //位置
    private String mac_address;      //
    private String work_shop;      //车间
    private int update_time;      //更新时间
    private int change_time;      //切换时间
    private String has_voice;    //是否有需要播报
    private String voice_procedure;    //播报的数据获取的存储过程
    private int has_hik_version ;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getWork_shop() {
        return work_shop;
    }

    public void setWork_shop(String work_shop) {
        this.work_shop = work_shop;
    }

    public int getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(int update_time) {
        this.update_time = update_time;
    }

    public int getChange_time() {
        return change_time;
    }

    public void setChange_time(int change_time) {
        this.change_time = change_time;
    }

    public String getHas_voice() {
        return has_voice;
    }

    public void setHas_voice(String has_voice) {
        this.has_voice = has_voice;
    }

    public String getVoice_procedure() {
        return voice_procedure;
    }

    public void setVoice_procedure(String voice_procedure) {
        this.voice_procedure = voice_procedure;
    }

    public int getHas_hik_version() {
        return has_hik_version;
    }

    public void setHas_hik_version(int has_hik_version) {
        this.has_hik_version = has_hik_version;
    }

    @Override
    public String toString() {
        return "(" + has_hik_version + "," + voice_procedure + "," + has_voice + "," + url + ")";
    }
}
