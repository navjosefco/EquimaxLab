package com.equimaxlab.solver.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equimaxlab.solver.domain.model.Position
import com.equimaxlab.solver.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun PokerTable(
    heroPosition: Position,
    activePositions: Set<Position> = Position.entries.toSet(),
    modifier: Modifier = Modifier,
    tableSize: Dp = 280.dp
) {
    Box(
        modifier = modifier.size(tableSize),
        contentAlignment = Alignment.Center
    ) {
        // Mesa
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val padding = 40f

            // Fieltro exterior
            drawRoundRect(
                color       = Color(0xFF0A2014),
                topLeft     = Offset(padding * 0.3f, padding * 0.3f),
                size        = Size(w - padding * 0.6f, h - padding * 0.6f),
                cornerRadius = CornerRadius(h / 2.2f),
            )

            // Fieltro verde interior
            drawRoundRect(
                color        = Color(0xFF143622),
                topLeft      = Offset(padding, padding),
                size         = Size(w - padding * 2, h - padding * 2),
                cornerRadius = CornerRadius(h / 2.5f)
            )

            // Borde dorado
            drawRoundRect(
                color        = Color(0xFF2A5A35),
                topLeft      = Offset(padding, padding),
                size         = Size(w - padding * 2, h - padding * 2),
                cornerRadius = CornerRadius(h / 2.5f),
                style        = Stroke(width = 2f)
            )
        }

        // Posiciones alrededor de la mesa
        Position.entries.forEach { position ->
            val angle    = positionAngle(position)
            val radiusX  = tableSize.value * 0.42f
            val radiusY  = tableSize.value * 0.36f
            val centerX  = tableSize.value / 2f
            val centerY  = tableSize.value / 2f

            val angleRad = angle.toDouble() * kotlin.math.PI / 180.0
            val x = centerX + radiusX * cos(angleRad).toFloat()
            val y = centerY + radiusY * sin(angleRad).toFloat()
            val isHero   = position == heroPosition
            val isActive = position in activePositions

            Box(
                modifier = Modifier
                    .offset(
                        x = (x - tableSize.value / 2f).dp - 20.dp,
                        y = (y - tableSize.value / 2f).dp - 14.dp
                    )
                    .size(40.dp, 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isHero   -> GreenAccent
                        isActive -> Color(0xFF1F4A2E)
                        else     -> Color(0xFF0D2B1A)
                    },
                    border = if (isHero)
                        androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF27AE60))
                    else if (isActive)
                        androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                    else null
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text       = position.shortName(),
                            color      = when {
                                isHero   -> Color.Black
                                isActive -> TextPrimary
                                else     -> TextSecondary.copy(alpha = 0.4f)
                            },
                            fontSize   = 9.sp,
                            fontWeight = if (isHero) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Dealer button
                if (isHero) {
                    Surface(
                        shape    = CircleShape,
                        color    = GoldAccent,
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("D", color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Texto central
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = heroPosition.shortName(),
                color      = GreenAccent,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text     = "tu posición",
                color    = TextSecondary,
                fontSize = 9.sp
            )
        }
    }
}

private fun positionAngle(position: Position): Float = when (position) {
    Position.UTG         -> 150f
    Position.UTG1        -> 120f
    Position.UTG2        -> 90f
    Position.LOJACK      -> 60f
    Position.HIJACK      -> 30f
    Position.CUTOFF      -> 350f
    Position.BUTTON      -> 320f
    Position.SMALL_BLIND -> 270f
    Position.BIG_BLIND   -> 220f
}

private fun Position.shortName() = when (this) {
    Position.UTG         -> "UTG"
    Position.UTG1        -> "UTG+1"
    Position.UTG2        -> "UTG+2"
    Position.LOJACK      -> "LJ"
    Position.HIJACK      -> "HJ"
    Position.CUTOFF      -> "CO"
    Position.BUTTON      -> "BTN"
    Position.SMALL_BLIND -> "SB"
    Position.BIG_BLIND   -> "BB"
}