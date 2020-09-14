package com.mydemo.colorfilldemo

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.WaveDrawable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mWaveDrawable: WaveDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_black.setOnClickListener {
            fillColorAnimation(R.color.color_black)
        }
        button_gray.setOnClickListener {
            fillColorAnimation(R.color.color_gray)
        }
        button_orange.setOnClickListener {
            fillColorAnimation(R.color.color_orange)
        }
        button_brown.setOnClickListener {
            fillColorAnimation(R.color.color_brown)
        }
        button_green.setOnClickListener {
            fillColorAnimation(R.color.color_green)
        }
        button_pink.setOnClickListener {
            fillColorAnimation(R.color.color_pink)
        }
        button_red.setOnClickListener {
            fillColorAnimation(R.color.color_red)
        }
        button_purple.setOnClickListener {
            fillColorAnimation(R.color.color_purple)
        }
    }

    private fun fillColorAnimation(colorId:Int) {
       // mWaveDrawable = WaveDrawable(this,ContextCompat.getDrawable(this,R.drawable.ic_pomegranate_line)!!, colorId)

        mWaveDrawable = if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            WaveDrawable(this,ResourcesCompat.getDrawable(resources,R.drawable.ic_pomegranate_color,theme)!!, colorId)
        }else{
            WaveDrawable(this,resources.getDrawable(R.drawable.ic_pomegranate_line), colorId)
        }
        mWaveDrawable.setIndeterminate(true)
        mWaveDrawable.setWaveAmplitude(0)
        main_image.setImageDrawable(mWaveDrawable)
       }
}