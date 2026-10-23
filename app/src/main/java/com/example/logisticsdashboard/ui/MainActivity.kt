
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

        // استرجاع التوكن المحفوظ
        etToken.setText(tokenManager.getAccessToken().orEmpty())

        // حفظ التوكن
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

        // جلب المناديب
        btnGetRiders.setOnClickListener {
            // حفظ أحدث قيمة مكتوبة قبل الطلب
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

            lifecycleScope.launch {
                btnGetRiders.isEnabled = false
                tvResult.text = "جاري تحميل بيانات المناديب..."

                try {
                    val response = apiService.getRiders(
                        cityId = 214,
                        page = 0,
                        size = 10
                    )

                    if (response.isSuccessful) {
                        val body = response.body()

                        tvResult.text = if (body != null) {
                            "تم الاتصال بنجاح ✅\n\n$body"
                        } else {
                            "تم الاتصال بنجاح، لكن الاستجابة فارغة."
                        }
                    } else {
                        tvResult.text =
                            "فشل الطلب ❌\n" +
                            "كود HTTP: ${response.code()}\n" +
                            "الرسالة: ${response.message()}"

                        if (response.code() == 401) {
                            Toast.makeText(
                                this@MainActivity,
                                "التوكن غير صالح أو انتهت صلاحيته",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    tvResult.text =
                        "حدث خطأ أثناء الاتصال ❌\n" +
                        (e.message ?: "خطأ غير معروف")
                } finally {
                    btnGetRiders.isEnabled = true
                }
            }
        }
    }
}
