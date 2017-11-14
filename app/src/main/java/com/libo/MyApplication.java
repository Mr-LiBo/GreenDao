package com.libo;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.libo.db.DaoMaster;
import com.libo.db.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by liaodp on 2017/11/3.
 */

public class MyApplication extends Application
{

    private DaoSession daoSession;



    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper helper =new DaoMaster.DevOpenHelper(this,"cache_db");
        Database db = helper.getWritableDb();
        daoSession= new DaoMaster(db).newSession();
    }
    public DaoSession getDaoSession(){
        return daoSession;
    }
}
