package android.os

class BatteryManager {
    fun getIntProperty(id: Int): Int = 100

    companion object {
        const val BATTERY_PROPERTY_CAPACITY = 4
    }
}
