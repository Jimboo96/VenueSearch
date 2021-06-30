package com.example.venuesearch

import android.Manifest
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.venuesearch.data.*
import com.example.venuesearch.db.SharedPrefs
import com.example.venuesearch.location.CurrentLocationProvider
import com.example.venuesearch.utils.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.example.venuesearch.utils.PERMISSION_DENIED
import com.example.venuesearch.utils.PERMISSION_GRANTED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.venue_searcher.*
import javax.inject.Inject

@AndroidEntryPoint
class VenueSearcherActivity : AppCompatActivity(), VenueSearcherContract.View {

    private lateinit var presenter: VenueSearcherPresenter
    private lateinit var adapter: VenueSearcherRecyclerAdapter
    private lateinit var prefs: SharedPrefs

    private var queryWord: String = ""

    @Inject
    lateinit var api: VenueSearchApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.venue_searcher)
        supportActionBar?.hide()
        setupSharedPrefs()
        setupPresenter()
        setupRecyclerView()
        venue_searcher_edit_text.addTextChangedListener(textWatcher)
        venue_searcher_clear_button.setOnClickListener(clearButtonOnClickListener)
    }

    private fun setupSharedPrefs() {
        prefs = SharedPrefs(applicationContext)
        if (prefs.locationUpdateIsReceived()) {
            venue_searcher_waiting_for_location_layout.visibility = View.GONE
        }
    }

    private fun setupPresenter() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationProvider = CurrentLocationProvider(
            applicationContext,
            locationManager
        )
        presenter =
            VenueSearcherPresenter(this, locationProvider, api, prefs)
    }

    private fun setupRecyclerView() {
        adapter = VenueSearcherRecyclerAdapter(emptyList())
        venue_searcher_result_list.adapter = adapter
        venue_searcher_result_list.layoutManager = LinearLayoutManager(applicationContext)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            // Not needed.
        }

        override fun beforeTextChanged(
            sequence: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
            // Not needed.
        }

        override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
            queryWord = sequence!!.toString()
            presenter.searchForVenues(queryWord)
        }
    }

    private val clearButtonOnClickListener = View.OnClickListener {
        runOnUiThread {
            adapter.updateDataSet(emptyList())
            venue_searcher_edit_text.setText("", TextView.BufferType.EDITABLE)
        }
    }

    override fun updateVenueList(list: List<Venue>) {
        runOnUiThread {
            adapter.updateDataSet(list)
        }
    }

    override fun hideWaitingForLocationView() {
        runOnUiThread {
            venue_searcher_waiting_for_location_layout.visibility = View.GONE
        }
        presenter.searchForVenues(queryWord)
    }

    override fun updateInfoTextVisibility(visibility: Int) {
        runOnUiThread {
            venue_searcher_info_text.visibility = visibility
        }
    }

    override fun updateLoadingProgressBarVisibility(visibility: Int) {
        runOnUiThread {
            venue_searcher_loading_data_progress_bar.visibility = visibility
        }
    }

    override fun showQuotaLimitToast() {
        toast(R.string.toast_quota_limit_exceeded)
    }

    override fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            ACCESS_FINE_LOCATION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            println(" jee ${grantResults[0]}")
            when (grantResults[0]) {
                PERMISSION_DENIED -> {
                    toast(R.string.toast_location_permission_denied)
                }
                PERMISSION_GRANTED -> {
                    presenter.startLocationUpdates()
                }
            }
        }
    }

    private fun toast(stringId: Int) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                getString(stringId),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}