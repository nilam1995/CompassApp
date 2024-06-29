package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.lang.Math.cos
import java.lang.Math.sin

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private lateinit var compassImageView: ImageView
    private lateinit var redDot: View
    private lateinit var offButton: Button

    private var dotX: Float = 0f
    private var dotY: Float = 0f
    private var dotVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compassImageView = findViewById(R.id.compassImageView)
        redDot = findViewById(R.id.redDot)
        offButton = findViewById(R.id.offButton)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        compassImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val centerX = compassImageView.width / 2
                val centerY = compassImageView.height / 2

                dotX = event.x - centerX
                dotY = event.y - centerY

                redDot.visibility = View.VISIBLE
                dotVisible = true
                updateRedDotPosition()
            }
            true
        }

        offButton.setOnClickListener {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
            }

            if (gravity != null && geomagnetic != null) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    val rotation = -azimuth

                    compassImageView.rotation = rotation
                    updateRedDotPosition()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    private fun updateRedDotPosition() {
        if (dotVisible) {
            val centerX = compassImageView.width / 2
            val centerY = compassImageView.height / 2

            val angle = Math.toRadians(compassImageView.rotation.toDouble())
            val rotatedX = dotX * Math.cos(angle) - dotY * Math.sin(angle)
            val rotatedY = dotX * Math.sin(angle) + dotY * Math.cos(angle)

            redDot.x = centerX + rotatedX.toFloat() - redDot.width / 2
            redDot.y = centerY + rotatedY.toFloat() - redDot.height / 2
        }
    }
}