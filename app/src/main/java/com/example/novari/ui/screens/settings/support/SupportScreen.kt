package com.example.novari.ui.screens.settings.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariTypography
import androidx.core.net.toUri

@Composable
fun SupportScreen(
    onBackClick: () -> Unit,
    supportEmail: String = "support@novari.app"
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val canSend = name.isNotBlank() && message.isNotBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NovariColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 28.dp
        )
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(23.dp)
                    )
                }

            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Support",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "We’re here to help you.",
                style = NovariTypography.bodyLarge,
                color = NovariColors.Slate
            )

            Spacer(Modifier.height(34.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NovariColors.Divider)
            )

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_support_headset),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.width(20.dp))

                Text(
                    text = "Send us a message and we’ll get\nback to you as soon as possible.",
                    style = NovariTypography.bodyLarge,
                    color = NovariColors.Slate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = NovariColors.Surface,
                border = BorderStroke(1.dp, NovariColors.Border),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 36.dp,
                        vertical = 28.dp
                    )
                ) {
                    Text(
                        text = "Name",
                        style = NovariTypography.titleLarge,
                        fontSize = 15.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),

                        placeholder = {
                            Text(
                                "Enter your name",
                                style = NovariTypography.bodyLarge,
                                color = NovariColors.Slate
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovariColors.Teal,
                            unfocusedBorderColor = NovariColors.Border,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = NovariColors.Teal
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.height(26.dp))

                    Text(
                        text = "Message",
                        style = NovariTypography.titleLarge,
                        fontSize = 15.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        placeholder = {
                            Text(
                                "Write your message here...",
                                style = NovariTypography.bodyLarge,
                                color = NovariColors.Slate
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovariColors.Teal,
                            unfocusedBorderColor = NovariColors.Border,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = NovariColors.Teal
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = "To (Email ID)",
                        style = NovariTypography.titleLarge,
                        fontSize = 15.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .border(
                                1.dp,
                                NovariColors.Border,
                                RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Text(
                            text = supportEmail,
                            style = NovariTypography.bodyLarge,
                            color = NovariColors.Navy
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            sendSupportEmail(
                                context = context,
                                supportEmail = supportEmail,
                                name = name.trim(),
                                message = message.trim()
                            )
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovariColors.Teal,
                            contentColor = Color.White,
                            disabledContainerColor = NovariColors.Mint,
                            disabledContentColor = NovariColors.Muted
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(Modifier.width(16.dp))

                        Text(
                            text = "Send Message",
                            style = NovariTypography.titleLarge,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(45.dp)
                )

                Spacer(Modifier.width(18.dp))

                Text(
                    text = "Your message will be sent using your default email app.\nNovari does not store or transmit your message.",
                    style = NovariTypography.bodyMedium,
                    color = NovariColors.Slate,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun sendSupportEmail(
    context: Context,
    supportEmail: String,
    name: String,
    message: String
) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:${Uri.encode(supportEmail)}".toUri()
        putExtra(
            Intent.EXTRA_SUBJECT,
            "Novari Support Request"
        )
        putExtra(
            Intent.EXTRA_TEXT,
            """
            Name: $name

            Message:
            $message
            """.trimIndent()
        )
    }

    runCatching {
        context.startActivity(intent)
    }
}
