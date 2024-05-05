package com.example.timerlifecycle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timerlifecycle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var handler = Handler(Looper.getMainLooper())

    private var timerValue = 10
    private var timeCounter = 10
    private var timerIsActive = false

    private lateinit var timerThread: Thread
    private var timerThreadStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            timerValue = savedInstanceState.getInt("timerValue")
            timerIsActive = savedInstanceState.getBoolean("timerIsActive")
            Log.d("msg", "onCreate: после изменения $timerValue")
            timeCounter = timerValue
            handler.post {
                updateTimer(binding)
                if (timerIsActive) startTimer(binding)
            }
        }

        handler.post {
            binding.slider.addOnChangeListener { _, value, _ ->
                timerValue = value.toInt()
                timeCounter = timerValue
                updateUI(binding)
            }

            binding.buttonStart.setOnClickListener {
                startTimer(binding)
            }
        }
    }

    private fun updateUI(binding: ActivityMainBinding) {
        when (timerIsActive) {
            true -> {
                binding.slider.isEnabled = false
                binding.buttonStart.visibility = View.GONE
                binding.buttonStop.visibility = View.VISIBLE
            }

            false -> {
                binding.slider.isEnabled = true
                binding.buttonStop.visibility = View.GONE
                binding.buttonStart.visibility = View.VISIBLE
            }
        }
        binding.progressBar.max = binding.slider.value.toInt()
        binding.progressBar.progress = timeCounter
        binding.timer.text = timerValue.toString()
    }

    private fun updateTimer(binding: ActivityMainBinding) {
        binding.timer.text = timeCounter.toString()
        binding.progressBar.progress = timeCounter
    }

    private fun startTimer(binding: ActivityMainBinding) {
        timerThread = Thread {
            while (!timerThreadStop && timeCounter > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                binding.buttonStop.setOnClickListener {
                    timerIsActive = false
                    stopTimerThread()
                }

                timeCounter--

                handler.post {
                    updateTimer(binding)
                }
            }
            if (timeCounter == 0) timerIsActive = false
            handler.post { finishTimer(binding) }
        }

        timerIsActive = true
        handler.post { updateUI(binding) }
        timerThread.start()
    }

    private fun finishTimer(binding: ActivityMainBinding) {
        if (!timerIsActive) {
            Toast.makeText(this, "Timer Task Finished", Toast.LENGTH_SHORT).show()
        }
        timerValue = binding.slider.value.toInt()
        timeCounter = timerValue
        timerThreadStop = false
        handler.post { updateUI(binding) }
    }

    private fun stopTimerThread() {
        timerThreadStop = true
        timerThread.interrupt()
    }

    override fun onPause() {
        super.onPause()
        try {
            stopTimerThread()
        } catch (ex: Exception) {
            Log.d("msg", "onPause: $ex")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("timerValue", timeCounter)
        outState.putBoolean("timerIsActive", timerIsActive)
        super.onSaveInstanceState(outState)
    }
}