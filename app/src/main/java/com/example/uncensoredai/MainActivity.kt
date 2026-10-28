package com.example.uncensoredai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    // Aapka IP address yahan successfully set kar diya gaya hai
    private const val BASE_URL = "http://192.168.0.31:11434/"

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

    init {
        messages.add(ChatMessage("ai", "Neural Link Active. Uncensored Mode Enabled. What is your command?"))
    }

    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        messages.add(ChatMessage("user", userPrompt))
        isLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitClient.api.generateResponse(OllamaRequest(prompt = userPrompt))
                messages.add(ChatMessage("ai", res.response.trim()))
            } catch (e: Exception) {
                messages.add(ChatMessage("ai", "Connection Error: Check local server IP. (${e.localizedMessage})"))
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0A0A0F), Color(0xFF12121A), Color(0xFF050508))
                            )
                        )
                ) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEXUS // UNRESTRICTED",
                    color = Color(0xFF00E5FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AI Core v2.7 Active",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF00FF66))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF2A2A3D), thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vm.messages) { msg ->
                ModernMessageBubble(msg)
            }
            if (vm.isLoading) {
                item {
                    Text(
                        text = "⚡ Processing neural stream...",
                        color = Color(0xFFFF007F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                placeholder = { Text("Ask anything without restrictions...", color = Color(0xFF6B6B83)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x99161622),
                    unfocusedContainerColor = Color(0x9910101A),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val p = inputText
                    inputText = ""
                    vm.sendMessage(p)
                },
                enabled = !vm.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    disabledContainerColor = Color(0xFF2A2A3D)
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Text(
                    text = "SEND",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ModernMessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (isUser) Color(0xFF1E1E2F) else Color(0xFF13131A),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .border(
                    width = 1.dp,
                    color = if (isUser) Color(0xFFFF007F).copy(alpha = 0.5f) else Color(0xFF00E5FF).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "YOU" else "NEXUS AI",
                    color = if (isUser) Color(0xFFFF007F) else Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.text,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
