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
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer





@Composable
fun IBichosWelcomeTwoScreen(
    onStartClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F5))
    ) {
        SoftBackgroundGlow()

        FloatingParticles(
            modifier = Modifier.matchParentSize()
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 76.dp)
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 98.dp)
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 0.dp, bottom = 120.dp)
        )

        AnimatedLeafCluster(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 0.dp, bottom = 110.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_oficial),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(305.dp)
                        .blur(18.dp)
                        .background(
                            color = Color(0x22000000),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(272.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            clip = false
                        )
                        .border(
                            width = 4.dp,
                            color = Color(0xFFF4F1D7),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.onboardin_two),
                        contentDescription = "Imagen principal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Completa tu colección",
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF256B35),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "con nuevos bichos",
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF636963),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.74f)
                    .height(72.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(40.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF19BF89),
                                Color(0xFFA9DD2F)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(40.dp)
                    )
                    .clickable { onStartClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = "Comenzar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SoftBackgroundGlow() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawCircle(
            color = Color(0xFFEAF3DE),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.22f, size.height * 0.42f),
            alpha = 0.55f
        )

        drawCircle(
            color = Color(0xFFEFF4E6),
            radius = size.minDimension * 0.31f,
            center = Offset(size.width * 0.82f, size.height * 0.40f),
            alpha = 0.50f
        )
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
            alpha = 0.50f
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
            alpha = 0.40f
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
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clusterFloat"
    )

    val leaf1Rotate by transition.animateFloat(
        initialValue = -26f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf1Rotate"
    )

    val leaf2Rotate by transition.animateFloat(
        initialValue = 18f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf2Rotate"
    )

    val leaf3Rotate by transition.animateFloat(
        initialValue = -34f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf3Rotate"
    )

    val leaf4Rotate by transition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf4Rotate"
    )

    val leaf1Bend by transition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf1Bend"
    )

    val leaf2Bend by transition.animateFloat(
        initialValue = -8f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf2Bend"
    )

    val leaf3Bend by transition.animateFloat(
        initialValue = 5f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf3Bend"
    )

    val leaf4Bend by transition.animateFloat(
        initialValue = -6f,
        targetValue = -13f,
        animationSpec = infiniteRepeatable(
            animation = tween(2700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf4Bend"
    )

    Box(
        modifier = modifier.offset(y = clusterFloat.dp)
    ) {
        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 0.dp, y = 0.dp)
                .size(width = 54.dp, height = 92.dp)
                .graphicsLayer {
                    rotationZ = leaf1Rotate
                    rotationY = leaf1Bend
                    rotationX = -4f
                    scaleX = 1.0f
                    scaleY = 1.04f
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    shadowElevation = 8.dp.toPx()
                },
            baseColor = Color(0xFF6ABB60),
            veinColor = Color(0xFF3F7B39)
        )

        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 30.dp, y = 14.dp)
                .size(width = 46.dp, height = 80.dp)
                .graphicsLayer {
                    rotationZ = leaf2Rotate
                    rotationY = leaf2Bend
                    rotationX = 5f
                    scaleX = 0.98f
                    scaleY = 1.03f
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    shadowElevation = 8.dp.toPx()
                },
            baseColor = Color(0xFF7DCC70),
            veinColor = Color(0xFF4A8A43)
        )

        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 58.dp, y = (-4).dp)
                .size(width = 38.dp, height = 68.dp)
                .graphicsLayer {
                    rotationZ = leaf3Rotate
                    rotationY = leaf3Bend
                    rotationX = -6f
                    scaleX = 0.96f
                    scaleY = 1.02f
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    shadowElevation = 7.dp.toPx()
                },
            baseColor = Color(0xFF95DA82),
            veinColor = Color(0xFF5B9D52)
        )

        Realistic3DLeaf(
            modifier = Modifier
                .offset(x = 12.dp, y = 44.dp)
                .size(width = 34.dp, height = 60.dp)
                .graphicsLayer {
                    rotationZ = leaf4Rotate
                    rotationY = leaf4Bend
                    rotationX = 4f
                    scaleX = 0.95f
                    scaleY = 1.01f
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    shadowElevation = 6.dp.toPx()
                },
            baseColor = Color(0xFF69BE61),
            veinColor = Color(0xFF447E40)
        )
    }
}

