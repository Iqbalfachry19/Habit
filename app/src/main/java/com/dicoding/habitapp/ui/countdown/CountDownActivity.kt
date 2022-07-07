package com.dicoding.habitapp.ui.countdown

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class CountDownActivity : AppCompatActivity() {
    private lateinit var workManager: WorkManager
    private lateinit var oneTimeWorkRequest: OneTimeWorkRequest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"
        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this)[CountDownViewModel::class.java]

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this) {
            findViewById<TextView>(R.id.tv_count_down).text = it
        }
        viewModel.eventCountDownFinish.observe(this) {
            updateButtonState(!it)
            val channelName = getString(R.string.notify_channel_name)
            workManager = WorkManager.getInstance(this)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = Data.Builder()
                .putString(NOTIFICATION_CHANNEL_ID, channelName)
                .putInt(HABIT_ID, habit.id)
                .putString(HABIT_TITLE, habit.title)
                .build()

            oneTimeWorkRequest =
                OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                    .setInputData(data)
                    .setConstraints(constraints)
                    .build()
            workManager.enqueue(oneTimeWorkRequest)
            workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                .observe(this) { workInfo ->
                    val status = workInfo.state.name
                    Log.d("statusk", status)
                }
        }


        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            updateButtonState(true)
            viewModel.startTimer()

        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            updateButtonState(false)
            viewModel.resetTimer()
            workManager.cancelWorkById(oneTimeWorkRequest.id)
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}