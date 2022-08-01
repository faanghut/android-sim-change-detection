package com.faanghut.detectsimchange

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.faanghut.detectsimchange.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Request permission to access the device's SIM card.
        requestPermissionForReadPhoneState()

        // Setup Views
        setUpViews()

        // Add Click Listeners
        setUpClickListeners()
    }

    override fun onResume() {
        super.onResume()
        requestPermissionForReadPhoneState()
    }

    private fun setUpViews() {
        val sharedPreferences = getSharedPreferences("simData", MODE_PRIVATE)
        val simData = sharedPreferences.getStringSet("simData", setOf())
        if (simData != null) {
            if (simData.isNotEmpty() && simData.size >= 1) {
                binding.btnLogin.visibility = View.GONE
                binding.tickMark.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSimDataInSharedPreferences()
        }

        binding.btnReset.setOnClickListener {
            saveSimDataInSharedPreferences()
        }
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun saveSimDataInSharedPreferences() {
        val subscriptionManager = getSystemService(SubscriptionManager::class.java)
        val infoList = subscriptionManager?.activeSubscriptionInfoList

        val sharedPreferences = getSharedPreferences("simData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (infoList != null) {
            when (infoList.size) {
                0 -> {
                    editor.putStringSet("simData", setOf())
                    Toast.makeText(this, "No SIM Card Detected", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    binding.btnLogin.visibility = View.GONE
                    binding.tickMark.visibility = View.VISIBLE
                    binding.btnReset.visibility = View.GONE
                    editor.putStringSet("simData", setOf(infoList[0].subscriptionId.toString()))
                    Toast.makeText(this, "1 SIM Card Detected", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    binding.btnLogin.visibility = View.GONE
                    binding.tickMark.visibility = View.VISIBLE
                    binding.btnReset.visibility = View.GONE
                    editor.putStringSet("simData", setOf(infoList[0].subscriptionId.toString(), infoList[1].subscriptionId.toString()))
                    Toast.makeText(this, "2 SIM Cards Detected", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    editor.putStringSet("simData", setOf())
                    Toast.makeText(this, "More Than 2 SIM Card Detected, Ignored for Now", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Storing Empty Set in Shared Preferences
            editor.putStringSet("simData", setOf())
            Toast.makeText(this, "No SIM Card Detected/No Permission", Toast.LENGTH_SHORT).show()
        }
        editor.apply()
    }

    private fun requestPermissionForReadPhoneState() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
        } else {
            simDataObserver()
        }
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun simDataObserver() {
        val subscriptionManager = getSystemService(SubscriptionManager::class.java)
        val infoList = subscriptionManager?.activeSubscriptionInfoList
        var currSet = setOf<String>()
        if (infoList != null) {
            for (info in infoList)
                currSet = currSet.plus(info.subscriptionId.toString())
        }

        val sharedPreferences = getSharedPreferences("simData", MODE_PRIVATE)
        val prevInfoList = sharedPreferences.getStringSet("simData", setOf())

        if (prevInfoList == null || infoList == null || infoList.size == 0) {
            // Could happen if no SIM detected or if the user had logged in prior to setting SIM state.
            Toast.makeText(applicationContext, "LOGOUT USER, NO SIM DETECTED", Toast.LENGTH_SHORT).show()
            binding.btnReset.visibility = View.VISIBLE
            binding.tickMark.visibility = View.GONE
        } else {
            for (prevInfo in prevInfoList) {
                if (prevInfo !in currSet) {
                    Toast.makeText(applicationContext, "LOGOUT USER, NO SIM DETECTED", Toast.LENGTH_SHORT).show()
                    binding.btnReset.visibility = View.VISIBLE
                    binding.tickMark.visibility = View.GONE
                }
            }
        }
    }

}