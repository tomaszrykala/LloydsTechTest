package com.tomaszrykala.githubbrowser.compose.ui

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomaszrykala.githubbrowser.compose.TAG
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.ui.theme.LloydsTechTestTheme
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserTheme as Theme

class MainActivityComposer {


    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun GithubBrowser(state: RepoState, controller: RepoController) {
        val scaffoldState = rememberScaffoldState()
        Scaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            backgroundColor = MaterialTheme.colors.background,
            topBar = { SearchBar(controller) },
            content = { RepoList(state, controller) },
        )
    }

    @Composable
    private fun SearchBar(controller: RepoController) {
        Column {
            Text(
                text = "List GitHub Repositories",
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(Theme.dimens.spacingStandard)
                    .fillMaxWidth()
            )

            val focusManager = LocalFocusManager.current
            var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(""))
            }

            TextField(
                value = searchQuery,
                singleLine = true,
                onValueChange = { newValue ->
                    searchQuery = newValue
                        .copy(text = newValue.text.replace("\n", ""))
                },
                modifier = Modifier
                    .padding(Theme.dimens.spacingStandard)
                    .fillMaxWidth()
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            controller.searchRepos(searchQuery.text)
                            focusManager.clearFocus()
                        }
                        false
                    },
                label = { Text("Organisation") },
                placeholder = { Text("Search by name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        controller.searchRepos(searchQuery.text)
                        focusManager.clearFocus()
                    }
                ),
            )
        }
    }

    @Composable
    private fun RepoList(state: RepoState, controller: RepoController) {
        Log.d(TAG, "RepoState: $state")
        when (state) {
            RepoState.InitState -> Unit
            RepoState.LoadingState -> ShowLoading()
            is RepoState.ErrorState -> ShowError(state, controller)
            is RepoState.ReadyState -> {
                if (state.repos.isEmpty()) {
                    ShowNoResults()
                } else {
                    ShowResults(state, controller)
                }
            }
        }
    }

    @Composable
    private fun ShowNoResults() {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    "No results!\nTry another org name.",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(Theme.dimens.spacingStandard),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun ShowError(state: RepoState.ErrorState, controller: RepoController) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    "Search failed!",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(Theme.dimens.spacingStandard)
                        .align(Alignment.CenterHorizontally),
                )
                OutlinedButton(
                    onClick = { controller.retrySearch() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .padding(Theme.dimens.spacingStandard)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Retry")
                }
                Text(
                    "Error(s):\n${state.errors.joinToString { "\n" + it }}",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier
                        .padding(Theme.dimens.spacingSemiXXLarge)
                        .align(Alignment.CenterHorizontally)
                )
                // Not the optimal way of displaying errors, but that's detail. The exceptions
                // could be mapped to pre-baked, more meaningful to the user, messages.
            }
        }
    }

    @Composable
    private fun ShowLoading() {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(Theme.dimens.spacingSemiXXLarge)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Searching...",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(Theme.dimens.spacingStandard)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    private fun ShowResults(state: RepoState.ReadyState, controller: RepoController) {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.padding(
                start = Theme.dimens.spacingStandard,
                end = Theme.dimens.spacingStandard
            ),
            contentPadding = PaddingValues(
                horizontal = Theme.dimens.spacingSmall,
                vertical = Theme.dimens.spacingTiny
            ),
            verticalArrangement = Arrangement.spacedBy(Theme.dimens.spacingSmall),
        ) {
            items(state.repos) { repo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Theme.dimens.spacingSmall),
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
                val uri = Uri.parse(repo.url)
                Log.d(TAG, "Open repo: $uri.")
                controller.openRepo(uri)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Starred count",
                    modifier = Modifier.padding(Theme.dimens.spacingTiny)
                )
                Text(
                    repo.totalCount.toString() + " | ",
                    maxLines = 1,
                    modifier = Modifier.padding(Theme.dimens.spacingTiny),
                    style = MaterialTheme.typography.body1,
                )
                Text(
                    repo.name,
                    maxLines = 2,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .padding(Theme.dimens.spacingTiny)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        LloydsTechTestTheme {
            GithubBrowser(
                state = RepoState.InitState,
                controller = object : RepoController {
                    override fun searchRepos(search: String) = Unit
                    override fun openRepo(uri: Uri) = Unit
                    override fun retrySearch() = Unit
                }
            )
        }
    }
}