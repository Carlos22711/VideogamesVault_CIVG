package com.gneticcore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.gneticcore.app.utils.GameConstants
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: GameViewModel,
    onSuccess: () -> Unit,
    onGoLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var username        by remember { mutableStateOf("") }
    var displayName     by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword    by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }
    var isLoading       by remember { mutableStateOf(false) }

    // Field errors
    var usernameError     by remember { mutableStateOf<String?>(null) }
    var displayNameError  by remember { mutableStateOf<String?>(null) }
    var passwordError     by remember { mutableStateOf<String?>(null) }
    var confirmError      by remember { mutableStateOf<String?>(null) }

    val requiredStr = stringResource(R.string.error_required)
    val minChars3   = stringResource(R.string.error_min_chars_var, 3)
    val minChars2   = stringResource(R.string.error_min_chars_var, 2)
    val minChars4   = stringResource(R.string.error_min_chars_var, 4)
    val passMismatch = stringResource(R.string.error_passwords_dont_match)

    fun validate(): Boolean {
        var ok = true
        usernameError    = if (username.isBlank()) { ok = false; requiredStr } else if (username.length < 3) { ok = false; minChars3 } else null
        displayNameError = if (displayName.isBlank()) { ok = false; requiredStr } else if (displayName.length < 2) { ok = false; minChars2 } else null
        passwordError    = if (password.length < 4) { ok = false; minChars4 } else null
        confirmError     = if (password != confirmPassword) { ok = false; passMismatch } else null
        return ok
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A0A0F), Color(0xFF1A0A2E), Color(0xFF0A0A0F)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(Icons.Default.PersonAdd, contentDescription = null,
                tint = Color(0xFF7C4DFF), modifier = Modifier.size(56.dp))
            Text(stringResource(R.string.screen_register), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))

            Spacer(Modifier.height(4.dp))

            // Username
            OutlinedTextField(
                value = username, onValueChange = { username = it; usernameError = null },
                label = { Text(stringResource(R.string.field_username), color = Color(0xFFB0B0C8)) },
                placeholder = { Text(stringResource(R.string.hint_username), color = Color(0xFF555570)) },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = Color(0xFF7C4DFF)) },
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it, color = Color(0xFFFF5252)) } },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )

            // Display name
            OutlinedTextField(
                value = displayName, onValueChange = { displayName = it; displayNameError = null },
                label = { Text(stringResource(R.string.field_display_name), color = Color(0xFFB0B0C8)) },
                placeholder = { Text(stringResource(R.string.hint_display_name), color = Color(0xFF555570)) },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = Color(0xFF7C4DFF)) },
                isError = displayNameError != null,
                supportingText = { Text(displayNameError ?: stringResource(R.string.label_display_name_help),
                    color = if (displayNameError != null) Color(0xFFFF5252) else Color(0xFF666680)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )

            // Password
            OutlinedTextField(
                value = password, onValueChange = { password = it; passwordError = null },
                label = { Text(stringResource(R.string.field_password), color = Color(0xFFB0B0C8)) },
                placeholder = { Text(stringResource(R.string.hint_password), color = Color(0xFF555570)) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF7C4DFF)) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color(0xFFB0B0C8))
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = Color(0xFFFF5252)) } },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )

            // Confirm password
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it; confirmError = null },
                label = { Text(stringResource(R.string.field_confirm_password), color = Color(0xFFB0B0C8)) },
                leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = Color(0xFF7C4DFF)) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmError != null,
                supportingText = confirmError?.let { { Text(it, color = Color(0xFFFF5252)) } },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )

            errorMsg?.let {
                Text(it, color = Color(0xFFFF5252), fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (!validate()) return@Button
                    isLoading = true; errorMsg = null
                    scope.launch {
                        when (val r = viewModel.register(username, displayName, password, confirmPassword)) {
                            is AuthResult.Success -> onSuccess()
                            is AuthResult.Error   -> { errorMsg = r.message; isLoading = false }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.btn_register), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(onClick = onGoLogin) {
                Text(stringResource(R.string.btn_go_login), color = Color(0xFF00E5FF))
            }
        }
    }
}
