package com.programmersbox.colorguesser

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            val (circle, colorInfo, guessArea) = createRefs()

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
                    .constrainAs(circle) {
                        top.linkTo(parent.top)
                        bottom.linkTo(colorInfo.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .constrainAs(colorInfo) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(circle.bottom)
                        bottom.linkTo(guessArea.top)
                    }
            ) {
                Spacer(Modifier.size(1.dp))
                AnimatedVisibility(
                    visible = vm.state == GameState.Restart,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val rgb = animateColor.toArgb()
                        Text("RGB: Red = ${rgb.red}, Green = ${rgb.green}, Blue = ${rgb.blue}")
                        Text("Hex: #${Integer.toHexString(rgb).drop(2)}")
                        val cmyk = getCMYKFromRGB(rgb.red, rgb.green, rgb.blue)
                        Text("CMYK: C = ${(cmyk.c * 100).toInt()}, M = ${(cmyk.m * 100).toInt()}, Y = ${(cmyk.y * 100).toInt()}, K = ${(cmyk.k * 100).toInt()}")
                    }
                }
            }

            Column(
                modifier = Modifier.constrainAs(guessArea) {
                    top.linkTo(colorInfo.bottom)
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

                OutlinedTextField(
                    value = vm.hexValue,
                    onValueChange = { vm.hexValue = it },
                    leadingIcon = { Text("#") },
                    label = { Text("Hex Color") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RGB",
                        modifier = Modifier.weight(1f)
                    )

                    NumberOutlinedTextField(
                        value = vm.rValue,
                        onValueChange = { vm.rValue = it.toIntOrNull()?.coerceIn(0, 255)?.toString() ?: "" },
                        label = "R",
                        modifier = Modifier.weight(1f)
                    )

                    NumberOutlinedTextField(
                        value = vm.gValue,
                        onValueChange = { vm.gValue = it.toIntOrNull()?.coerceIn(0, 255)?.toString() ?: "" },
                        label = "G",
                        modifier = Modifier.weight(1f)
                    )

                    NumberOutlinedTextField(
                        value = vm.bValue,
                        onValueChange = { vm.bValue = it.toIntOrNull()?.coerceIn(0, 255)?.toString() ?: "" },
                        label = "B",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CMYK",
                        modifier = Modifier.weight(1f)
                    )

                    NumberOutlinedTextField(
                        value = vm.cValue,
                        onValueChange = { vm.cValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                        label = "C",
                        modifier = Modifier.weight(1f)
                    )

                    NumberOutlinedTextField(
                        value = vm.mValue,
                        onValueChange = { vm.mValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                        label = "M",
                        modifier = Modifier.weight(1f)
                    )

                    NumberOutlinedTextField(
                        value = vm.yValue,
                        onValueChange = { vm.yValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                        label = "Y",
                        modifier = Modifier.weight(1f)
                    )

                    val keyboard = LocalSoftwareKeyboardController.current

                    NumberOutlinedTextField(
                        value = vm.kValue,
                        onValueChange = { vm.kValue = it.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: "" },
                        label = "K",
                        modifier = Modifier.weight(1f),
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