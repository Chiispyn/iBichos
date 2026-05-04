package com.cetecom.ibichos.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cetecom.ibichos.R
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer





@Composable
fun IBichosWelcomeScreen(
    onStartClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F5))
    ) {
        FloatingParticles(
            modifier = Modifier.matchParentSize()
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 26.dp, top = 108.dp)
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 132.dp)
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, bottom = 168.dp)
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 182.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
                .size(360.dp)
                .blur(32.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFDFF6E1),
                            Color(0x88EEF9EF),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 55.dp)
                .size(width = 360.dp, height = 220.dp)
                .blur(36.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFDFF5E6),
                            Color(0x88EEF9EF),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(140.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_oficial),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .blur(25.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE8F9E9),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_boy_bug),
                        contentDescription = "Niño explorador",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color(0x66FFFFFF),
                                        Color(0xCCFFFFFF),
                                        Color(0xFFF7F7F5)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Bienvenido a iBichos",
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78A76D),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Descubre el mundo de los insectos,\ncaza, colecciona y explora.",
                fontSize = 15.sp,
                lineHeight = 27.sp,
                color = Color(0xFF8A8A8A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(52.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(58.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF43B78E),
                                Color(0xFFBCE44D)
                            )
                        )
                    )
                    .clickable {
                        onStartClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Comenzar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FloatingParticles(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val p1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )

    val p2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    val p3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p3"
    )

    val p4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p4"
    )

    Box(modifier = modifier) {
        Particle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 70.dp, y = (160 + p1).dp),
            size = 8.dp,
            alpha = 0.55f
        )

        Particle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-82).dp, y = (190 + p2).dp),
            size = 10.dp,
            alpha = 0.45f
        )

        Particle(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 38.dp, y = (-40 + p3).dp),
            size = 6.dp,
            alpha = 0.5f
        )

        Particle(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-30).dp, y = (-10 + p4).dp),
            size = 7.dp,
            alpha = 0.35f
        )

        Particle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 90.dp, y = (-180 + p2).dp),
            size = 9.dp,
            alpha = 0.35f
        )

        Particle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-85).dp, y = (-210 + p1).dp),
            size = 8.dp,
            alpha = 0.4f
        )
    }
}

@Composable
private fun Particle(
    modifier: Modifier = Modifier,
    size: Dp,
    alpha: Float
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = alpha))
    )
}

@Composable
private fun AnimatedLeafCluster(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "leaf_cluster")

    val clusterFloat by transition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clusterFloat"
    )

    val leaf1Rotate by transition.animateFloat(
        initialValue = -24f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf1Rotate"
    )

    val leaf2Rotate by transition.animateFloat(
        initialValue = 16f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf2Rotate"
    )

    val leaf3Rotate by transition.animateFloat(
        initialValue = -34f,
        targetValue = -22f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf3Rotate"
    )

    val leaf4Rotate by transition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf4Rotate"
    )

    val leaf1Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf1Y"
    )

    val leaf2Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf2Y"
    )

    val leaf3Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf3Y"
    )

    val leaf4Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf4Y"
    )

    Box(
        modifier = modifier.offset(y = clusterFloat.dp)
    ) {
        Realistic3DLeaf(
            modifier = Modifier
                .offset(y = leaf1Y.dp)
                .size(width = 30.dp, height = 54.dp)
                .graphicsLayer {
                    rotationZ = leaf1Rotate
                    shadowElevation = 10.dp.toPx()
                },
            baseColor = Color(0xFF5FAF5C),
            veinColor = Color(0xFF3F7C3D)
        )

        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 20.dp, y = (12f + leaf2Y).dp)
                .size(width = 28.dp, height = 48.dp)
                .graphicsLayer {
                    rotationZ = leaf2Rotate
                    shadowElevation = 9.dp.toPx()
                },
            baseColor = Color(0xFF74C66A),
            veinColor = Color(0xFF4A8C45)
        )

        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 40.dp, y = (-4f + leaf3Y).dp)
                .size(width = 24.dp, height = 42.dp)
                .graphicsLayer {
                    rotationZ = leaf3Rotate
                    shadowElevation = 8.dp.toPx()
                },
            baseColor = Color(0xFF8CD77C),
            veinColor = Color(0xFF5D9E55)
        )

        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 12.dp, y = (30f + leaf4Y).dp)
                .size(width = 22.dp, height = 38.dp)
                .graphicsLayer {
                    rotationZ = leaf4Rotate
                    shadowElevation = 7.dp.toPx()
                },
            baseColor = Color(0xFF6DBB63),
            veinColor = Color(0xFF447F42)
        )
    }
}

