package com.programmersbox.colorguesser

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.colorguesser.ui.theme.ColorGuesserTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val vm: ColorViewModel = viewModel()
            val animateColor by animateColorAsState(targetValue = vm.color)

            ColorGuesserTheme(
                customPrimaryColor = animateColor
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { ColorGuesserView(vm) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ColorGuesserView(vm: ColorViewModel = viewModel()) {

    val animateColor by animateColorAsState(targetValue = vm.color)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    LaunchedEffect(vm.state) {
        if (vm.state == GameState.Restart) drawerState.open()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = vm.state == GameState.Restart,
        drawerContent = {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = {},
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = animateColor)
                    )
                }
            ) { p ->
                val rgb = animateColor.toArgb()
                val cmyk = getCMYKFromRGB(rgb.red, rgb.green, rgb.blue)
                LazyColumn(
                    contentPadding = p,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    item {
                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text("Actual Values")
                            Text("RGB: Red = ${rgb.red}, Green = ${rgb.green}, Blue = ${rgb.blue}")
                            Text("Hex: #${Integer.toHexString(rgb).drop(2)}")
                            Text("CMYK: C = ${(cmyk.c * 100).toInt()}, M = ${(cmyk.m * 100).toInt()}, Y = ${(cmyk.y * 100).toInt()}, K = ${(cmyk.k * 100).toInt()}")
                        }
                    }

                    item { Text("Your Guesses", modifier = Modifier.padding(start = 14.dp)) }

                    vm.hexColor?.let {
                        item {
                            SmallTopAppBar(
                                title = {},
                                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = it)
                            )
                            Column(modifier = Modifier.padding(start = 14.dp)) {
                                Text("Actual Hex: #${Integer.toHexString(rgb).drop(2)}")
                                Text("Hex: #${vm.hexValue}")
                                Text("Hex Score: ${vm.hexScore(rgb)}")
                            }
                        }
                    }

                    vm.rgbColor?.let {
                        item {
                            SmallTopAppBar(
                                title = {},
                                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = it)
                            )

                            Column(modifier = Modifier.padding(start = 14.dp)) {
                                Text("Actual RGB: Red = ${rgb.red}, Green = ${rgb.green}, Blue = ${rgb.blue}")
                                Text("R: ${vm.rValue}")
                                Text("G: ${vm.gValue}")
                                Text("B: ${vm.bValue}")
                                Text("RGB Score: ${vm.rgbScore(rgb)}")
                            }
                        }
                    }

                    vm.cmykColor?.let {
                        item {
                            SmallTopAppBar(
                                title = {},
                                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = it)
                            )

                            Column(modifier = Modifier.padding(start = 14.dp)) {
                                Text("Actual CMYK: C = ${(cmyk.c * 100).toInt()}, M = ${(cmyk.m * 100).toInt()}, Y = ${(cmyk.y * 100).toInt()}, K = ${(cmyk.k * 100).toInt()}")
                                Text("C: ${vm.cValue}")
                                Text("M: ${vm.mValue}")
                                Text("Y: ${vm.yValue}")
                                Text("K: ${vm.kValue}")
                                Text("CMYK Score: ${vm.cmykScore(rgb)}")
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("Color Guesser") }
                )
            },
            bottomBar = {
                BottomAppBar(
                    icons = {
                        Text("Score: ${animateIntAsState(vm.currentScore).value}")
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = vm::reset,
                            containerColor = MaterialTheme.colorScheme.primary
                        ) { Icon(Icons.Default.Refresh, null) }
                    }
                )
            }
        ) { p ->
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(p)
                    .padding(horizontal = 4.dp)
            ) {
                val (circle, guessArea) = createRefs()

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                        .constrainAs(circle) {
                            top.linkTo(parent.top)
                            bottom.linkTo(guessArea.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )

                Column(
                    modifier = Modifier.constrainAs(guessArea) {
                        top.linkTo(circle.bottom)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                ) {

                    val buttonState = when (vm.state) {
                        GameState.Play -> "Make a Guess" to vm::guess
                        GameState.Restart -> "Play Again" to vm::reset
                    }
                    OutlinedButton(
                        onClick = buttonState.second,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) { Text(buttonState.first) }

                    val hexValues = "0123456789ABCDEFabcdef".toCharArray()

                    OutlinedTextField(
                        value = vm.hexValue,
                        onValueChange = {
                            vm.hexValue = if (it.length <= 6 && it.lastOrNull()?.let { it1 -> hexValues.contains(it1) } == true)
                                it
                            else
                                vm.hexValue
                        },
                        leadingIcon = { Text("#") },
                        label = { Text("Hex Color") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "RGB",
                            modifier = Modifier.weight(1f)
                        )

                        NumberOutlinedTextField(
                            value = vm.rValue,
                            onValueChange = { vm.rValue = it.toIntOrNull()?.coerceIn(0, 255)?.toString() ?: "" },
                            label = "R",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                        )

                        NumberOutlinedTextField(
                            value = vm.gValue,
                            onValueChange = { vm.gValue = it.toIntOrNull()?.coerceIn(0, 255)?.toString() ?: "" },
                            label = "G",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                        )

                        NumberOutlinedTextField(
                            value = vm.bValue,
                            onValueChange = { vm.bValue = it.toIntOrNull()?.coerceIn(0, 255)?.toString() ?: "" },
                            label = "B",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "CMYK",
                            modifier = Modifier.weight(1f)
                        )

                        NumberOutlinedTextField(
                            value = vm.cValue,
                            onValueChange = { vm.cValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                            label = "C",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                        )

                        NumberOutlinedTextField(
                            value = vm.mValue,
                            onValueChange = { vm.mValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                            label = "M",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                        )

                        NumberOutlinedTextField(
                            value = vm.yValue,
                            onValueChange = { vm.yValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                            label = "Y",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                        )

                        val keyboard = LocalSoftwareKeyboardController.current

                        NumberOutlinedTextField(
                            value = vm.kValue,
                            onValueChange = { vm.kValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                            label = "K",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp),
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = { keyboard?.hide() }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = keyboardActions,
        modifier = modifier
    )
}

fun Random.nextColor(
    r: Int = nextInt(0, 255),
    g: Int = nextInt(0, 255),
    b: Int = nextInt(0, 255),
    a: Int = nextInt(0, 255)
) = Color(r, g, b, a)

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    ColorGuesserTheme {
        ColorGuesserView()
    }
}