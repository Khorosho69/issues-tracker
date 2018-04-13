package com.antont.issuestracker.activities

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.antont.issuestracker.R
import com.antont.issuestracker.view_models.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mLoginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mLoginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        if (!mLoginViewModel.isUserAuthorized()) {
            googleLoginButton.setOnClickListener {
                startActivityForResult(mLoginViewModel.getGoogleSingInIntent(), LoginViewModel.GOOGLE_SIGN_IN_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { intent -> mLoginViewModel.onActivityResult(requestCode, intent) }
    }
}
