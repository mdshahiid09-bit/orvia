package com.orvia.shahid.app.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandBlue
import com.orvia.shahid.app.ui.theme.BrandCyan
import com.orvia.shahid.app.ui.theme.BrandPrimary

@Composable
fun AnimatedOrb(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransition")

    val rotation by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "OrbRotation"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val pulse by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "OrbPulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = (this.size.minDimension / 2.4f) * pulse

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BrandAccent.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = center
        )

        // Center radiant core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BrandPrimary, BrandBlue, Color(0xFF130E26)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Rotating orbital ring 1
        rotate(rotation, pivot = center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(BrandCyan, BrandPrimary, BrandBlue, BrandCyan),
                    center = center
                ),
                radius = radius * 1.15f,
                center = center,
                style = Stroke(width = 3.5f)
            )
        }

        // Counter-rotating orbital ring 2
        rotate(-rotation * 0.75f, pivot = center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(BrandAccent, Color.Transparent, BrandCyan, BrandAccent),
                    center = center
                ),
                radius = radius * 1.3f,
                center = center,
                style = Stroke(width = 2f)
            )
        }
    }
}
