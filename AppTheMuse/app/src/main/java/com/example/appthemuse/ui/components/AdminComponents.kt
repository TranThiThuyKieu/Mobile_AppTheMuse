package com.example.appthemuse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appthemuse.ui.model.AdminBookUi

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label) })
}

@Composable
fun AdminBookRow(
    book: AdminBookUi,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Tac gia: ${book.authorId}", style = MaterialTheme.typography.bodySmall)
                }
                StatusChip(label = book.statusLabel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Luot xem: ${book.viewCountText}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Chuong: ${book.chapterCountText}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Sao: ${book.ratingText}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row {
                OutlinedButton(onClick = onApprove, enabled = book.statusValue == "pending") {
                    Text("Duyet")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onHide, enabled = book.statusValue != "hidden") {
                    Text("An")
                }
            }
        }
    }
}
