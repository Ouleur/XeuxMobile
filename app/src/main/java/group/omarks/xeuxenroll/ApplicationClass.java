package group.omarks.xeuxenroll;

import android.app.Application;
import android.content.Context;

public class ApplicationClass extends Application {
    private static Context mContext;
    public static ApplicationClass instace;
    @Override    public void onCreate() {
        super.onCreate();        mContext = getApplicationContext();        instace = this;    }

    @Override    public Context getApplicationContext() {
        return super.getApplicationContext();    }

    public static ApplicationClass getInstance() {
        return instace;    }
}