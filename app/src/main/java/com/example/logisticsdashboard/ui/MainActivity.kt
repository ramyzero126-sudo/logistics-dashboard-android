
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
            saveToken()
        }

        btnGetRiders.setOnClickListener {
            val token = etToken.text.toString().trim()

            if (token.isBlank()) {
                showToast("أدخل بيانات المصادقة أولاً")
                return@setOnClickListener
            }

            tokenManager.saveTokens(token)
            loadRiders()
        }
    }

    private fun saveToken() {
        val token = etToken.text.toString().trim()

        if (token.isBlank()) {
            showToast("اكتب التوكن الأول")
            return
        }

        tokenManager.saveTokens(token)
        showToast("تم حفظ التوكن")
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

                val contentType =
                    response.headers()["Content-Type"].orEmpty()

                val bodyStart = serverText.trimStart()

                val isHtml =
                    contentType.contains(
                        "text/html",
                        ignoreCase = true
                    ) ||
                    bodyStart.startsWith(
                        "<!DOCTYPE html",
                        ignoreCase = true
                    ) ||
                    bodyStart.startsWith(
                        "<html",
                        ignoreCase = true
                    )

                val isJson =
                    contentType.contains(
                        "application/json",
                        ignoreCase = true
                    ) ||
                    bodyStart.startsWith("{") ||
                    bodyStart.startsWith("[")

                val title = when {
                    response.code() == 401 ->
                        "المصادقة غير مقبولة ❌"

                    response.code() == 403 ->
                        "الوصول مرفوض ❌"

                    isHtml ->
                        "وصلت صفحة HTML بدل بيانات المناديب ❌"

                    response.isSuccessful && isJson ->
                        "تم استلام رد JSON ✅"

                    response.isSuccessful ->
                        "الاتصال نجح لكن الرد ليس JSON ❌"

                    else ->
                        "السيرفر رفض الطلب ❌"
                }

                tvResult.text = buildString {
                    append(title)
                    append("\nHTTP: ${response.code()}")
                    append("\nالحالة: ${response.message()}")
                    append(
                        "\nنوع المحتوى: ${
                            contentType.ifBlank { "غير محدد" }
                        }"
                    )

                    if (isHtml) {
                        append("\n\nالتوضيح:")
                        append(
                            "\nالسيرفر أعاد صفحة HTML، غالبًا صفحة " +
                                "Cloudflare Access أو تسجيل الدخول."
                        )
                        append(
                            "\nHTTP 200 وحده لا يؤكد وصول بيانات API."
                        )
                    } else if (response.isSuccessful && isJson) {
                        append(
                            "\n\nتم استلام JSON. " +
                                "هذا يؤكد صيغة الرد فقط، وليس صحة محتواه."
                        )
                    }

                    append("\n\nرد السيرفر:\n")
                    append(
                        serverText.take(2500)
                            .ifBlank {
                                "السيرفر رجّع ردًا فارغًا"
                            }
                    )
                }

                when {
                    response.code() == 401 -> {
                        showToast(
                            "راجع بيانات المصادقة وصلاحيتها"
                        )
                    }

                    response.code() == 403 -> {
                        showToast(
                            "تحقق من صلاحيات الحساب والوصول"
                        )
                    }

                    response.code() == 404 -> {
                        showToast(
                            "راجع مسار API ورقم المدينة"
                        )
                    }

                    isHtml -> {
                        showToast(
                            "الرد صفحة تسجيل دخول، وليس بيانات المناديب"
                        )
                    }
                }

            } catch (e: IOException) {
                tvResult.text = buildString {
                    append("فشل الاتصال بالإنترنت ❌\n")
                    append(
                        e.message ?: "تحقق من الاتصال وحاول مرة أخرى"
                    )
                }
            } catch (e: Exception) {
                tvResult.text = buildString {
                    append("حدث خطأ أثناء قراءة الرد ❌\n")
                    append(e.javaClass.simpleName)
                    append("\n")
                    append(e.message ?: "خطأ غير معروف")
                }
            } finally {
                btnGetRiders.isEnabled = true
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