@Composable
private fun Realistic3DLeaf(
    modifier: Modifier = Modifier,
    baseColor: Color,
    veinColor: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val leafPath = Path().apply {
            moveTo(w * 0.5f, h * 0.02f)

            cubicTo(
                w * 0.88f, h * 0.10f,
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
                w * 0.12f, h * 0.10f,
                w * 0.5f, h * 0.02f
            )

            close()
        }

        clipPath(leafPath) {
            drawPath(
                path = leafPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.88f),
                        baseColor,
                        baseColor.copy(alpha = 0.94f)
                    ),
                    start = Offset(w * 0.15f, h * 0.08f),
                    end = Offset(w * 0.85f, h)
                )
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.34f, h * 0.24f),
                    radius = w * 0.46f
                ),
                size = size
            )

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.10f),
                        Color.Transparent,
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = w * 0.55f
                ),
                size = size
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.05f),
                        Color.Black.copy(alpha = 0.14f)
                    ),
                    startY = h * 0.38f,
                    endY = h
                ),
                size = size
            )

            for (i in 0..10) {
                val x = w * (0.18f + i * 0.06f)
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = Offset(x, h * 0.12f),
                    end = Offset(x - w * 0.05f, h * 0.90f),
                    strokeWidth = 0.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            for (i in 0..5) {
                val startY = h * (0.18f + i * 0.11f)
                drawLine(
                    color = veinColor.copy(alpha = 0.12f),
                    start = Offset(w * 0.50f, startY),
                    end = Offset(w * 0.72f, startY + h * 0.09f),
                    strokeWidth = 0.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            for (i in 0..5) {
                val startY = h * (0.18f + i * 0.11f)
                drawLine(
                    color = veinColor.copy(alpha = 0.12f),
                    start = Offset(w * 0.50f, startY),
                    end = Offset(w * 0.28f, startY + h * 0.09f),
                    strokeWidth = 0.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            drawPath(
                path = Path().apply {
                    moveTo(w * 0.46f, h * 0.10f)
                    quadraticBezierTo(w * 0.56f, h * 0.45f, w * 0.50f, h * 0.88f)
                },
                color = Color.White.copy(alpha = 0.10f),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        drawPath(
            path = leafPath,
            color = baseColor.copy(alpha = 0.95f),
            style = Stroke(width = 1.1.dp.toPx())
        )

        drawLine(
            color = veinColor,
            start = Offset(w * 0.5f, h * 0.08f),
            end = Offset(w * 0.5f, h * 0.92f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        fun curvedVein(startY: Float, endX: Float, endY: Float, curve: Float) {
            val path = Path().apply {
                moveTo(w * 0.5f, startY)
                quadraticBezierTo(
                    w * curve, startY + (endY - startY) * 0.3f,
                    w * endX, endY
                )
            }

            drawPath(
                path = path,
                color = veinColor.copy(alpha = 0.55f),
                style = Stroke(
                    width = 0.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        curvedVein(h * 0.22f, 0.75f, h * 0.34f, 0.62f)
        curvedVein(h * 0.34f, 0.78f, h * 0.50f, 0.65f)
        curvedVein(h * 0.48f, 0.74f, h * 0.66f, 0.63f)

        curvedVein(h * 0.22f, 0.25f, h * 0.34f, 0.38f)
        curvedVein(h * 0.34f, 0.22f, h * 0.50f, 0.35f)
        curvedVein(h * 0.48f, 0.26f, h * 0.66f, 0.37f)

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(w * 0.36f, h * 0.20f),
                radius = w * 0.18f
            ),
            topLeft = Offset(w * 0.22f, h * 0.10f),
            size = Size(w * 0.25f, h * 0.14f)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewScreen() {
    IBichosWelcomeTwoScreen()
}
