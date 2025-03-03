package com.example.avisordincivisme.ui.home

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import com.example.avisordincivisme.ui.Incidente
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.avisordincivisme.databinding.FragmentHomeBinding
import com.example.avisordincivisme.ui.SharedViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var authUser: FirebaseUser

    private var mCurrentPhotoPath: String? = null
    private var photoURI: Uri? = null
    private val REQUEST_TAKE_PHOTO = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeViewModel = ViewModelProvider(this).get(
            HomeViewModel::class.java
        )

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(
            SharedViewModel::class.java
        )

        sharedViewModel.getCurrentAddress()
            .observe(viewLifecycleOwner, Observer<String> { address: String? ->
                binding!!.txtDireccio.setText(
                    String.format(
                        "Direcció: %1\$s \n Hora: %2\$tr",
                        address,
                        System.currentTimeMillis()
                    )
                )
            })

        sharedViewModel.getCurrentLatLng()
            .observe(viewLifecycleOwner, Observer<LatLng> { latlng: LatLng? ->
                binding!!.txtLatitud.setText(latlng?.latitude.toString())
                binding!!.txtLongitud.setText(latlng?.longitude.toString())

            })

        sharedViewModel.getProgressBar().observe(viewLifecycleOwner) { visible ->
            binding!!.loading.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }

        sharedViewModel.switchTrackingLocation()

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }



        binding!!.imagenBtn.setOnClickListener({ boton ->
            Log.d("XXX","Pulsado")
            dispatchTakePictureIntent()

        })

        binding!!.buttonNotificar.setOnClickListener({ button ->
            val incidente = Incidente();
            incidente.direccio = binding!!.txtDireccio.getText().toString()
            incidente.lalitud = binding!!.txtLatitud.getText().toString()
            incidente.longitud = binding!!.txtLongitud.getText().toString()
            incidente.problema = binding!!.txtDescripcio.getText().toString()
            incidente.carles = "Hola Carles"


            val base: DatabaseReference = FirebaseDatabase.getInstance(
            ).getReference();

            val users: DatabaseReference = base.child("users");
            val uid: DatabaseReference = users.child(authUser.getUid());
            val incidencies: DatabaseReference = uid.child("incidencies");

            val reference: DatabaseReference = incidencies.push();
            reference.setValue(incidente);
        });


        return root
    }



@Throws(IOException::class)
private fun createImageFile(): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    val image = File.createTempFile(imageFileName, ".jpg", storageDir)
    mCurrentPhotoPath = image.absolutePath
    return image
}
private fun dispatchTakePictureIntent() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (context?.packageManager?.let { takePictureIntent.resolveActivity(it) } != null) {
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (ex: IOException) {
        }

        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.example.android.fileprovider",
                photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }
}
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_TAKE_PHOTO) {
        if (resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(photoURI).into(binding!!.imagen)
        } else {
            Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
        }
    }
}


override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
