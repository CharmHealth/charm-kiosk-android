package android.util

// Minimal stub to satisfy android.util.Log used in production code when running JVM unit tests.
object Log {
    @JvmStatic
    fun d(tag: String?, msg: String?): Int = 0
}
