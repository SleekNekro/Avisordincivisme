package com.example.avisordincivisme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.avisordincivisme.databinding.ActivityMainBinding
import com.example.avisordincivisme.ui.SharedViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var singInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ).build()

        val navController: NavController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navView, navController)

        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedViewModel.setFusedLocationClient(mFusedLocationClient)

        sharedViewModel.getCheckPermission().observe(this, Observer { s ->
            checkPermission()
        })

        locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted) {
                sharedViewModel.startTrackingLocation(false)
            } else if (coarseLocationGranted) {
                sharedViewModel.startTrackingLocation(false)
            } else {
                Toast.makeText(this, "No concedeixen permisos", Toast.LENGTH_SHORT).show()
            }
        }
         singInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract(),
            {result ->
                if(result.resultCode ==  RESULT_OK){
                    val user:FirebaseUser = FirebaseAuth.getInstance().currentUser as FirebaseUser
                    sharedViewModel.setUser(user)
                }
            });
    }
    override fun onStart() {
        super.onStart()

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()
        Log.e("XXXX", auth.currentUser.toString())
        if (auth.currentUser == null) {
            val signInIntent: Intent =
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(
                        listOf(
                            EmailBuilder().build(),
                            GoogleBuilder().build()
                        )
                    )
                    .build()
            singInLauncher.launch(signInIntent)
        } else {
            sharedViewModel.setUser(auth.currentUser!!)
        }

    }

    fun checkPermission() {
        Log.d("PERMISSIONS", "Check permisssions")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            Log.d("PERMISSIONS", "Request permisssions")
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            sharedViewModel.startTrackingLocation(false)
        }
    }
}
