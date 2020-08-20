package com.winotech.cicerone.controller;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;


/*
Classe di servizio avviata nel momento in cui l'app viene avviata
 */
public class AppController extends Application implements Serializable {

    public static final String TAG = AppController.class.getSimpleName();

    transient private RequestQueue m_request_queue;

    private static AppController m_instance ;

    @Override
    public void onCreate () {

        super.onCreate();
        m_instance = this;

        m_request_queue = Volley.newRequestQueue(this);
    }

    public static synchronized AppController get_instance () {

        return m_instance;
    }

    public RequestQueue get_request_queue () {

        if (m_request_queue == null)
            m_request_queue = Volley.newRequestQueue(getApplicationContext());

        return m_request_queue;
    }

    public <T> void add_to_request_queue (Request<T> request, String tag) {

        Log.d("req ", request.toString());
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        get_request_queue().add(request);
    }

    public <T> void add_to_request_queue (Request<T> request) {

        request.setTag(TAG);
        get_request_queue().add(request);
    }

    public void cancel_pending_requests (Object tag) {

        if (m_request_queue != null)
            m_request_queue.cancelAll(tag);
    }


}
