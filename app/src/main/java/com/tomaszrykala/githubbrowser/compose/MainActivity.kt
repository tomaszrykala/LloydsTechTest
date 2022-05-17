package com.tomaszrykala.githubbrowser.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tomaszrykala.githubbrowser.compose.repository.ReposState
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ReposViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.repositories.observe(this) { list ->
            list.forEach {
                println("CSQ Repo name: ${it.name}, totalCount: ${it.totalCount}, url: ${it.url}")
            }
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is ReposState.ErrorState -> Unit // show error
                else -> Unit // dismiss errors
            }
        }

        setContent {
            GithubBrowserComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchRepos()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GithubBrowserComposeTheme {
        Greeting("Android")
    }
}