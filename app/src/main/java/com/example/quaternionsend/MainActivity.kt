package com.example.quaternionsend

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var rotationSensor: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == rotationSensor) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            // Calculate the quaternion
            val quaternion = FloatArray(4)
            SensorManager.getQuaternionFromVector(quaternion, event.values)

            // Send the UDP message
            val deviceId = "1" // Replace with your own device ID
            val message = "$deviceId,${quaternion[0]},${quaternion[1]},${quaternion[2]},${quaternion[3]}"
            UDPSender().execute(message, "172.16.0.162", "8000") // Replace with your own IP address and port number
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private class UDPSender : AsyncTask<String, Void, Void>() {

        private var socket: DatagramSocket? = null

        override fun doInBackground(vararg params: String?): Void? {
            try {
                val message = params[0]
                val ipAddress = params[1]
                val port = Integer.parseInt(params[2])
                socket = DatagramSocket()
                val address = InetAddress.getByName(ipAddress)
                val data = message!!.toByteArray()
                val packet = DatagramPacket(data, data.size, address, port)
                socket!!.send(packet)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                socket?.close()
            }
            return null
        }
    }
}