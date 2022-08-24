package com.tomaszrykala.githubbrowser.compose.ui

import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomaszrykala.githubbrowser.compose.TAG
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserComposeTheme

class MainActivityComposer {

    @Composable
    fun GithubBrowser(state: RepoState, controller: RepoController) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(""))
            }

            Column {
                Text(
                    text = "List GitHub Repositories",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(8.dp)
                )

                TextField(
                    value = searchQuery,
                    singleLine = true,
                    onValueChange = { newValue ->
                        searchQuery = newValue
                            .copy(text = newValue.text.replace("\n", ""))
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .onKeyEvent {
                            if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                controller.fetchRepos(searchQuery.text)
                            }
                            false
                        },
                    label = { Text("Organisation") },
                    placeholder = { Text("Search by name") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { controller.fetchRepos(searchQuery.text) }
                    ),
                )

                when (state) {
                    RepoState.InitState -> Unit
                    RepoState.LoadingState -> ShowLoading()
                    is RepoState.ErrorState -> Unit // TODO Show Error
                    is RepoState.ReadyState -> ListRepos(state, controller)
                }
            }
        }
    }

    @Composable
    private fun ShowLoading() {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Searching...",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    private fun ListRepos(state: RepoState.ReadyState, controller: RepoController) {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
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
                    ListItem(repo, controller)
                }
            }
        }
    }

    @Composable
    private fun ListItem(repo: Repository, controller: RepoController) {
        Button(
            onClick = {
                Log.d(TAG, "Open repo: ${repo.url}.")
                controller.openRepo(Uri.parse(repo.url))
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
                state = RepoState.ReadyState(
                    listOf(Repository(1234, "hello world", "www.google.com"))
                ),
                controller = object : RepoController {
                    override fun fetchRepos(search: String) = Unit
                    override fun openRepo(uri: Uri) = Unit
                }
            )
        }
    }
}