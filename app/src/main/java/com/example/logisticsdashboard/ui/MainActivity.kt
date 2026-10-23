
package com.example.logisticsdashboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.logisticsdashboard.R
import com.example.logisticsdashboard.api.ApiService
import com.example.logisticsdashboard.api.RetrofitClient
import com.example.logisticsdashboard.data.TokenManager
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService

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

        etToken.setText(tokenManager.getAccessToken().orEmpty())

        btnSaveToken.setOnClickListener {
            val token = etToken.text.toString().trim()

            if (token.isBlank()) {
                Toast.makeText(
                    this,
                    "اكتب التوكن الأول",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                tokenManager.saveTokens(token)
                Toast.makeText(
                    this,
                    "تم حفظ التوكن",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnGetRiders.setOnClickListener {
            val token = etToken.text.toString().trim()

            if (token.isBlank()) {
                Toast.makeText(
                    this,
                    "اكتب التوكن الأول",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            tokenManager.saveTokens(token)
            loadRiders()
        }
    }

    private fun loadRiders() {
        lifecycleScope.launch {
            btnGetRiders.isEnabled = false
            tvResult.text = "جاري الاتصال بالسيرفر..."

            try {
                val response = apiService.getRiders(
                    cityId = 214,
                    page = 0,
                    size = 10
                )

                val serverText = if (response.isSuccessful) {
                    response.body()?.use { it.string() }.orEmpty()
                } else {
                    response.errorBody()?.use { it.string() }.orEmpty()
                }

                tvResult.text = buildString {
                    append(
                        if (response.isSuccessful) {
                            "تم الاتصال بالسيرفر ✅"
                        } else {
                            "السيرفر رفض الطلب ❌"
                        }
                    )
                    append("\nHTTP: ${response.code()}")
                    append("\nالحالة: ${response.message()}")
                    append("\n\nرد السيرفر:\n")
                    append(
                        serverText
                            .take(2500)
                            .ifBlank { "السيرفر رجّع ردًا فارغًا" }
                    )
                }

                when (response.code()) {
                    401 -> Toast.makeText(
                        this@MainActivity,
                        "التوكن غير صالح أو انتهت صلاحيته",
                        Toast.LENGTH_LONG
                    ).show()

                    403 -> Toast.makeText(
                        this@MainActivity,
                        "الحساب لا يملك صلاحية الوصول",
                        Toast.LENGTH_LONG
                    ).show()

                    404 -> Toast.makeText(
                        this@MainActivity,
                        "مسار API أو رقم المدينة غير صحيح",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                tvResult.text =
                    "فشل الاتصال بالإنترنت ❌\n${e.message ?: "تحقق من الاتصال"}"
            } catch (e: Exception) {
                tvResult.text = buildString {
                    append("حدث خطأ ❌\n")
                    append(e.javaClass.simpleName)
                    append("\n")
                    append(e.message ?: "خطأ غير معروف")
                }
            } finally {
                btnGetRiders.isEnabled = true
            }
        }
    }
}
