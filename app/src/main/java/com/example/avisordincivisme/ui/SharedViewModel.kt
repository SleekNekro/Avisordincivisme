package com.example.avisordincivisme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val currentAddress = MutableLiveData<String>()
    private val checkPermission = MutableLiveData<String>()
    private val buttonText = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()
    private var mTrackingLocation = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val currentLatLng:MutableLiveData<LatLng> = MutableLiveData();

    fun getCurrentLatLng():MutableLiveData<LatLng>  {
        return currentLatLng;
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                fetchAddress(location)
            }
        }
    }

    fun setFusedLocationClient(client: FusedLocationProviderClient) {
        mFusedLocationClient = client
    }

    fun getCurrentAddress(): LiveData<String> = currentAddress

    fun getButtonText(): MutableLiveData<String> = buttonText

    fun getProgressBar(): MutableLiveData<Boolean> = progressBar

    fun getCheckPermission(): LiveData<String> = checkPermission

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()
    }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(needsChecking = true)
        } else {
            stopTrackingLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("check")
        } else {
            mFusedLocationClient?.requestLocationUpdates(
                getLocationRequest(),
                mLocationCallback,
                Looper.getMainLooper()
            )
            currentAddress.postValue("Carregant...")
            progressBar.postValue(true)
            mTrackingLocation = true
            buttonText.value = "Aturar el seguiment de la ubicació"
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.value = "Comença a seguir la ubicació"
        }
    }

    private fun fetchAddress(location: Location) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var resultMessage = ""
            try {
                val latlng = LatLng(location.getLatitude(), location.getLongitude());
                currentLatLng.postValue(latlng);
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses.isNullOrEmpty()) {
                    resultMessage = "No s'ha trobat cap adreça"
                    Log.e("INCIVISME", resultMessage)
                } else {
                    val address = addresses[0]
                    val addressParts = mutableListOf<String>()
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                    resultMessage = TextUtils.join("\n", addressParts)
                }
            } catch (ioException: IOException) {
                resultMessage = "Servei no disponible"
                Log.e("INCIVISME", resultMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                resultMessage = "Coordenades no vàlides"
                Log.e(
                    "INCIVISME",
                    "$resultMessage. Latitude = ${location.latitude}, Longitude = ${location.longitude}",
                    illegalArgumentException
                )
            }

            val finalMessage = resultMessage
            handler.post {
                if (mTrackingLocation) {
                    currentAddress.postValue("Direcció: $finalMessage")
                }
            }
        }
    }
    private val user:MutableLiveData<FirebaseUser> = MutableLiveData()
    fun getUser():LiveData<FirebaseUser>{
        return user
    }
    fun setUser(passedUser:FirebaseUser){
        user.postValue(passedUser)
    }

}
