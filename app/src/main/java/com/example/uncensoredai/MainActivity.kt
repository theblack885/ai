package com.example.uncensoredai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatMessage(val role: String, val text: String)

data class OllamaRequest(
    val model: String = "dolphin-llama3",
    val prompt: String,
    val stream: Boolean = false
)

data class OllamaResponse(val response: String)

interface OllamaApi {
    @POST("api/generate")
    suspend fun generateResponse(@Body request: OllamaRequest): OllamaResponse
}

object RetrofitClient {
    // Yahan apne PC ka local IP address dalein (jaise http://192.168.1.5:11434/)
    private const val BASE_URL = "http://YOUR_PC_IP_HERE:11434/"

    val api: OllamaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApi::class.java)
    }
}

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<ChatMessage>()
    var isLoading by mutableStateOf(false)

    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        messages.add(ChatMessage("user", userPrompt))
        isLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitClient.api.generateResponse(OllamaRequest(prompt = userPrompt))
                messages.add(ChatMessage("ai", res.response.trim()))
            } catch (e: Exception) {
                messages.add(ChatMessage("ai", "Error: ${e.localizedMessage ?: "Connection Failed"}"))
            } finally {
                isLoading = false
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Uncensored AI Assistant", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))

        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            items(vm.messages) { msg ->
                val isUser = msg.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isUser) Color(0xFF6200EE) else Color(0xFF2C2C2C),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = msg.text, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                    }
                }
            }
            if (vm.isLoading) {
                item { Text("AI is thinking...", color = Color.Yellow, fontSize = 12.sp, modifier = Modifier.padding(8.dp)) }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything...", color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val p = inputText
                    inputText = ""
                    vm.sendMessage(p)
                },
                enabled = !vm.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send")
            }
        }
    }
}
