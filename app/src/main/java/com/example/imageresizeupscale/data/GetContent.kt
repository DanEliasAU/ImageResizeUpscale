package com.example.imageresizeupscale.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/*
* Custom class for creating new activity using the get content intent
* https://jun-hub.github.io/android/result-api/ ty to this guy
* */
class GetContent : ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: Context, input: String) =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

    override fun getSynchronousResult(context: Context, input: String): SynchronousResult<Uri?>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.data
    }
}