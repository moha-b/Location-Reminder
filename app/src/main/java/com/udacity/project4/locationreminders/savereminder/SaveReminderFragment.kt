package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

@SuppressLint("UnspecifiedImmutableFlag")
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var launcherLocation: ActivityResultLauncher<IntentSenderRequest>
    private val geoClient: GeofencingClient by lazy {LocationServices.getGeofencingClient(requireContext())}
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var launcherPermissions: ActivityResultLauncher<Array<String>>
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View{
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        launcherPermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (result.all { res -> res.value!! }) {
                    //granted
                    checkPermissions()
                    Log.d("TAG", "Permission Granted")

                } else {
                    Snackbar.make(
                        binding.saveReminder,
                        R.string.select_poi, Snackbar.LENGTH_LONG
                    ).setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                        }.show()
                }
            }
        launcherLocation = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
            if (res.resultCode == RESULT_OK) {
                geoReminder()
            }
        }

        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value = NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val id = if (_viewModel.reminderId.value != null)_viewModel.reminderId.value
            else UUID.randomUUID().toString()

            reminderDataItem= ReminderDataItem (title, description, location, latitude, longitude, id!!)

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                checkPermissions()
            }
        }
    }

    private fun checkPermissions(){
            if (foregroundPermission() && backgroundPermission()){
                checkLocationSettings()
            } else{
                if (foregroundPermission() && backgroundPermission()){ checkLocationSettings() }
                if(!backgroundPermission()){ requestBackgroundPermission() }
                if(!foregroundPermission()){ requestForegroundPermissions() }
            }
    }

    private fun requestForegroundPermissions() {
        when {
            foregroundPermission() -> { return }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(binding.saveReminder, R.string.select_poi, Snackbar.LENGTH_LONG)
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
            else -> {
                launcherPermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    private fun foregroundPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun backgroundPermission(): Boolean {
        return if (runningQOrLater) {
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else{
            true
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPermission () {
        if(backgroundPermission()) {
            checkLocationSettings()
            return
        }
        if (runningQOrLater) {
            launcherPermissions.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        } else {
            return
        }
    }

    private fun checkLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                geoReminder()
            }
        }
        locationSettingsResponseTask.addOnFailureListener { exp ->
            if (exp is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val request = IntentSenderRequest.Builder(exp.resolution).build()
                    launcherLocation.launch(request)

                } catch (error: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: ${error.message}")
                }
            } else {
                Snackbar.make(binding.saveReminder, R.string.location_required_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                    checkLocationSettings()
                }.show()
            }
        }
    }

    private fun geoReminder() {

        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                _viewModel.remindingLocationRange.value!!.toFloat()
            )
            .setExpirationDuration(NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geoClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Log.e("TAG", geofence.requestId)
                        _viewModel.validateAndSaveReminder(reminderDataItem)
                        /*_viewModel.navigationCommand.value =
                            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())*/

                    }
                    addOnFailureListener {
                        Toast.makeText(requireContext(), R.string.geofences_not_added,
                            Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w("TAG", it.message.toString())
                        }
                    }
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

}




