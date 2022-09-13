package com.udacity.project4.locationreminders

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    val viewModel: DescriptionViewModel by inject()

    companion object {

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra("ReminderDataItem", reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)

        binding.remind = intent.getSerializableExtra("ReminderDataItem") as ReminderDataItem?

        binding.edit.setOnClickListener{
            val ent = Intent(this, RemindersActivity::class.java)
            ent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            ent.putExtra("Id", binding.remind!!)
            startActivity(ent)
        }

        binding.delete.setOnClickListener{
            viewModel.deleteReminder(binding.remind!!)
            finish()
        }
    }
}

class DescriptionViewModel(private val dataSource: ReminderDataSource, application: Application)
    : AndroidViewModel(application) {

    fun deleteReminder(reminderData: ReminderDataItem) {
        viewModelScope.launch {
            dataSource.delete(reminderData.id)
        }
    }

}