package com.paypay.converter

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class ApplicationExtender : MultiDexApplication {
    private var mRequestQueue: RequestQueue? = null

    /**
     * Instantiates a new App analytics extender.
     */
    constructor() {}

    /**
     * Instantiates a new App analytics extender.
     *
     * @param context the context
     */
    constructor(context: Context?) {
        mCtx = context
    }

    /**
     * Sets current activity.
     *
     * @param controlActivity the control activity
     */
    fun setCurrentActivity(controlActivity: AppCompatActivity?) {
        currentActivity = controlActivity as ControlActivity?
    }

    /**
     * FOR THE MULTIDEX
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        // register to be informed of activities starting up
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {

                // new activity created; force its orientation to portrait
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        app = this
    }

    /**
     * Save to shared preferences.
     *
     * @param key   the key
     * @param value the value
     */
    ////
    fun saveToSharedPreferences(key: String?, value: String?) {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Read from shared preferences string.
     *
     * @param key the key
     * @return the string
     */
    fun readFromSharedPreferences(key: String?): String? {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        return sharedPreferences.getString(key, "")
    }

    /**
     * Gets request queue.
     *
     * @return the request queue
     */
    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mCtx!!.applicationContext)
            }
            return mRequestQueue!!
        }

    /**
     * Add to request queue.
     *
     * @param <T>     the type parameter
     * @param request the request
    </T> */
    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }

    companion object {
        /**
         * The constant MY_SOCKET_TIMEOUT_MS.
         */
        const val MY_SOCKET_TIMEOUT_MS = 60000

        /**
         * FOR THE INACTIVE TIMER CHECKER
         */
        private val TIMETAG = ApplicationExtender::class.java.name
        /**
         * Gets app.
         *
         * @return the app
         */
        /**
         * The App.
         */
        var app: ApplicationExtender? = null
        private var mCtx: Context? = null

        /**
         * Get current activity control activity.
         *
         * @return the control activity
         */
        var currentActivity: ControlActivity? = null
            private set

        /**
         * Gets instance.
         *
         * @param context the context
         * @return the instance
         */
        @Synchronized
        fun getInstance(context: Context?): ApplicationExtender? {
            if (app == null) {
                app = ApplicationExtender(context)
            }
            mCtx = app
            return app
        }
    }
}