@Composable
private fun Realistic3DLeaf(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF6FBE63),
    veinColor: Color = Color(0xFF4D8A45)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val leafPath = Path().apply {
            moveTo(w * 0.50f, h * 0.02f)

            cubicTo(
                w * 0.90f, h * 0.12f,
                w * 0.98f, h * 0.42f,
                w * 0.62f, h * 0.96f
            )

            cubicTo(
                w * 0.54f, h * 1.02f,
                w * 0.46f, h * 1.02f,
                w * 0.38f, h * 0.96f
            )

            cubicTo(
                w * 0.02f, h * 0.42f,
                w * 0.10f, h * 0.12f,
                w * 0.50f, h * 0.02f
            )

            close()
        }

        drawPath(
            path = leafPath,
            color = Color.Black.copy(alpha = 0.16f)
        )

        drawPath(
            path = leafPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.82f),
                    baseColor,
                    baseColor.copy(alpha = 0.95f),
                    baseColor.copy(alpha = 0.72f)
                ),
                start = Offset(w * 0.15f, h * 0.10f),
                end = Offset(w * 0.85f, h)
            )
        )

        drawPath(
            path = leafPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(w * 0.35f, h * 0.28f),
                radius = w * 0.45f
            )
        )

        drawPath(
            path = leafPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.10f)
                ),
                startY = h * 0.45f,
                endY = h
            )
        )

        drawPath(
            path = leafPath,
            color = baseColor.copy(alpha = 0.95f),
            style = Stroke(width = 1.2.dp.toPx())
        )

        drawLine(
            color = veinColor,
            start = Offset(w * 0.50f, h * 0.08f),
            end = Offset(w * 0.50f, h * 0.92f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = veinColor.copy(alpha = 0.75f),
            start = Offset(w * 0.50f, h * 0.22f),
            end = Offset(w * 0.72f, h * 0.34f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = veinColor.copy(alpha = 0.75f),
            start = Offset(w * 0.50f, h * 0.34f),
            end = Offset(w * 0.76f, h * 0.50f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = veinColor.copy(alpha = 0.75f),
            start = Offset(w * 0.50f, h * 0.48f),
            end = Offset(w * 0.72f, h * 0.66f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = veinColor.copy(alpha = 0.75f),
            start = Offset(w * 0.50f, h * 0.22f),
            end = Offset(w * 0.28f, h * 0.34f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = veinColor.copy(alpha = 0.75f),
            start = Offset(w * 0.50f, h * 0.34f),
            end = Offset(w * 0.24f, h * 0.50f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = veinColor.copy(alpha = 0.75f),
            start = Offset(w * 0.50f, h * 0.48f),
            end = Offset(w * 0.28f, h * 0.66f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                center = Offset(w * 0.38f, h * 0.22f),
                radius = w * 0.22f
            ),
            topLeft = Offset(w * 0.22f, h * 0.12f),
            size = Size(w * 0.28f, h * 0.16f)
        )

        drawOval(
            color = Color.Black.copy(alpha = 0.10f),
            topLeft = Offset(w * 0.42f, h * 0.70f),
            size = Size(w * 0.20f, h * 0.10f)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IBichosWelcomeScreenPreview() {
    IBichosWelcomeScreen()
}

