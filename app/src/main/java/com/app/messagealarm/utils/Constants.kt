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

    class InputData{
        companion object{
            const val APP_SIZE = "app_size"
            const val LANG_SIZE = "lang_size"
            const val CONSTRAIN_SIZE = "constrain_size"
        }
    }

    class Purchase{
        companion object{
            const val PRODUCT_ID = "pro_feature"
            const val SUBSCRIPTION_ID = "pro_subs"
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
            const val TYPE_MISSED = 1
            const val TYPE_ALARM = 2
            const val MUTE_TIMER = "mute_timer"
            const val SYNC = "sync"
            const val WORK_SYNC = "work_sync"
            const val MANUAL = "Until I unmute it"
            const val NEVER = "never"
            const val MIN_SOUND_LEVEL: Int = 96
        }
    }
    class ACTION{
        companion object{
            const val ACTION_PURCHASE_FROM_MAIN = 12
            const val ACTION_SAVE_APPLICATION = 64
            const val ACTION_PURCHASE_FROM_ADD = 13
            const val ACTION_PURCHASE_FROM_SETTING = 14
            const val ACTION_UN_MUTE = "un_mute"
            const val SYNC = "SYNC"
            const val UPDATE = "UPDATE"
            const val BUY = "BUY"
            const val WEB_URL = "url_web"
            const val OPEN_SERVICE = "OPEN_SERVICE"
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
            const val IS_AUTO_STARTED = "is_auto_started"
            const val IS_BATTERY_RESTRICTED = "battery_restriction"
            const val IS_GETTING_STARTED_PAGE_VISITED = "visited_getting_started"
            const val IS_FIRST_TIME_ALARM_PLAYED = "alarm_played"
            const val ALARM_COUNT = "alarm_count"
            const val MAIN_SCREEN_OPENED = "main_screen_opened"
            const val SHOW_PRO_DIALOG_COUNT = "shown_pro_count"
            const val SHOW_SOUND_WARNING_COUNT = "sound_warning_count"
            const val LAST_APP_NAME = "last_app_name"
            const val LAST_SENDER_NAME = "last_sender_name"
            const val LAST_APP_ICON_NAME = "last_app_icon_name"
            const val IS_REVIEW_SCREEN_SHOWN = "review_asked"
            const val IS_PURCHASED = "is_purchased"
            const val FIREBASE_TOKEN = "firebase_token"
            const val IS_TUTORIAL_SHOW = "is_tutorial"
            const val CONSTRAIN_COUNT = "constrain_count"
            const val FIRST_APP_SYNC_FINISHED = "first_app_sync_finished"
            const val UPDATED_VERSION = "updated_version"
            const val IS_NOTIFICATION_SWIPED = "is_swiped"
            const val IS_ACTIVITY_STARTED = "is_activity_started"
            const val IS_STOPPED = "is_stopped"
            const val IS_HEROKU_TOKEN_SYNCED = "is_heroku_token_synced"
            const val IS_FIREBASE_TOKEN_SYNCED_2_0_2 = "is_firebase_token_synced"
            const val IS_SERVICE_STOPPED = "service_stopped"
            const val IS_VIDEO_SHOWED = "is_video_showed"
            const val IS_DARK_MODE = "is_dark"
            const val IS_LANG_WARNING_SHOWED = "is_lang_warning"
            const val IS_MUTED = "is_muted"
            const val MUTE_TIME = "mute"
            const val THEME = "theme"
            const val IS_TERMS_ACCEPTED = "terms_accepted"
            const val GOT_ONE_MESSAGE = "got_one_message"
            const val COUNTRY_CODE = "country_code"
            const val SOUND_LEVEL = "sound_level"
            const val DEFAULT_SOUND_LEVEL = "default_sound"
            const val COUNTRY = "country"
            const val CURRENCY_CODE = "currency_code"
            const val CURRENCY_SYMBOL = "currency_symbol"
            const val IS_USER_INFO_SAVED = "is_user_info_saved"
            const val IS_DB_ROLLED_BACK = "is_db_rollback"
            const val IS_TOKEN_UPDATED = "is_token_updated"
            const val IS_FREELANCER = "is_user_freelancer"

        }
    }

    class IntentKeys {
        companion object {
            const val IS_PURCHASED = "is_purchased"
            const val TITLE = "title"
            const val DESC = "desc"
            const val IMAGE_PATH = "image"
            const val PACKAGE_NAME = "package"
            const val TYPE_ALARM = "type_alarm"
            const val JUST_CANCEL = "just_cancel"
            const val PUSH_TITLE = "push_title"
            const val PUSH_DESC = "push_desc"
            const val PUSH_IMAGE = "push_image"
            const val PUSH_URL = "url_push"
            const val IS_PUSH_URL = "push_url"
            const val APP_NAME = "app_name"
            const val TONE = "tone"
            const val SOUND_LEVEL = "sound_level"
            const val TOTAL_APP = "total_app"
            const val NUMBER_OF_PLAY = "number_of_play"
            const val IS_VIBRATE = "is_vibrate"
            const val IS_FLASH_LIGHT = "is_flash_light"
            const val IS_JUST_VIBRATE = "is_just_vibrate"
            const val IS_BUY_PRO = "is_buy_pro"
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


    class API{
        companion object{
            const val SYNC = "sync"
            const val REGISTER_TOKEN = "token/"
            const val UNKNOWN_APP = "unknownapp/"
            const val VERIFY_PURCHASE = "verify_purchase/"
            const val LATEST_VERSION = "latest_version/"
            const val UPDATE_TOKEN = "update_token/"
        }

        class Body{
            companion object{
                const val APP_SIZE = "app_size"
                const val LANG_SIZE = "lang_size"
                const val CONSTRAIN_SIZE = "constrain_size"
                const val TOKEN = "token"
                const val LANG_CODE = "lang_code"
                const val APP_NAME = "app_name"
                const val PACKAGE_NAME = "app_package_name"
                const val RECEIPT = "receipt"
                const val USER_TOKEN = "user_token"
                const val SIGNATURE = "signature"
                const val COUNTRY = "country"
                const val IS_PAID = "is_paid"
                const val UUID = "uuid"
                const val TIME_ZONE = "time_zone"
                const val ALARM_COUNT = "alarm_count"
            }
        }
        class ResponseFormat{
            companion object{
                const val JSON_RESPONSE = "Accept:application/json"
            }
        }
    }


    class APP{
        companion object{
            const val IMO_PACKAGE = "com.imo.android.imoim"
            const val XIAOMI_PACKAGE = "com.android.mms"
        }
    }



    class LanguageCodes {
        companion object {
            const val ENGLISH = "en"
            const val PORTUGUESE = "pt"
        }
    }

}