### 远程服务的接口回调

Android 开发中的一些场景需要用到进程间通信，一般使用AIDL(Android Interface Definition 

Language)。使用AIDL绑定一个远程服务，远程服务可以被其他应用绑定，绑定后可以使用定义在AIDL接口中的方法来进行一些操作，远程服务会另外开启一个进程。

#### 1、简单使用

- 1.1 就像使用普通的服务一样，首先要创建一个服务类的子类，继承Service。

  RemoteService.java

```java
package com.example.administrator.newpager.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * auther：wzy
 * date：2017/1/8 19 :52
 * desc: 
 */

public class RemoteService extends Service {
    private static final String TAG = "RemoteService";
    private RemoteCallbackList<IMyAidlCallBack> mRemoteCallbackList = new RemoteCallbackList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }
   //Binder类
    class LocalBinder extends IRemoteService.Stub {

        @Override
        public void refreshFromNet() throws RemoteException {
			doSomeThingHere();
        }
    }

}
```

这里与普通服务子类创建的不同之处在于：

​	`普通`服务类中Activity需要与Service绑定时，会在onBind()方法里返回一个IBinder的子类，这个子类只是			   简单方法，可以是Service子类中的方法，也可以继承一个接口，实现需要重写的方法。

​	`这里的IBinder`类则需要继承的是一个创建AIDL后自动生成的一个类，下面会说。

- 1.2  创建AIDl ，在Android Studio 中直接有创建AIDL的选项

 ![aidlCreate](mdSource\aidlCreate.png)

**编写格式像这样：**

IRemoteService.java

```java
package com.example.administrator.newpager.service;
import com.example.administrator.newpager.service.IMyAidlCallBack;

interface IRemoteService  {
    void refreshFromNet();
}
```

其中要注意，文件中需要引用其他文件的时候，一定要带上如上的

`import com.example.administrator.newpager.service.IMyAidlCallBack;`

**即使是在同一个包下**。

- 1.3  创建好AIDL文件后，执行IDE的ReBuild操作，这时候会生成一个Stub文件，如样例中则会生成`IRemoteService.Stub`文件，这个文件的内容细节先不去管，1.1中的Stub就是从这里得到的。

​       ![rebuild](mdSource\rebuild.png)

- 1.4  如1.1中代码所示，继承`IRemoteService.Stub`  类，重写里面的方法，这些方法就是之前AIDL中定义的未实现的方法，实现这些方法很重要，其他应用或者Activity之类的绑定服务 ，就是为了调用这里面的方法来执行一些必要操作。

- 1.5  在 onBind()方法中返回 `IRemoteService.Stub`的子类对象。

- 1.6  在需要调用服务中方法的地方，如一个应用的Activity，这个应用我们称为客户端，在Activity中，就像绑定普通服务一样，执行`mContext.bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);`  

  这里是得到服务对象的重要步骤，bindService是耗时的，因此异步操作从回调中获取远程服务对象。

  **mServiceConnection :**

  ```java
  private ServiceConnection mServiceConnection = new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
              mIRemoteService = IRemoteService.Stub.asInterface(service);
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {

          }
      };
  ```

- 1.7 绑定服务，**bindService();**

  ```java
     private void bindService() {
  //        Intent intent = new Intent("com.example.administrator.newpager.REMOTESERVICE");
  //        intent.setComponent
  //                    (new ComponentName("com.example.administrator.newpager.service",
  //                        "com.example.administrator.newpager.service.RemoteService"));
          Intent intent = new Intent(mContext, RemoteService.class);
          mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
          showTextView();
      }
  ```

  绑定服务操作因Intent的不同，有多重绑定途径。

  ​

- 1.8 与普通服务开启的操作一样，这里也需要 在manifest中需要进行配置。

  也由于使用场景不同，这里可以配合Intent添加一些其他参数。

  ```xml
    <service
             <!-- 这里可以添加一些Action 和 category-->
              android:name=".service.RemoteService">
    </service>
  ```
  ​

- 1.9 在需要调用远程服务中方法的地方调用。



#### 2、远程服务与接口回调配合使用

​	在有些场景中，调用方如其他应用，需要在进程在运行到某个阶段执行调用方的一些操作，或者远程服务在进行一些异步操作的时候需要将结果回调给调用方，这时候，使用接口回调就能解决，Android中也提供了这样的接口：`RemoteCallbackList`  。



-  2.1 步骤与之前使用远程服务类似，不同的是要多创建一个AIDL文件，用于回调的接口类型。

   ```java
   // IMyAidlCallBack.aidl
   package com.example.administrator.newpager.service;

   interface IMyAidlCallBack {
       void callBack(int result);
   }
   ```

