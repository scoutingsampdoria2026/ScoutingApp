package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccesso: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibile by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SCOUTING",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = SampColors.Blu
        )
        Text(
            text = "Sampdoria",
            style = MaterialTheme.typography.titleLarge,
            color = SampColors.Rosso
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false
            ),
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisibile) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisibile = !passwordVisibile }) {
                    Icon(
                        imageVector = if (passwordVisibile) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisibile) "Nascondi password" else "Mostra password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

        if (viewModel.caricamento) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(username.trim(), password, onLoginSuccesso) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accedi")
            }
        }

        viewModel.errore?.let { messaggio ->
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = messaggio,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
