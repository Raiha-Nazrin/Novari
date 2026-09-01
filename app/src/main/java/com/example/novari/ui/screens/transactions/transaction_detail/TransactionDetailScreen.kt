package com.example.novari.ui.screens.transactions.transaction_detail


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    uiState: TransactionDetailsUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovariColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 20.sp,
                        color = NovariColors.Navy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NovariColors.Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        when (uiState) {
            TransactionDetailsUiState.Loading -> LoadingState(Modifier.padding(innerPadding))
            TransactionDetailsUiState.NotFound -> NotFoundState(
                onBackClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
            is TransactionDetailsUiState.Content -> TransactionDetailsContent(
                transaction = uiState.transaction,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = NovariColors.Teal)
    }
}

@Composable
private fun NotFoundState(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "This transaction no longer exists",
            style = NovariTypography.headlineSmall,
            color = NovariColors.Navy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(NovariColors.PaleTeal)
                .clickable(onClick = onBackClick)
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Go back",
                style = NovariTypography.labelLarge,
                color = NovariColors.DarkTeal
            )
        }
    }
}

@Composable
private fun TransactionDetailsContent(
    transaction: TransactionDetailsUiModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 28.dp
        )
    ) {

        // ---------------------------------------------------------
        // Date subtitle
        // ---------------------------------------------------------

        item {
            Text(
                text = "${transaction.dateLabel} · ${transaction.dateTime}",
                style = NovariTypography.bodyLarge,
                color = NovariColors.Slate,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 2.dp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ---------------------------------------------------------
        // Hero transaction card
        // ---------------------------------------------------------

        item {
            TransactionHeroCard(
                transaction = transaction
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // ---------------------------------------------------------
        // Transaction information
        // ---------------------------------------------------------

        item {
            Text(
                text = "Transaction information",
                style = MaterialTheme.typography.titleMedium,
                color = NovariColors.Navy,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TransactionInformationCard(
                transaction = transaction
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // ---------------------------------------------------------
        // Notes
        // ---------------------------------------------------------

        if (!transaction.note.isNullOrBlank()) {
            item {
                Text(
                    text = "Notes",
                    style = NovariTypography.headlineSmall,
                    color = NovariColors.Navy,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                NotesCard(
                    note = transaction.note
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // ---------------------------------------------------------
        // Actions
        // ---------------------------------------------------------

        item {
            TransactionActions(
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun TransactionHeroCard(
    transaction: TransactionDetailsUiModel,
    modifier: Modifier = Modifier
) {
    val amountColor =
        if (transaction.isIncome) {
            NovariColors.Teal
        } else {
            NovariColors.Error
        }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NovariColors.Surface)
            .border(
                width = 1.dp,
                color = NovariColors.Border,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(
                horizontal = 24.dp,
                vertical = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Category icon
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(NovariColors.PaleTeal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = transaction.categoryIconRes),
                contentDescription = transaction.category,
                tint = NovariColors.Teal,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = transaction.merchant,
            style = NovariTypography.headlineSmall,
            color = NovariColors.Navy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${if (transaction.isIncome) "+" else "-"}₹${transaction.amount}",
            style = NovariTypography.displayMedium,
            color = amountColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${transaction.category} · ${transaction.dateLabel}",
            style = NovariTypography.bodyLarge,
            color = NovariColors.Slate,
            textAlign = TextAlign.Center
        )

        if (transaction.isAutoDetected) {
            Spacer(modifier = Modifier.height(12.dp))
            AutoDetectedChip()
        }
    }
}

@Composable
private fun AutoDetectedChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NovariColors.PaleTeal)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Sms,
            contentDescription = null,
            tint = NovariColors.DarkTeal,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Auto-detected from SMS",
            style = NovariTypography.labelSmall,
            color = NovariColors.DarkTeal
        )
    }
}

@Composable
private fun TransactionInformationCard(
    transaction: TransactionDetailsUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NovariColors.Surface)
            .border(
                width = 1.dp,
                color = NovariColors.Border,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp)
    ) {

        TransactionInfoRow(
            label = "Merchant",
            value = transaction.merchant
        )

        TransactionInfoDivider()

        TransactionInfoRow(
            label = "Category",
            value = transaction.category,
            trailingContent = {
                CategoryIcon(iconRes = transaction.categoryIconRes)
            }
        )

        TransactionInfoDivider()

        TransactionInfoRow(
            label = "Date",
            value = transaction.dateTime
        )

        TransactionInfoDivider()

        TransactionInfoRow(
            label = "Amount",
            value = "${if (transaction.isIncome) "+" else "-"}₹${transaction.amount}",
            valueColor = if (transaction.isIncome) {
                NovariColors.Teal
            } else {
                NovariColors.Navy
            },
            valueFontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransactionInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = NovariColors.Navy,
    valueFontWeight: FontWeight = FontWeight.Normal,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            style = NovariTypography.bodyLarge,
            color = NovariColors.Slate,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = NovariTypography.bodyLarge,
                color = valueColor,
                fontWeight = valueFontWeight
            )

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}
@Composable
private fun TransactionInfoDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NovariColors.Divider)
    )
}

@Composable
private fun NotesCard(
    note: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NovariColors.Surface)
            .border(
                width = 1.dp,
                color = NovariColors.Border,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            ),
        verticalAlignment = Alignment.Top
    ) {

        Icon(
            imageVector = Icons.Outlined.Notes,
            contentDescription = null,
            tint = NovariColors.Teal,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = note,
            style = NovariTypography.bodyLarge,
            color = NovariColors.Navy
        )
    }
}

@Composable
private fun CategoryIcon(
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(NovariColors.PaleTeal),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = NovariColors.Teal,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun TransactionActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Edit
        Row(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(NovariColors.PaleTeal)
                .border(
                    width = 1.dp,
                    color = NovariColors.Teal,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onEditClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit transaction",
                tint = NovariColors.Teal,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Edit",
                style = NovariTypography.labelLarge,
                color = NovariColors.DarkTeal
            )
        }

        // Delete
        Row(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(NovariColors.ErrorBackground)
                .border(
                    width = 1.dp,
                    color = NovariColors.Error,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onDeleteClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete transaction",
                tint = NovariColors.Error,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Delete",
                style = NovariTypography.labelLarge,
                color = NovariColors.Error
            )
        }
    }
}