-  2.2 在之前的AIDL文件中添加这些

   ```java
    package com.example.administrator.newpager.service;
    import com.example.administrator.newpager.service.IMyAidlCallBack;

    interface IRemoteService  {
        void refreshFromNet();
      //----添加----//
        void registerListener(IMyAidlCallBack callBack);
        void unRegisterListener(IMyAidlCallBack callBack);
        void doInBackGround();
       //-----------//
    }
   ```

-  2.3 在之前的代码中

   ```java
    package com.example.administrator.newpager.service;

    import android.app.Service;
    import android.content.Intent;
    import android.os.IBinder;
    import android.os.RemoteCallbackList;
    import android.os.RemoteException;
    import android.support.annotation.Nullable;
    import android.util.Log;
    import android.widget.Toast;

    /**
   * auther：wzy
   * date：2017/1/8 19 :52
   * desc: 
   */
    public class RemoteService extends Service {
     private static final String TAG = "RemoteService";
     private RemoteCallbackList<IMyAidlCallBack> mRemoteCallbackList = new RemoteCallbackList<>();

     @Nullable
     @Override
     public IBinder onBind(Intent intent) {
         Log.i(TAG, "onBind: ");
         return new LocalBinder();
     }

     @Override
     public void onCreate() {
         Log.i(TAG, "onCreate: ");
         super.onCreate();
     }
        //----------------添加------------------------//
      //执行耗时操作
     public void doInBackGround() {
         new Thread() {
             @Override
             public void run() {
                 long result = this.getId();
                 int count = 0;
                 Log.i(TAG, "mRemoteCallbackList: " +     mRemoteCallbackList+",mRemoteCallbackList.mCallBack:"+mRemoteCallbackList);
                 count = mRemoteCallbackList.beginBroadcast();
                 if (count == 0) {
                     return;
                 }
                 try {
                     for (int i = 0; i < count; i++) {
                         mRemoteCallbackList.getBroadcastItem(i).callBack((int) result);
                     }
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 } finally {
                     mRemoteCallbackList.finishBroadcast();
                 }
             }
         }.start();
     }
        //------------------------------//
       @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.i(TAG, "onStartCommand: ");
         return super.onStartCommand(intent, flags, startId);
     }
    //Binder类
     class LocalBinder extends IRemoteService.Stub {

         @Override
         public void refreshFromNet() throws RemoteException {

         }
   //-----------------添加-----------------------//
         @Override
         public void registerListener(IMyAidlCallBack callBack) throws RemoteException {
             Log.i(TAG, "callBack:" + callBack);
             Log.i(TAG, "mRemoteCallbackList:" + mRemoteCallbackList);
             if (mRemoteCallbackList == null) {
                 Toast.makeText(getApplicationContext(), "mRemoteCallbackList==null", Toast.LENGTH_SHORT).show();
                 Log.i(TAG, "mRemoteCallbackList==null");
                 return;
             }
             mRemoteCallbackList.register(callBack);
         }

         @Override
         public void unRegisterListener(IMyAidlCallBack callBack) throws RemoteException {
             mRemoteCallbackList.unregister(callBack);
         }

         @Override
         public void doInBackGround() throws RemoteException {
             RemoteService.this.doInBackGround();
         }
       //---------------------------------//
     }
    }
   ```

 上面添加的代码中比较关键的两点有：注册和反注册远程回调接口，也就是将回调对象元素加入或移  除`mRemoteCallbackList`这个对象中的一个集合；执行回调，将回调方法放在需要执行的地方，这里模拟耗时操作，放在了线程执行结束的地方，` mRemoteCallbackList.getBroadcastItem(i).callBack((int) result);`

 需要注意的是：`mRemoteCallbackList.finishBroadcast();`这句必须执行，否则有可能会出现状态不合法异常。



- 2.3 使用它，在需要进行耗时操作，并且调用方需要获取耗时操作的结果，再对其进行处理的地方；需要继承`IMyAidlCallBack.aidl`自动生成的 `IMyAidlCallBack.Stub`类，并重写里面的方法。

```java
  try {
              mIRemoteService.registerListener(new IMyAidlCallBack.Stub() {
                  @Override
                  public void callBack(final int result) throws RemoteException {
                      new Handler(Looper.getMainLooper()).post(new Runnable() {
                          @Override
                          public void run() {
                              textView.setText("result：" + result + "");
                          }
                      });
                  }
              });
          } catch (RemoteException e) {
              e.printStackTrace();
          }
```

#### 运行结果：

 ![AIDL](mdSource\AIDL.gif)







