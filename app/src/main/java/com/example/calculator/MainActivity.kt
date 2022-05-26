package com.example.calculator

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.calculator.Constants.GlobalFunctions
import com.example.calculator.Constants.OnSwipeTouchListener
import com.example.calculator.ViewModels.MainViewModel
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private val mainScreen by lazy { findViewById<ConstraintLayout>(R.id.main_layout)}
    private val resultTitle by lazy { findViewById<TextView>(R.id.result_title)}
    private val numberTitle by lazy { findViewById<TextView>(R.id.number_title)}
    private val symbolTitle by lazy { findViewById<TextView>(R.id.symbol_title)}
    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]}

    private var sensorManager : SensorManager? = null
    private var accelerometer : Sensor? = null
    private var vibrator : Vibrator?=null

    private var accelerationCurrentValue = 0.0
    private var accelerationPreviousValue = 0.0

    private var sensorListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            val x = p0!!.values[0]
            val y = p0.values[1]
            val z = p0.values[2]
            //sqrt(x*x + y*y + z*z).toDouble()
            accelerationCurrentValue = x.toDouble()
            val accelerationDifference = abs(accelerationCurrentValue - accelerationPreviousValue)
            accelerationPreviousValue = accelerationCurrentValue

            if(accelerationDifference.toInt() > 15) {
                viewModel.setShacked(true)
            }
        }
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    @SuppressLint("ClickableViewAccessibility", "ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        //variables initialization
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mainScreen.setOnTouchListener(object: OnSwipeTouchListener(this@MainActivity) {
            override fun onPress() {
                super.onPress()
                viewModel.setSymbol(".")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200,VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                  vibrator?.vibrate(200)
                }
            }

            override fun onLongPress() {
                super.onLongPress()
                viewModel.setSymbol("-")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(350,VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator?.vibrate(350)
                }
            }

            override fun onSwipeTop() {
                super.onSwipeTop()
                val result= viewModel.getResult().value.toString()
                if(result.isNotEmpty() && GlobalFunctions.checkNotOperation(result.last())) {
                    viewModel.setResult("/")
                    swipesVibrations()
                }
            }

            override fun onSwipeBottom() {
                super.onSwipeBottom()
                val result= viewModel.getResult().value.toString()
                if(result.isNotEmpty() && GlobalFunctions.checkNotOperation(result.last())) {
                    viewModel.setResult("*")
                    swipesVibrations()
                }
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                val result= viewModel.getResult().value.toString()
                if(result.isNotEmpty() && GlobalFunctions.checkNotOperation(result.last())) {
                    viewModel.setResult("-")
                    swipesVibrations()
                }
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                val result= viewModel.getResult().value.toString()
                if(result.isNotEmpty() && GlobalFunctions.checkNotOperation(result.last())) {
                    viewModel.setResult("+")
                    swipesVibrations()
                }
            }
            /*
            override fun onDoubleTap() {
                super.onDoubleTap()
                viewModel.clearSymbolAndNumber()
            }

             */

            override fun onTwoFingerTap() {
                super.onTwoFingerTap()
                val pattern = longArrayOf(0,200,100,200)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern,-1))
                } else {
                    vibrator?.vibrate(pattern,-1)
                }
                viewModel.setResult(viewModel.getNumber().value.toString())
                viewModel.clearSymbolAndNumber()
            }

            override fun onTwoLongFingerTap() {
                super.onTwoLongFingerTap()
                val pattern = longArrayOf(0,200,100,200)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern,-1))
                } else {
                    vibrator?.vibrate(pattern,-1)
                }
                viewModel.clearResult()
                viewModel.clearSymbolAndNumber()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(sensorListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(1000)
        }

        viewModel.getSymbol().observe(this, Observer {
            symbolTitle.text = it
            viewModel.setNumber(GlobalFunctions.getNumber(it))
        })
        viewModel.getResult().observe(this, Observer{
            resultTitle.text = it
        })
        viewModel.getNumber().observe(this, Observer {
            numberTitle.text = it
        })
        viewModel.getShaked().observe(this, Observer { it ->
            val expression = viewModel.getResult().value.toString()
            var isNumeric = true
            if(it && GlobalFunctions.checkNotOperation(expression.last())) {
                val result = GlobalFunctions.calculateEquation(expression)
                viewModel.setFinalResult(result)
                spellResult()
            }
            viewModel.setShacked(false)
        })
    }

    override fun onPause() {
        vibrator?.cancel()
        sensorManager?.unregisterListener(sensorListener)
        super.onPause()
    }

    private fun spellResult() {
        val vibrationPattern = ArrayList<Long>()
        vibrationPattern.add(0)
        vibrationPattern.add(500)
        vibrationPattern.add(800)
        var result = viewModel.getFinalResult()
        if (result.first() == '-') {
            vibrationPattern.add(350)
            vibrationPattern.add(750)
            result = result.drop(1)

        }

        for(i in result.indices) {
            val symbol = GlobalFunctions.getSymbol(result[i].toString())
            for (j in symbol!!.indices) {
                if(symbol[j].toString() == ".") {
                    vibrationPattern.add(200)
                    vibrationPattern.add(500)
                }
                else if(symbol[j].toString() == "-") {
                    vibrationPattern.add(350)
                    vibrationPattern.add(500)
                }
            }
            vibrationPattern[vibrationPattern.lastIndex] = 800
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern.toLongArray(),-1))
        } else {
            vibrator?.vibrate(vibrationPattern.toLongArray(),0)
        }
    }

    fun swipesVibrations() {
        val pattern = longArrayOf(0,90,100,90)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern,-1))
        } else {
            vibrator?.vibrate(pattern,-1)
        }
    }
}