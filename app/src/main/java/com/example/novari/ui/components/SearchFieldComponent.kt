package com.example.novari.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariTypography

@Composable
fun SearchFieldComponent(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    val shape = RoundedCornerShape(13.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(NovariColors.Surface)
            .border(
                width = 1.dp,
                color = NovariColors.Border,
                shape = shape
            )
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = NovariColors.DarkTeal,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.size(10.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = NovariTypography.bodyLarge.copy(
                color = NovariColors.Navy
            ),
            cursorBrush = SolidColor(NovariColors.Teal),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                }
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_transactions),
                        style = NovariTypography.bodyLarge,
                        color = NovariColors.Slate
                    )
                }

                innerTextField()
            }
        )

        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.size(12.dp))

            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = NovariColors.Mint,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.clear_search),
                    tint = NovariColors.Navy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}