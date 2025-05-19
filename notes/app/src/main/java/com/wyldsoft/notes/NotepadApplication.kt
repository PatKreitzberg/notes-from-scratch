package com.wyldsoft.notes

import android.app.Application
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import com.onyx.android.sdk.rx.RxManager
import com.onyx.android.sdk.utils.ResManager
import org.lsposed.hiddenapibypass.HiddenApiBypass

class NotepadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ResManager.init(this)
        RxManager.Builder.initAppContext(this)

        // Check for hidden API bypass (as seen in Onyx demo applications)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }


    }
}