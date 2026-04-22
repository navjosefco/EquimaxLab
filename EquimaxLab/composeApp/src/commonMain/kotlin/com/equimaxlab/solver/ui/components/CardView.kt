package com.equimaxlab.solver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.core.Rank
import com.equimaxlab.solver.core.Suit
import com.equimaxlab.solver.ui.theme.CardWhite
import com.equimaxlab.solver.ui.theme.DividerColor
import com.equimaxlab.solver.ui.theme.GreenFeltLight
import com.equimaxlab.solver.ui.theme.RedAccent

val suitSymbol: (Suit) -> String = { suit ->
    when (suit) {
        Suit.CLUBS    -> "♣"
        Suit.DIAMONDS -> "♦"
        Suit.HEARTS   -> "♥"
        Suit.SPADES   -> "♠"
    }
}

val suitColor: (Suit) -> Color = { suit ->
    when (suit) {
        Suit.HEARTS, Suit.DIAMONDS -> RedAccent
        Suit.CLUBS, Suit.SPADES    -> Color(0xFF1A1A1A)
    }
}

val rankLabel: (Rank) -> String = { rank ->
    when (rank) {
        Rank.TEN   -> "T"
        Rank.JACK  -> "J"
        Rank.QUEEN -> "Q"
        Rank.KING  -> "K"
        Rank.ACE   -> "A"
        else       -> rank.value.toString()
    }
}

@Composable
fun CardView(
    card: Card,
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(size.cornerRadius)
    Box(
        modifier = modifier
            .size(width = size.width, height = size.height)
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(CardWhite)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = rankLabel(card.rank),
                color      = suitColor(card.suit),
                fontSize   = size.rankFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = size.rankFontSize * 1.1f
            )
            Text(
                text     = suitSymbol(card.suit),
                color    = suitColor(card.suit),
                fontSize = size.suitFontSize,
                lineHeight = size.suitFontSize
            )
        }
    }
}

@Composable
fun EmptyCardSlot(
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(size.cornerRadius)
    Box(
        modifier = modifier
            .size(width = size.width, height = size.height)
            .clip(shape)
            .background(GreenFeltLight)
            .border(width = 1.dp, color = DividerColor, shape = shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "+",
            color     = DividerColor,
            fontSize  = size.rankFontSize,
            textAlign = TextAlign.Center
        )
    }
}

enum class CardSize(
    val width: Dp,
    val height: Dp,
    val cornerRadius: Dp,
    val rankFontSize: androidx.compose.ui.unit.TextUnit,
    val suitFontSize: androidx.compose.ui.unit.TextUnit
) {
    SMALL(40.dp, 54.dp, 4.dp, 14.sp, 11.sp),
    MEDIUM(56.dp, 76.dp, 6.dp, 20.sp, 15.sp),
    LARGE(72.dp, 98.dp, 8.dp, 26.sp, 20.sp)
}