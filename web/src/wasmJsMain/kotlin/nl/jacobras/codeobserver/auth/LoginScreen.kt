package nl.jacobras.codeobserver.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gabrieldrn.carbon.Carbon
import com.gabrieldrn.carbon.button.ButtonType
import com.gabrieldrn.carbon.foundation.color.CarbonLayer
import com.gabrieldrn.carbon.foundation.color.layerBackground
import com.gabrieldrn.carbon.textinput.PasswordInput
import com.gabrieldrn.carbon.textinput.TextInput
import nl.jacobras.codeobserver.util.ui.button.SmallProgressButton
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LoginScreen() {
    val viewModel = koinViewModel<LoginViewModel>()
    val loggingIn by viewModel.loggingIn.collectAsState()
    val error by viewModel.error.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordHidden by remember { mutableStateOf(true) }

    val canSubmit = username.isNotBlank() && password.isNotEmpty() && !loggingIn

    fun doLogin() {
        viewModel.login(username.trim(), password)
    }

    val usernameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        usernameFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CarbonLayer {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .layerBackground()
                    .padding(24.dp)
            ) {
                BasicText(
                    text = "Log in",
                    style = Carbon.typography.heading06
                )
                Spacer(Modifier.height(16.dp))

                TextInput(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.focusRequester(usernameFocusRequester)
                )
                Spacer(Modifier.height(8.dp))
                PasswordInput(
                    label = "Password",
                    value = password,
                    passwordHidden = passwordHidden,
                    onValueChange = { password = it },
                    onPasswordHiddenChange = { passwordHidden = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { doLogin() })
                )

                error?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    BasicText(
                        text = message,
                        style = Carbon.typography.bodyCompact01.copy(color = Carbon.theme.textError)
                    )
                }

                Spacer(Modifier.height(16.dp))
                SmallProgressButton(
                    label = "Log in",
                    buttonType = ButtonType.Primary,
                    isEnabled = canSubmit,
                    loading = loggingIn,
                    onClick = { doLogin() }
                )
            }
        }
    }
}