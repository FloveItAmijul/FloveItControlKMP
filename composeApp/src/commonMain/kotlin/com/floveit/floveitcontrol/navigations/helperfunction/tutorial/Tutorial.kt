package com.floveit.floveitcontrol.navigations.helperfunction.tutorial

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.tuto_indicator
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tutorial(
    modifier: Modifier = Modifier,
    pages: List<TutorialPage>,
    currentPage: Int = 0,
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val current = pagerState.currentPage
    val scale = remember { Animatable(1f) }

    var showAnimation by remember { mutableStateOf(true) }

    suspend fun animatedImage() {
        scale.animateTo(0.7f , animationSpec = tween(durationMillis = 700))
        scale.animateTo(1f , animationSpec = tween(durationMillis = 700))

    }



    LaunchedEffect(Unit) {
        while(showAnimation){
            animatedImage()
        }

    }

    BoxWithConstraints(modifier
        .fillMaxSize()
        .padding( top = if(isAndroid()) 10.dp else 15.dp,
            start = if(isAndroid()) 10.dp else 15.dp,
            end = if(isAndroid()) 10.dp else 15.dp,
            bottom = if(isAndroid()) 25.dp else 40.dp
        )

    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight


        val targetOffsetX = when(current) {
            0 -> if(isAndroid()) 150.dp else (maxWidth * 47 / 100)
            1 -> if(isAndroid()) 230.dp else (maxWidth * 67 / 100)
            2 -> if(isAndroid()) 100.dp else (maxWidth * 34 / 100)
            else -> 0.dp
        }

        val targetOffsetY = when(current) {
            0 -> if(isAndroid()) 100.dp else 120.dp
            1 -> if(isAndroid()) 75.dp else 70.dp
            2 -> if(isAndroid()) 160.dp else (maxHeight * 22 / 100)
            else -> 0.dp
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {



            // 1) Top row: back button
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (current > 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.clickable {
                            scope.launch { pagerState.animateScrollToPage(current - 1) }
                            onBack()
                        }
                    )
                } else {
                    Box{}
                }

                TextButton(onClick = { onFinish()}) {
                    Text("Skip", color = Color.White)
                }

            }
            Spacer(Modifier.height((maxWidth * 10 / 100)))
            // 3) Image pager, occupies the rest
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                        // 1) Top: description text for this page
                        Text(
                            text = pages[page].description,
                            style = MaterialTheme.typography.body1.copy(fontSize = 28.sp , lineHeight = 40.sp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(24.dp))
                        Box(modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1f)
                        ){



                            Image(
                                painter = painterResource(pages[page].image),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .matchParentSize()
                                    .aspectRatio(1f)

                            )

                            Image(
                                painter = painterResource(Res.drawable.tuto_indicator),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(50.dp)
                                    .aspectRatio(1f)
                                    .offset( x = targetOffsetX , y = targetOffsetY)
                                    .graphicsLayer {
                                        scaleX = scale.value
                                        scaleY = scale.value
                                    }
                            )
                        }

                    }

                }
            }

            // 4) Next / Get Started button
            Button(
                onClick = {
                    if (current < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(current + 1) }
                        onNext()
                    } else {
                        showAnimation = false
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color(0xFF4D2D44)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( 10.dp)
            ) {
                Text("Next" , color = Color.White , fontSize = 22.sp)
            }
        }
    }

}


