package com.example.avisordincivisme.ui.notification

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.avisordincivisme.R
import com.example.avisordincivisme.databinding.FragmentMapBinding
import com.google.android.gms.maps.SupportMapFragment
import com.example.avisordincivisme.ui.Incidente
import com.example.avisordincivisme.ui.SharedViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class NotificationFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FirebaseApp.initializeApp(context as Context)
        val notificationViewModel =
            ViewModelProvider(this).get(NotificationViewModel::class.java)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment: SupportMapFragment =
            getChildFragmentManager().findFragmentById(R.id.g_map) as SupportMapFragment

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val base: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val users: DatabaseReference = base.child("users")
        val uid: DatabaseReference = users.child(auth.uid.toString())
        val incidencies: DatabaseReference = uid.child("incidencies")

        val model: SharedViewModel =
            ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        mapFragment.getMapAsync({ map ->
            map.setMyLocationEnabled(true)
            val currentLatlng: MutableLiveData<LatLng> = model.getCurrentLatLng()
            val owner: LifecycleOwner = viewLifecycleOwner
            currentLatlng.observe(owner, { latLng ->
                val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15F)
                map.animateCamera(cameraUpdate)
                currentLatlng.removeObservers(owner)
            })
            incidencies.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val incidente = snapshot.getValue(Incidente::class.java)

                    incidente?.let {
                        val aux: LatLng = LatLng(
                            incidente.lalitud.toDouble(),
                            incidente.longitud.toDouble()
                        )
                        map.addMarker(
                            MarkerOptions()
                                .title(incidente.problema)
                                .snippet(incidente.direccio)
                                .position(aux)
                        )
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        })

        return root
    }
    override fun onDestroyView() {
    super.onDestroyView()
        _binding = null
    }
}