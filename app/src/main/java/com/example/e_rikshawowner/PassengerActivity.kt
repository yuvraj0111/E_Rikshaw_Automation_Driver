package com.example.e_rikshawowner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.e_rikshawowner.databinding.ActivityPassengerBinding
import com.google.firebase.database.*

class PassengerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassengerBinding
    private lateinit var database:FirebaseDatabase
    private lateinit var databaseref:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPassengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
       database= FirebaseDatabase.getInstance()

        val email=intent.extras?.getString("EmailOfPassenger")
        databaseref=database.getReference("USERS").child(email.toString())
        databaseref.addValueEventListener(logListener)
    }


    val logListener = object : ValueEventListener{

        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()) {
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val long = snapshot.child("longitude").getValue(Double::class.java)
                if(lat!=null && long!=null)
                {
                    val PassengerLoc=LatLng(lat,long)
                    mMap.addMarker(MarkerOptions().position(PassengerLoc).title("Pick From Here"))
                    //Zoom level - 1: World, 5: Landmass/continent, 10: City, 15: Streets and 20: Buildings
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PassengerLoc,15f))
                }

            }

        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext, "Could not read from database", Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera


    }
}