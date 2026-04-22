package com.equimaxlab.solver.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.core.Rank
import com.equimaxlab.solver.core.Suit
import com.equimaxlab.solver.ui.theme.GreenFelt
import com.equimaxlab.solver.ui.theme.TextSecondary

@Composable
fun CardPickerDialog(
    blockedCards: List<Card> = emptyList(),
    onCardSelected: (Card) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GreenFelt,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text     = "SELECCIONA UNA CARTA",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Suit.entries.forEach { suit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Rank.entries.forEach { rank ->
                                val card    = Card(rank, suit)
                                val blocked = card in blockedCards
                                if (blocked) {
                                    EmptyCardSlot(size = CardSize.SMALL)
                                } else {
                                    CardView(
                                        card    = card,
                                        size    = CardSize.SMALL,
                                        onClick = {
                                            onCardSelected(card)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    }
}