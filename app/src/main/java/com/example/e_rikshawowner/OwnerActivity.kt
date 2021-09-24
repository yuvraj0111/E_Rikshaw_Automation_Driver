package com.example.e_rikshawowner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.e_rikshawowner.databinding.ActivityOwnerBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class OwnerActivity : AppCompatActivity() {

    private lateinit var toogle: ActionBarDrawerToggle
    private lateinit var binding: ActivityOwnerBinding
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var simpleDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner)
        setSupportActionBar(findViewById(R.id.action_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_owner)
        simpleDialog= Dialog(this)
        firebaseAuth= FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1083604959054-43th3co7arldu77mb1r6qb9jj57f6jc1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val db= FirebaseFirestore.getInstance()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val user=firebaseAuth.currentUser
        val drawayerLayout=binding.drawerLayout
        val navView=binding.navViewId
        //toogle= ActionBarDrawerToggle(this,drawayerLayout,R.string.open,R.string.close)
        toogle= ActionBarDrawerToggle(this,drawayerLayout,findViewById(R.id.action_bar),R.string.open,R.string.close)
        drawayerLayout.addDrawerListener(toogle)
        toogle.isDrawerIndicatorEnabled=true
        toogle.syncState()

        val headerView=navView.getHeaderView(0)
        val userNameTextView=headerView.findViewById<TextView>(R.id.username)
        val userEmailId=headerView.findViewById<TextView>(R.id.useremail)
        val userImage=headerView.findViewById<CircleImageView>(R.id.userimage)

        val provider=FirebaseAuth.getInstance().getAccessToken(false).getResult().signInProvider
        if(provider=="google.com")
            userNameTextView.text=user?.displayName.toString()

        if(provider=="password")
        {
            val useremail=user?.email.toString()
            val dref=db.collection("DRIVERS").document(useremail)

            dref.get().addOnSuccessListener { document ->
                val Name=document["Name"].toString()  //get("Name").toString()

                Log.d("fullName","full name is $Name")
                userNameTextView.text=Name

            }
                .addOnFailureListener{
                    Log.e("DriverName","failed to fetch name")
                }
        }
        userEmailId.text=user?.email.toString()
        Glide.with(this).load(user?.photoUrl).into(userImage)

        navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.logout ->
                {
                    simpleDialog.simpleloading()
                    val email = getIntent().extras?.getString("Email")
                    mGoogleSignInClient.signOut()
                    val intent=Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this,"LogOut",Toast.LENGTH_SHORT).show()
                    simpleDialog.dismissSimpleDialog()
                    finish()
                }
            }
            true
        }

        binding.enterLocationOnGoogleMapsButton.setOnClickListener {
                val intent=Intent(this,MapsActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toogle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
}