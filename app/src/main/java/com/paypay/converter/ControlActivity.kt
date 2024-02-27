/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter

import android.app.Activity
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import android.graphics.Color


open class ControlActivity : AppCompatActivity() {

    /**
     * The Os bold.
     */
    protected var osBold: Typeface? = null

    /**
     * The Os semi bold.
     */
    protected var osSemiBold: Typeface? = null

    /**
     * The Os extra bold.
     */
    protected var osExtraBold: Typeface? = null

    /**
     * The Os regular.
     */
    protected var osRegular: Typeface? = null

    /**
     * Gets reference to global Application
     *
     * @return must always be type of AppAnalyticsExtender! See AndroidManifest.xml
     */
    val app: ApplicationExtender
        get() = this.application as ApplicationExtender

    override fun onStart() {
        super.onStart()
        app.setCurrentActivity(this)
        Log.d(TAG, "Activity has started $this")
        //getLocationManager();
        isActivityRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityRunning = false
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        osBold = ResourcesCompat.getFont(this, R.font.os_bold)
        osSemiBold = ResourcesCompat.getFont(this, R.font.os_semibold)
        osExtraBold = ResourcesCompat.getFont(this, R.font.os_extrabold)
        osRegular = ResourcesCompat.getFont(this, R.font.os_regular)
    }

    public override fun onResume() {
        super.onResume()
    }

    /**
     * Stores activity data in the Bundle.
     */
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)


        /*
        Binder.java Crash Error
        This exception occurs when too much data is transferred via Parcels concurrently.
        The underlying Binder transaction buffer has a limited fixed size, currently 1MB, which is shared by all transactions in progress for the process.
        Consequently this exception can be thrown when there are many transactions in progress even when most of the individual transactions are of moderate size.
        For example, `intent.putExtra(key, new LargeParcelableObject())`
        where the size of the `LargeParcelableObject` exceeds 1MB will return in this exception. This is often seen when transferring bitmaps between Activities,
        or when saving a large amount of state between Activity configuration changes. For more information, check out the resources below.
        */
        savedInstanceState.clear()
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    fun showSnackbar(
        mainTextStringId: Int, actionStringId: Int,
        listener: View.OnClickListener?
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(actionStringId), listener).show()
    }

    companion object {
        /**
         * The Tag.
         */
        val TAG = ControlActivity::class.java.simpleName
        /**
         * The Is activity running.
         */
        var isActivityRunning = false

        /**
         * Gets instance.
         *
         * @return the instance
         */
        var instance: ControlActivity? = null
            get() {
                if (field == null) {
                    field = ControlActivity()
                }
                return field
            }
            private set

        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val win = activity.window
            val winParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }
}