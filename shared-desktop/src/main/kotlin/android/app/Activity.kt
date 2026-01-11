package android.app

import android.content.Context
import android.view.Window

open class Activity : Context() {
    val window: Window = Window()
}
