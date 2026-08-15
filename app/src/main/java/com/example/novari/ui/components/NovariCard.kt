package com.example.novari.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape
import com.example.novari.ui.theme.NovariSpacing

@Composable
fun NovariCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(NovariSpacing.xl),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = NovariShape.card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NovariColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
