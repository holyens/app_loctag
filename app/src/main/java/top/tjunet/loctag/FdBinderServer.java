package top.tjunet.loctag;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public class FdBinderServer extends Service {
    ParcelFileDescriptor pfd;
    @Override
    public IBinder onBind(Intent arg0) {
        return new MyBinder();
    }
    class MyBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 0:
                    //pfd = data.readParcelable(null);
                    // 或者
                    pfd = data.readFileDescriptor();
                    break;
                case 1:
                    //reply.writeParcelable(pfd,0);
                    // 或者
                    reply.writeFileDescriptor(pfd.getFileDescriptor());
                    break;

                default:
                    break;
            }
            return true;
        }
    }
}
