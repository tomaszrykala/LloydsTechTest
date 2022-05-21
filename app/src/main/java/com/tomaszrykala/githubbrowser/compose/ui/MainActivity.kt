package com.tomaszrykala.githubbrowser.compose.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent.KEYCODE_ENTER
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomaszrykala.githubbrowser.compose.IReposViewModel
import com.tomaszrykala.githubbrowser.compose.ReposViewModel
import com.tomaszrykala.githubbrowser.compose.TAG
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ReposViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {

        val state: RepoState by viewModel.state

        setContent {
            GithubBrowserComposeTheme {
                GithubBrowser(viewModel, state)
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
    state: RepoState = RepoState.InitState,
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

            if (state is RepoState.ReadyState) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(state.repos) { repo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            ListItem(repo, viewModel)
                        }
                    }
                }
            }

            when (state) {
                RepoState.InitState -> Unit
                RepoState.LoadingState -> Unit // show loading
                is RepoState.ErrorState -> Unit // show error
                is RepoState.ReadyState -> Unit // dismiss stuff
            }
        }
    }
}

@Composable
private fun ListItem(
    repo: Repository,
    viewModel: IReposViewModel
) {
    val context = LocalContext.current

    Button(
        onClick = {
            Log.d(TAG, "Open repo: ${repo.url}.")
            viewModel.openRepo(Uri.parse(repo.url), context)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Starred count",
                modifier = Modifier.padding(2.dp)
            )
            Text(
                repo.totalCount.toString() + " | ",
                maxLines = 1,
                fontSize = 16.sp,
                modifier = Modifier.padding(2.dp)
            )
            Text(
                repo.name,
                maxLines = 2,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GithubBrowserComposeTheme {
        GithubBrowser(
            viewModel = object : IReposViewModel {
                override fun fetchRepos(search: String) = Unit
                override fun openRepo(uri: Uri, context: Context) = Unit
                override fun onStart() = Unit
                override fun onStop() = Unit
            },
            state = RepoState.ReadyState(
                listOf(Repository(1234, "hello world", "www.google.com"))
            )
        )
    }
}