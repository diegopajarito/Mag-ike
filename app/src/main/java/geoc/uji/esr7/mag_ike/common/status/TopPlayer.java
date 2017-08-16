package geoc.uji.esr7.mag_ike.common.status;

import geoc.uji.esr7.mag_ike.R;

/**
 * Created by pajarito on 22/07/2017.
 */

public class TopPlayer {

    private String avatar;
    private String device;
    private long value;

    public TopPlayer (String device, long value){
        this.device = device;
        this.value = value;
    }

    public String getDevice() {
        return device;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
