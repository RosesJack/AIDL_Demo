    // IRemoteService.aidl
    package com.example.administrator.newpager.service;
    import com.example.administrator.newpager.service.IMyAidlCallBack;
    // Declare any non-default types here with import statements

    interface IRemoteService  {
        void refreshFromNet();
        void registerListener(IMyAidlCallBack callBack);
        void unRegisterListener(IMyAidlCallBack callBack);
        void doInBackGround();
    }
