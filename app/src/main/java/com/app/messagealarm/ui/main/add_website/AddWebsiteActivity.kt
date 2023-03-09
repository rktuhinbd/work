package com.app.messagealarm.ui.main.add_website

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.app.messagealarm.R
import com.app.messagealarm.model.InstalledApps
import com.app.messagealarm.ui.main.add_options.AddApplicationOption
import com.app.messagealarm.utils.Constants
import com.app.messagealarm.utils.SharedPrefUtils
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.activity_add_application.*
import kotlinx.android.synthetic.main.activity_add_application.spinner_filter
import kotlinx.android.synthetic.main.activity_add_website.*
import java.io.Serializable
import java.lang.NullPointerException

class AddWebsiteActivity : AppCompatActivity() {

    val bottomSheetModel = AddApplicationOption()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_website)
        toolBarSetup()
        setupSpinner()
        filterListener()
        val app = InstalledApps("WhatsApp", "com.whatsapp", "1.2.5", null)
        btn_configure?.setOnClickListener {
            if (!bottomSheetModel.isAdded) {
                val bundle = Bundle()
                bundle.putBoolean(Constants.BundleKeys.IS_EDIT_MODE, false)
                bundle.putSerializable(Constants.BundleKeys.APP, app as Serializable)
                bottomSheetModel.arguments = bundle
                bottomSheetModel.isCancelable = false
                bottomSheetModel.show(supportFragmentManager, "OPTIONS")
            }
        }
    }


    @SuppressLint("ResourceType")
    private fun setupSpinner(){
        val spinnerList = ArrayList<String>()
        spinnerList.add("WebSocket")
        spinnerList.add("WebHook")
        spinnerList.add("PushNotification")
        spinnerList.add("REST API")
        spinnerList.add("MQTT")


        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.spinner_item, spinnerList)
        spinner_filter?.adapter = adapter
    }

    private fun filterListener() {
        spinner_filter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }
        }
    }

    private fun toolBarSetup() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.txt_add_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(SharedPrefUtils.readBoolean(Constants.PreferenceKeys.IS_DARK_MODE)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white, theme))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white, theme))
            }else{
                toolbar.navigationIcon?.setTint(resources.getColor(R.color.color_white))
                toolbar.collapseIcon?.setTint(resources.getColor(R.color.color_white))
            }
        }
    }



}