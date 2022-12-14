package com.example.e_rikshawowner.loginFragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.e_rikshawowner.Dialog
import com.example.e_rikshawowner.OwnerActivity
import com.example.e_rikshawowner.R
import com.example.e_rikshawowner.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding:FragmentLoginBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentLoginBinding.inflate(layoutInflater)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1083604959054-43th3co7arldu77mb1r6qb9jj57f6jc1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        dialog= Dialog(requireActivity())

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)


        firebaseAuth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener{
            dialog.startloading()

            if(checking()) // Checking wheather user has filled ID and Password or not.
            {
                val email=binding.emailEditText.text.toString()
                val password=binding.passwordEditText.text.toString()
                firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(requireActivity()){ task ->
                        if(task.isSuccessful)
                        {
                            if (firebaseAuth.currentUser?.isEmailVerified == true) {
                            val name = firebaseAuth.currentUser?.displayName.toString()
                            val user = hashMapOf(
                                "Name" to name,
                                "Email" to email
                            )

                            val users = db.collection("DRIVERS")
                            val query = users.whereEqualTo("Email", email).get()
                                .addOnSuccessListener { it ->
                                    if (it.isEmpty) {
                                        users.document(email).set(user)
                                    }
                                }
                            Toast.makeText(requireActivity(),"Login Successfull", Toast.LENGTH_LONG).show()
                            val intent= Intent(requireActivity(),OwnerActivity::class.java)
                            intent.putExtra("Email",email)
                            startActivity(intent)
                            dialog.dismissSignInDialog()
                            requireActivity().finish()
                        }
                            else
                            {
                                dialog.dismissSignInDialog()
                                Toast.makeText(requireActivity(),"Email not verified",Toast.LENGTH_LONG).show()
                            }
                            }
                        else {
                            Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_LONG)
                                .show()
                            dialog.dismissSignInDialog()
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(requireActivity(),"User Id and Password not registered",
                            Toast.LENGTH_LONG).show()
                        dialog.dismissSignInDialog()
                    }
            }
            else
            {
                Toast.makeText(requireActivity(),"Please Enter Login ID and Password", Toast.LENGTH_LONG).show()
                dialog.dismissSignInDialog()
            }
        }

        binding.registerButton.setOnClickListener { view:View ->
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }


        binding.googleSignInButton.setOnClickListener {
            val intent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(intent)
        }

        return binding.root
    }

    private fun checking(): Boolean {
        val email=binding.emailEditText.text.toString()
        val password=binding.passwordEditText.text.toString()

        if(email.trim{it<=' '}.isNotEmpty() && password.trim{it<=' '}.isNotEmpty())
            return true
        else
            return false
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dialog.startloading()
            //Toast.makeText(requireContext(),"Login Succesfull",Toast.LENGTH_SHORT).show()
            // There are no request codes
            val data: Intent? = result.data
            val accountTask=GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogleAccount(account)
            }
            catch (e: ApiException)
            {
                Toast.makeText(requireContext(),"Login Failed",Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                // Sign in success, update UI with the signed-in user's information
                val firebaseuser = firebaseAuth.currentUser
                //val uid = firebaseuser!!.uid
                val email = firebaseuser!!.email
                val name=firebaseuser.displayName.toString()
                if (authResult.additionalUserInfo!!.isNewUser)
                {
                    val users=db.collection("DRIVERS")
                    val user= hashMapOf(
                        "Name" to name,
                        "Email" to email
                    )
                    users.document(email!!).set(user)
                    Toast.makeText(
                        requireActivity(),
                        "New user logged in with $email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT)
                    .show()

                val intent=Intent(requireActivity(),OwnerActivity::class.java)
                intent.putExtra("Email",email)
                startActivity(intent)
                dialog.dismissSignInDialog()
                requireActivity().finish()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
                dialog.dismissSignInDialog()
            }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}