package com.tomaszrykala.githubbrowser.compose

import android.os.Bundle
import android.view.KeyEvent.KEYCODE_ENTER
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ReposViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {

        val repos: List<Repository> by viewModel.repositories
        val state: RepoState by viewModel.state

        setContent {
            GithubBrowserComposeTheme {
                GithubBrowser(viewModel, repos, state)
            }
        }
    }

    override fun onStart() = super.onStart().also {
        viewModel.onStart()
    }

    override fun onStop() = super.onStop().also {
        viewModel.onStop()
    }
}

@Composable
fun GithubBrowser(
    viewModel: IReposViewModel,
    repos: List<Repository> = emptyList(),
    state: RepoState = RepoState.OkState,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {

        var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(""))
        }
        Column {
            Text(
                text = "List GitHub Repositories",
                style = typography.h6,
                modifier = Modifier.padding(8.dp)
            )

            TextField(
                value = text,
                singleLine = true,
                onValueChange = { newValue ->
                    text = newValue
                        .copy(text = newValue.text.replace("\n", ""))
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == KEYCODE_ENTER) {
                            viewModel.fetchRepos(text.text)
                        }
                        false
                    },
                label = { Text("Organisation") },
                placeholder = { Text("Search by name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.fetchRepos(text.text) }
                ),
            )

            LazyColumn(
                modifier = Modifier.padding(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(repos) { repo ->
                    Text(
                        repo.name,
                        // modifier = Modifier.padding(2.dp)
                    )
                }
            }

            when (state) {
                is RepoState.ErrorState -> Unit // show error
                is RepoState.LoadingState -> Unit // show loading
                is RepoState.OkState -> Unit // dismiss stuff
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GithubBrowserComposeTheme {
        GithubBrowser(object : IReposViewModel {
            override fun fetchRepos(search: String) = Unit
            override fun onStart() = Unit
            override fun onStop() = Unit
        })
    }
}