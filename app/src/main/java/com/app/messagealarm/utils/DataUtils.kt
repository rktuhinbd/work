package com.app.messagealarm.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import com.app.messagealarm.BaseApplication
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * This is a class that contains utils to work with data
 * @author Al Mujahid Khan
 * */
class DataUtils private constructor() {
    companion object {
        /**
         * This method provides an unique id using UUID
         *
         * @return [String] unique string
         * */
        fun getUniqueId(): String {
            return UUID.randomUUID().toString()
        }

        /**
         * This method provides a random number
         *
         * @param min minimum limit
         * @param max maximum limit
         * @return [Int] random number
         * */
        fun randomInt(min: Int, max: Int): Int {
            return Random().nextInt(max - min + 1) + min
        }

        /**
         * This method returns a local string
         *
         * @param resourceId desired resource id
         * @return desired string
         * */
        fun getString(resourceId: Int): String {
            return getLocalizedString(resourceId)
        }

        /**
         * This method provides a string according to the locale
         *
         * @param resourceId id of the string resource
         * */
        fun getLocalizedString(resourceId: Int): String {
            return BaseApplication.getBaseApplicationContext().resources.getString(resourceId)
        }

        /**
         * This method returns a local integer
         *
         * @param resourceId desired resource id
         * @return desired integer
         * */
        fun getInteger(resourceId: Int): Int {
            return ViewUtils.getResources().getInteger(resourceId)
        }

        /**
         * This method returns a local resource [Uri]
         *
         * @param resourceId desired resource id
         * @return desired [Uri]
         * */
        fun getUriFromResource(resourceId: Int): Uri {
            return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                        + AndroidUtils.getApplicationId() + "/" + resourceId
            )
        }

        /**
         * This method returns a converted title case string
         *
         * @param given given string
         * @return desired [String]
         * */
        fun toTitleCase(given: String): String {
            var isSpace = true
            val builder = StringBuilder(given)
            val len = builder.length

            for (i in 0 until len) {
                val char = builder[i]
                if (isSpace) {
                    if (!Character.isWhitespace(char)) {
                        // Convert to title case and switch out of whitespace mode.
                        builder.setCharAt(i, Character.toTitleCase(char))
                        isSpace = false
                    }
                } else if (Character.isWhitespace(char)) {
                    isSpace = true
                } else {
                    builder.setCharAt(i, Character.toLowerCase(char))
                }
            }

            return builder.toString()
        }

        @SuppressLint("InlinedApi", "LogNotTimber")
        fun getAndroidHashKey() {
            val context = BaseApplication.getBaseApplicationContext()
            try {
                val packageInfo: PackageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    if (AndroidUtils.getCurrentBuildApiVersion() >= Build.VERSION_CODES.P) {
                        PackageManager.GET_SIGNING_CERTIFICATES
                    } else {
                        PackageManager.GET_SIGNATURES
                    }
                )

                var signatures = arrayOf<Signature>()

                if (AndroidUtils.getCurrentBuildApiVersion() >= Build.VERSION_CODES.P) {
                    if (packageInfo.signingInfo != null) {
                        signatures = packageInfo.signingInfo.apkContentsSigners
                    }
                } else {
                    signatures = packageInfo.signatures
                }

                for (signature in signatures) {
                    val messageDigest = MessageDigest.getInstance("SHA")
                    messageDigest.update(signature.toByteArray())
                    val hashKey = String(Base64.encode(messageDigest.digest(), 0))
                    Log.i(Constants.Common.ANDROID_HASH_KEY, hashKey)
                }
            } catch (e: NoSuchAlgorithmException) {
                Timber.e(e)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        /**
         * This method adds a suffix to the number. For example, 1200 will turn to 1.2k
         *
         * @param value number to be converted
         * @return number with suffix
         * */
        fun addSuffixToNumber(value: Long): String {
            // Initialization
            val suffixes: NavigableMap<Long, String> = TreeMap()
            suffixes[1_000L] = "k"
            suffixes[1_000_000L] = "M"
            suffixes[1_000_000_000L] = "G"
            suffixes[1_000_000_000_000L] = "T"
            suffixes[1_000_000_000_000_000L] = "P"
            suffixes[1_000_000_000_000_000_000L] = "E"

            //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
            if (value == Long.MIN_VALUE) return addSuffixToNumber(Long.MIN_VALUE + 1)
            if (value < 0) return "-" + addSuffixToNumber(-value)
            if (value < 1000) return value.toString()

            val entry: Map.Entry<Long, String> = suffixes.floorEntry(value)!!
            val divideBy = entry.key
            val suffix = entry.value

            val truncated = value / (divideBy / 10) //the number part of the output times 10
            val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()

            return if (hasDecimal) {
                (truncated / 10.0).toString() + suffix
            } else {
                (truncated / 10).toString() + suffix
            }
        }
    }
}