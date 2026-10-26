package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyManager
import com.example.data.repository.AuthRepository
import com.example.ui.components.GamerBazaarLogo
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerBg
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.GamerTextMuted
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.GoldLegendary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Register, 2: Forgot Password

    // Login state
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register state
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf("BUYER") }
    var regCurrency by remember { mutableStateOf("USD") }

    // Forgot password state
    var forgotEmail by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }

    // Verification Dialog
    var showVerifyDialog by remember { mutableStateOf(false) }
    var verificationCodeInput by remember { mutableStateOf("") }
    var generatedCodeHint by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GamerTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                GamerBazaarLogo(compact = true)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Login / Register / Forgot
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GamerSurface,
                    contentColor = NeonPurple,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GamerCardBorder, RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Sign In", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 0) NeonPurple else GamerTextSecondary) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Register", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 1) NeonPurple else GamerTextSecondary) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Reset", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 2) NeonPurple else GamerTextSecondary) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (selectedTab) {
                    0 -> {
                        // SIGN IN TAB
                        Text("Welcome Back, Gamer", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Log into your verified Gamer Bazaar account", fontSize = 12.sp, color = GamerTextSecondary)

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("Email or Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan) },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = GamerTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    val result = authRepository.login(loginIdentifier, loginPassword)
                                    isLoading = false
                                    result.onSuccess { user ->
                                        snackbarHostState.showSnackbar("Logged in as ${user.displayName}")
                                        onAuthSuccess()
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar("Login failed: ${err.message}")
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }

                    1 -> {
                        // REGISTER TAB
                        Text("Create Gaming Account", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Join worldwide verified gaming marketplace", fontSize = 12.sp, color = GamerTextSecondary)

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_input"),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = GamerTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Account Role Selection
                        Text("Primary Role", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("BUYER" to "Buyer Account", "SELLER" to "Seller Account").forEach { (roleKey, roleLabel) ->
                                val isSelected = regRole == roleKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonPurple else GamerSurface)
                                        .border(1.dp, if (isSelected) NeonPurple else GamerCardBorder, RoundedCornerShape(8.dp))
                                        .clickable { regRole = roleKey }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(roleLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    val result = authRepository.register(
                                        username = regUsername,
                                        email = regEmail,
                                        password = regPassword,
                                        role = regRole,
                                        preferredCurrency = regCurrency
                                    )
                                    isLoading = false
                                    result.onSuccess { user ->
                                        generatedCodeHint = user.verificationCode
                                        showVerifyDialog = true
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar("Registration failed: ${err.message}")
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }

                    2 -> {
                        // FORGOT PASSWORD TAB
                        Text("Reset Your Password", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Enter your account email to set a new password", fontSize = 12.sp, color = GamerTextSecondary)

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Account Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("New Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    val result = authRepository.resetPassword(forgotEmail, forgotNewPassword)
                                    result.onSuccess {
                                        snackbarHostState.showSnackbar("Password updated! You can now sign in.")
                                        selectedTab = 0
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar("Reset failed: ${err.message}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Fast Quick-Login Profiles (For test convenience)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GamerSurface)
                        .border(1.dp, GamerCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("Quick Profile Switcher (Testing & Evaluation)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        authRepository.switchAccount("ADMIN")
                                        snackbarHostState.showSnackbar("Switched to Admin profile")
                                        onAuthSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLegendary)
                            ) {
                                Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        authRepository.switchAccount("SELLER")
                                        snackbarHostState.showSnackbar("Switched to Seller profile")
                                        onAuthSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple)
                            ) {
                                Text("Seller", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        authRepository.switchAccount("BUYER")
                                        snackbarHostState.showSnackbar("Switched to Buyer profile")
                                        onAuthSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                            ) {
                                Text("Buyer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Email Verification Dialog Modal (Requirement 5)
        if (showVerifyDialog) {
            AlertDialog(
                onDismissRequest = { showVerifyDialog = false },
                containerColor = GamerSurface,
                title = {
                    Text("Verify Email Address", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                text = {
                    Column {
                        Text(
                            text = "A 6-digit verification code has been dispatched to $regEmail.",
                            fontSize = 12.sp,
                            color = GamerTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E1B33))
                                .padding(8.dp)
                        ) {
                            Text("Demo Verification Code: $generatedCodeHint or 123456", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = verificationCodeInput,
                            onValueChange = { verificationCodeInput = it.take(6) },
                            label = { Text("Enter 6-digit code") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val result = authRepository.verifyEmail(verificationCodeInput)
                                result.onSuccess {
                                    showVerifyDialog = false
                                    snackbarHostState.showSnackbar("Email verified successfully! Welcome to Gamer Bazaar.")
                                    onAuthSuccess()
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar("Verification error: ${err.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("Verify & Continue", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showVerifyDialog = false
                        onAuthSuccess()
                    }) {
                        Text("Skip for Now", color = GamerTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = GamerTextPrimary,
    unfocusedTextColor = GamerTextPrimary,
    focusedContainerColor = GamerSurface,
    unfocusedContainerColor = GamerSurface,
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = GamerCardBorder,
    focusedLabelColor = CyberCyan,
    unfocusedLabelColor = GamerTextSecondary
)
