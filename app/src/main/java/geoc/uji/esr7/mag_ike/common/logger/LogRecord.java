package geoc.uji.esr7.mag_ike.common.logger;

import android.content.res.Resources;
import geoc.uji.esr7.mag_ike.R;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

/**
 * Created by pajarito on 09/09/2017.
 */

public class LogRecord {

    private static LogRecord instance = null;

    private ParseObject parseObject;
    private String log_record_class;
    private String device;
    private String device_tag;
    private String log_text_tag;

    protected LogRecord(){
        // Just to protect
    }

    public static LogRecord getInstance(){
        if (instance == null){
            instance = new LogRecord();
        }
        return instance;
    }

    public void setUpLogRecord(Resources res, String dev){
        log_record_class = res.getString(R.string.log_record_class_parse);
        device_tag = res.getString(R.string.device_tag);
        device = dev;
        log_text_tag = res.getString(R.string.log_text_tag);
    }

    public void writeLog_Eventually(String log_text){
        parseObject = new ParseObject(this.log_record_class);
        parseObject.put(device_tag, device);
        parseObject.put(log_text_tag, log_text);
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - LOG SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE LOG FAILED", String.valueOf(e));
                }
            }
        });

    }

}
