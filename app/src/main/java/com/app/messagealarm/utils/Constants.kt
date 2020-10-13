package com.app.messagealarm.utils

/**
 * This is a class that contains all the needed constants
 * @author Al Mujahid Khan
 * */
class Constants {
    class Invalid {
        companion object {
            const val INVALID_INTEGER: Int = -1
            const val INVALID_LONG: Long = -1
        }
    }

    class Default {
        companion object {
            const val DEFAULT_STRING: String = ""
            const val DEFAULT_INTEGER: Int = 0
            const val DEFAULT_FLOAT: Float = 0.0f
            const val DEFAULT_LONG: Long = 0
            const val DEFAULT_BOOLEAN: Boolean = false
            const val DEFAULT_LANGUAGE: String = "en"
            const val TRUE_INTEGER: Int = 1
            const val FALSE_INTEGER: Int = 0
            const val SPACE_STRING: String = " "
        }
    }

    class Common {
        companion object {
            const val DEVICE_TYPE_ANDROID = 1
            const val PROPER_BASE_URL = "http://orion.tstaffs.com/"
            const val PATH_SEGMENT_API = "api"
            const val PATH_SEGMENT_SLASH = ""
            const val HTTP = "http"
            const val HTTPS = "https"
            const val IMAGE_STORING_SUFFIX = ".png"
            const val NEW_LINE_CHARACTER = "\n"
            const val IMAGE_STORING_PREFIX = "share_image_"
            const val DEVICE_TYPE = "android"
            const val COMMA = ","
            const val FIRST_BRACKET = "("
            const val API_SECRET_CODE = "EH5m5%\$+3V\$7Ue4j3*Kc5UzA4Mq7TXEt8a!8^AJ#"
            const val ANDROID_HASH_KEY = "Hash Key"
            const val PLAIN_TEXT_MIME_TYPE = "text/plain"
            const val PNG_IMAGE_MIME_TYPE = "image/png"
            const val ALL_IMAGE_MIME_TYPE = "image/*"
            const val JPEG_IMAGE_MIME_TYPE = "image/jpeg"
            const val COMMON_TIME_ZONE = "UTC"
            const val APP_COMMON_DATE_FORMAT: String = "yyyy-MM-dd HH:mm"
            const val APP_PROFILE_DATE_FORMAT: String = "dd/MM/yyyy"
            const val APP_COMMON_ONLY_DATE_FORMAT: String = "yyyy-MM-dd"
            const val APP_COMMON_TIME_FORMAT: String = "hh:mm a"
            const val APP_COMMON_DAY_FORMAT: String = "E"
            const val APP_COMMON_MONTH_FORMAT: String = "MMM"
            const val ALIGNMENT_LEFT: String = "left"
            const val ALIGNMENT_RIGHT: String = "right"
            const val ALIGNMENT_INHERIT: String = "inherit"
            const val ROLE = "Bearer "
            const val TELEPHONE_URI_STARTING = "tel:"
            const val GOOGLE_DOC_TOOLBAR_REMOVING_URL = "javascript:(function() { " +
                    "document.querySelector('[role=\"toolbar\"]').remove();" +
                    "})()"
            const val PREFIX_PDF_LOADING_URL = "https://docs.google.com/gview?embedded=true&url="
            const val BASE_URL_APP_RESOURCES = "file:///android_res/"
            const val HTML_JUSTIFIED_STYLE = "<html>" +
                    "<style type='text/css'>" +
                    "@font-face {" +
                    "font-family: MyFont;" +
                    "src: url('font/regular.ttf')" +
                    "}" +
                    "body {" +
                    "margin: 0;" +
                    "line-height: 1.5;" +
                    "padding: 0;" +
                    "font-family: MyFont;" +
                    "font-size: 14px;" +
                    "text-align: justify;" +
                    "color: #434343" +
                    "}" +
                    "</style>" +
                    "<body>%s" +
                    "</body>" +
                    "</html>"
            const val HTML_SMALL_JUSTIFIED_STYLE = "<html>" +
                    "<style type='text/css'>" +
                    "@font-face {" +
                    "font-family: MyFont;" +
                    "src: url('font/raleway_regular.ttf')" +
                    "}" +
                    "body {" +
                    "margin: 0;" +
                    "line-height: 1.5;" +
                    "padding: 0;" +
                    "font-family: MyFont;" +
                    "font-size: 12px;" +
                    "text-align: justify;" +
                    "color: #FFFFFF" +
                    "}" +
                    "</style>" +
                    "<body>%s" +
                    "</body>" +
                    "</html>"
            const val HTML_MIME_TYPE = "text/html"
            const val HTML_ENCODING = "UTF-8"
            const val PRODUCT_TYPE_BIDS = 2
            const val PRODUCT_TYPE_SOFTWARE = 1
        }
    }

    class File {
        companion object {
            const val PREFIX_IMAGE = "IMG_"
            const val PREFIX_CROPPED_IMAGE = "IMG_CROPPED_"
            const val SUFFIX_IMAGE = ".jpg"
        }
    }

    class PreferenceKeys {
        companion object {
            const val IS_SNOOZED_MODE_ACTIVE = "snozzed"
            const val IS_SERVICE_STOPPED = "service_stopped"
        }
    }

    class IntentKeys {
        companion object {
            const val TITLE = "title"
            const val DESC = "desc"
            const val IMAGE_PATH = "image"
            const val PACKAGE_NAME = "package"
            const val APP_NAME = "app_name"
            const val TONE = "tone"
            const val IS_VIBRATE = "is_vibrate"
        }
    }


    class BundleKeys{
        companion object{
            const val APP = "app"
            const val IS_EDIT_MODE = "is_edit"
            const val APP_NAME = "app_name"
            const val PACKAGE_NAME = "package_name"
            const val BITMAP = "bitmap"
        }
    }


    class LanguageCodes {
        companion object {
            const val ENGLISH = "en"
            const val PORTUGUESE = "pt"
        }
    }

}