package com.app.messagealarm.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.app.messagealarm.BaseApplication
import java.util.*

class TTSUtils {

    companion object{

        var sentence = ""
        private var textToSpeech: TextToSpeech? = null

        private val textToSpeechListener = TextToSpeech.OnInitListener {
            if(it == TextToSpeech.SUCCESS){
                textToSpeech?.setSpeechRate(0.6f)
                textToSpeech?.setPitch(0.6f)
                textToSpeech?.voice = Voice(null, Locale.getDefault(), Voice.QUALITY_VERY_HIGH
                    , Voice.LATENCY_VERY_LOW, false, null)
                val result: Int = textToSpeech!!.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTS", "Language is not supported")
                } else {
                    speakOut(sentence)
                    textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(p0: String?) {

                        }
                        override fun onDone(p0: String?) {
                            if (textToSpeech != null) {
                                textToSpeech!!.stop()
                                textToSpeech!!.shutdown()
                            }
                        }

                        override fun onError(p0: String?) {

                        }
                    })
                }
            }else{
                Log.e("TTS", "Initilization Failed")
            }
        }

        private fun speakOut(textToSpeak:String){
            textToSpeech?.speak(textToSpeak,TextToSpeech.QUEUE_FLUSH,null,null)
        }

        fun speak(textToSpeak:String){
            sentence = textToSpeak
            textToSpeech = TextToSpeech(BaseApplication.getBaseApplicationContext(), textToSpeechListener)
        }

        fun stopSpeak(){
            // Don't forget to shutdown!
            if (textToSpeech != null) {
                textToSpeech!!.stop()
                textToSpeech!!.shutdown()
            }
        }
    }

    }
