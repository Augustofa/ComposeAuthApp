package me.augusto.composeauthenticationapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

val FirebaseRed = Color(0xFFE53935)

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthNavigation(authViewModel)
                }
            }
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null
)

@Composable
fun AuthNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authUiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = viewModel)
        }
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authUiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(150.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.firebase_logo),
                contentDescription = "Firebase Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Authentication",
                fontSize = 32.sp,
                color = FirebaseRed,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(50.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Your E-mail",
            keyboardType = KeyboardType.Email,
            leadingIconPainter = painterResource(id = R.drawable.email_icon)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            leadingIconPainter = painterResource(id = R.drawable.key_icon)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.loginUser(email, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FirebaseRed)
            ) {
                Text("Login", fontSize = 22.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 50.dp)) {
            Text("Don't have an account?", fontSize = 18.sp)
            ClickableText(
                text = AnnotatedString("Sign Up"),
                onClick = { navController.navigate("register") },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(10.dp)
            )
        }
    }

    authState.error?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authUiState.collectAsState()
    val context = LocalContext.current

    fun validateFields(): Boolean {
        passwordError = if (password.length < 6) "Must be at least 6 characters long" else null
        confirmPasswordError = if (password != confirmPassword) "Passwords must match" else null
        return passwordError == null && confirmPasswordError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.firebase_logo),
                contentDescription = "Firebase Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Creating\nAccount",
                fontSize = 32.sp,
                color = FirebaseRed,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Your E-mail",
            keyboardType = KeyboardType.Email,
            leadingIconPainter = painterResource(id = R.drawable.email_icon)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it; validateFields() },
            label = "Your Password",
            keyboardType = KeyboardType.Password,
            leadingIconPainter = painterResource(id = R.drawable.key_icon),
            isError = passwordError != null,
            errorMessage = passwordError
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; validateFields() },
            label = "Confirm your password",
            keyboardType = KeyboardType.Password,
            leadingIconPainter = painterResource(id = R.drawable.key_confirm_icon),
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (validateFields()) {
                        viewModel.registerUser(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.length >= 6 && password == confirmPassword,
                colors = ButtonDefaults.buttonColors(containerColor = FirebaseRed)
            ) {
                Text("Register", fontSize = 22.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 50.dp)) {
            Text("Already have an account?", fontSize = 18.sp)
            ClickableText(
                text = AnnotatedString("Sign In"),
                onClick = { navController.navigate("login") },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(10.dp)
            )
        }
    }

    authState.error?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel) {
    val authState by viewModel.authUiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome!", fontSize = 50.sp, color = FirebaseRed)
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("Logged User: ", fontSize = 18.sp)
                Text(
                    text = authState.userEmail ?: "No email found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        ClickableText(
            text = AnnotatedString("Logout"),
            onClick = { viewModel.logoutUser() },
            style = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        )
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated && navController.currentDestination?.route != "login") {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    leadingIconPainter: Painter,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = {
                Image(
                    painter = leadingIconPainter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            visualTransformation = if (keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

