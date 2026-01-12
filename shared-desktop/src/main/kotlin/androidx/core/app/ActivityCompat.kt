package androidx.core.app

import android.content.Context
import android.content.pm.PackageManager

object ActivityCompat {
    fun checkSelfPermission(context: Context, permission: String): Int {
        return PackageManager.PERMISSION_GRANTED
    }
}
