package com.example.avisordincivisme.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avisordincivisme.databinding.FragmentReportBinding
import com.example.avisordincivisme.databinding.ReciclerViewItemBinding
import com.example.avisordincivisme.ui.Incidente
import com.example.avisordincivisme.ui.SharedViewModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class DashboardFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private var authUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        _binding = FragmentReportBinding.inflate(inflater, container, false)

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user

            user?.let {
                val base = FirebaseDatabase.getInstance().reference
                val users = base.child("users")
                val uid = users.child(user.uid)
                val incidencies = uid.child("incidencies")

                val options: FirebaseRecyclerOptions<Incidente> = FirebaseRecyclerOptions.Builder<Incidente>()
                    .setQuery(incidencies, Incidente::class.java)
                    .setLifecycleOwner(viewLifecycleOwner)
                    .build()

                val adapter = IncidenciaAdapter(options)

                binding.rvIncidencies.adapter = adapter
                binding.rvIncidencies.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class IncidenciaAdapter(options: FirebaseRecyclerOptions<Incidente>) :
        FirebaseRecyclerAdapter<Incidente, IncidenciaAdapter.IncidenciaViewholder>(options) {

        override fun onBindViewHolder(
            holder: IncidenciaViewholder, position: Int, model: Incidente
        ) {
            holder.binding.txtDescripcio.text = model.problema
            holder.binding.txtAdreca.text = model.direccio
            holder.binding.carles.text = model.carles
        }

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): IncidenciaViewholder {
            val binding = ReciclerViewItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return IncidenciaViewholder(binding)
        }

        inner class IncidenciaViewholder(val binding: ReciclerViewItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
