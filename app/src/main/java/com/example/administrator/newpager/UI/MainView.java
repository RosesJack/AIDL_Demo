package com.example.administrator.newpager.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.newpager.R;
import com.example.administrator.newpager.service.IMyAidlCallBack;
import com.example.administrator.newpager.service.IRemoteService;
import com.example.administrator.newpager.service.RemoteService;

import static android.R.attr.id;
import static android.R.attr.start;

/**
 * auther：wzy
 * date：2017/1/8 13 :57
 * desc:
 */

public class MainView extends FrameLayout {
    private Context mContext;
    private View mainViewChild;
    private View button;
    private TextView textView;

    public MainView(Context context) {
        this(context, null);
    }

    public MainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMainView();
    }

    private void initMainView() {
        mContext = getContext();
        mainViewChild = LayoutInflater.from(mContext).inflate(R.layout.layout_main_view, null);
        this.addView(mainViewChild);
        initMainViewChild();
        bindService();
    }

    private void initMainViewChild() {
        if (mainViewChild == null) {
            return;
        }
        button = mainViewChild.findViewById(R.id.button);
        textView = (TextView) mainViewChild.findViewById(R.id.textView);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonEvent();
            }
        });
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mIRemoteService.doInBackGround();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showTextView() {
        if (mIRemoteService == null) {
            Toast.makeText(mContext, "mIRemoteService == null", Toast.LENGTH_SHORT).show();
            return;
        }
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
    }

    private void buttonEvent() {
        Toast.makeText(mContext, "点击了按钮", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unBindService();
        Toast.makeText(mContext, "onDetachedFromWindow", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bindService();
        Toast.makeText(mContext, "onAttachedToWindow", Toast.LENGTH_SHORT).show();
    }

    private void unBindService() {
        mContext.unbindService(mServiceConnection);
    }

    private void bindService() {
//        Intent intent = new Intent("com.example.administrator.newpager.REMOTESERVICE");
//        intent.setComponent
//                    (new ComponentName("com.example.administrator.newpager.service",
//                        "com.example.administrator.newpager.service.RemoteService"));
        Intent intent = new Intent(mContext, RemoteService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private IRemoteService mIRemoteService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIRemoteService = IRemoteService.Stub.asInterface(service);
            showTextView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
