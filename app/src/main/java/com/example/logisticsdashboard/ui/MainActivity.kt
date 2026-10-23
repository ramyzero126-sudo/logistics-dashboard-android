package com.example.logisticsdashboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.logisticsdashboard.R
import com.example.logisticsdashboard.api.RetrofitClient
import com.example.logisticsdashboard.data.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: com.example.logisticsdashboard.api.ApiService

    private lateinit var etToken: EditText
    private lateinit var btnSaveToken: Button
    private lateinit var btnGetRiders: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)
        apiService = RetrofitClient.create(tokenManager)

        etToken = findViewById(R.id.etToken)
        btnSaveToken = findViewById(R.id.btnSaveToken)
        btnGetRiders = findViewById(R.id.btnGetRiders)
        tvResult = findViewById(R.id.tvResult)

        // لو فيه توكن محفوظ نعرضه
        tokenManager.getAccessToken()?.let {
            etToken.setText(it)
        }

        btnSaveToken.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token.isNotEmpty()) {
                tokenManager.saveTokens(token)
                Toast.makeText(this, "تم حفظ التوكن", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "اكتب التوكن الأول", Toast.LENGTH_SHORT).show()
            }
        }

        btnGetRiders.setOnClickListener {
            if (!tokenManager.hasToken()) {
                Toast.makeText(this, "حط التوكن الأول", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    tvResult.text = "جاري التحميل..."
                    val response = apiService.getRiders(cityId = 214, page = 0, size = 10)

                    if (response.isSuccessful) {
                        tvResult.text = "نجح ✅\n\n${response.body()}"
                    } else {
                        tvResult.text = "فشل ❌\nكود الخطأ: \( {response.code()}\n \){response.message()}"
                    }
                } catch (e: Exception) {
                    tvResult.text = "خطأ: ${e.message}"
                }
            }
        }
    }
}
