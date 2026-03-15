package com.gneticcore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gneticcore.app.R
import com.gneticcore.app.ui.viewmodel.AuthResult
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.UserSession
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: GameViewModel,
    onSuccess: (isAdmin: Boolean) -> Unit,
    onGoRegister: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var username      by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var showPassword  by remember { mutableStateOf(false) }
    var errorMsg      by remember { mutableStateOf<String?>(null) }
    var isLoading     by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A0A0F), Color(0xFF1A0A2E), Color(0xFF0A0A0F)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.SportsEsports, contentDescription = null,
                tint = Color(0xFF7C4DFF), modifier = Modifier.size(64.dp))
            Text(stringResource(R.string.app_name), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
            Text(stringResource(R.string.screen_login), fontSize = 16.sp, color = Color(0xFFB0B0C8))

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it; errorMsg = null },
                label = { Text(stringResource(R.string.field_username), color = Color(0xFFB0B0C8)) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF7C4DFF)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it; errorMsg = null },
                label = { Text(stringResource(R.string.field_password), color = Color(0xFFB0B0C8)) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF7C4DFF)) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = Color(0xFFB0B0C8))
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )

            errorMsg?.let {
                Text(it, color = Color(0xFFFF5252), fontSize = 13.sp)
            }

            val fieldsRequired = stringResource(R.string.error_required_fields)

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMsg = fieldsRequired; return@Button
                    }
                    isLoading = true
                    scope.launch {
                        when (val result = viewModel.login(username, password)) {
                            is AuthResult.Success -> onSuccess(UserSession.isAdmin)
                            is AuthResult.Error   -> { errorMsg = result.message; isLoading = false }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.btn_login), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(onClick = onGoRegister) {
                Text(stringResource(R.string.btn_go_register), color = Color(0xFF00E5FF))
            }
        }
    }
}
