package geoc.uji.esr7.mag_ike.common.status;

/**
 * Created by pajarito on 22/07/2017.
 */

public class TopPlayer {


    private String avatar;
    private long value;

    public TopPlayer (String avatar, long value){
        this.avatar = avatar;
        this.value = value;
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
