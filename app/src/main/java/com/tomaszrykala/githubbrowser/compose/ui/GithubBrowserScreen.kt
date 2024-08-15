package com.tomaszrykala.githubbrowser.compose.ui

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tomaszrykala.githubbrowser.compose.TAG
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserTheme as Theme

//@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun GithubBrowser(
//    state: RepoState,
//    controller: RepoController
    viewModel: ReposViewModel,
    modifier: Modifier = Modifier,
) {
    val state: RepoState by viewModel.state.collectAsState()

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colors.background,
        topBar = { SearchBar(viewModel::searchRepos) },
//        content = {
//            RepoList(
//                state = state,
//                onRetry = viewModel::retrySearch,
//                onRepoSelected = viewModel::openRepo
//            )
//        },
    )  { innerPadding ->
        RepoList(
            modifier = Modifier.padding(innerPadding),
            state = state,
            onRetry = viewModel::retrySearch,
            onRepoSelected = viewModel::openRepo
        )
    }
}

@Composable
private fun SearchBar(
    searchRepos: (String) -> Unit,
) {
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
                        // controller.searchRepos(searchQuery.text)
                        searchRepos(searchQuery.text)
                        focusManager.clearFocus()
                    }
                    false
                },
            label = { Text("Organisation") },
            placeholder = { Text("Search by name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    // controller.searchRepos(searchQuery.text)
                    searchRepos(searchQuery.text)
                    focusManager.clearFocus()
                }
            ),
        )
    }
}

@Composable
private fun RepoList(
    modifier: Modifier,
    state: RepoState,
    onRetry: () -> Unit,
    onRepoSelected: (uri: Uri, context: Context) -> Unit,
) {
    Log.d(TAG, "RepoState: $state")
    when (state) {
        RepoState.InitState -> Unit
        RepoState.LoadingState -> ShowLoading()
        is RepoState.ErrorState -> ShowError(modifier, state, onRetry)
        is RepoState.ReadyState -> {
            if (state.repos.isEmpty()) {
                ShowNoResults(modifier)
            } else {
                ShowResults(modifier, state, onRepoSelected)
            }
        }
    }
}

@Composable
private fun ShowNoResults(modifier: Modifier,) {
    Box(modifier = modifier.fillMaxSize()) {
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
private fun ShowError(
    modifier: Modifier,
    state: RepoState.ErrorState,
    onRetry: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                "Search failed!",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .padding(Theme.dimens.spacingStandard)
                    .align(Alignment.CenterHorizontally),
            )
            OutlinedButton(
                onClick = onRetry,
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
private fun ShowResults(
    modifier: Modifier,
    state: RepoState.ReadyState,
    onRepoSelected: (uri: Uri, context: Context) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.padding(
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
                ListItem(repo, onRepoSelected)
            }
        }
    }
}

@Composable
private fun ListItem(
    repo: Repository,
    onRepoSelected: (uri: Uri, context: Context) -> Unit,
) {
    val context = LocalContext.current
    Button(
        onClick = {
            val uri = Uri.parse(repo.url)
            // controller.openRepo(uri, context)
            onRepoSelected(uri, context)
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

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    LloydsTechTestTheme {
//        GithubBrowser(
//            state = RepoState.InitState,
//            controller = object : RepoController {
//                override fun searchRepos(search: String) = Unit
//                override fun openRepo(uri: Uri) = Unit
//                override fun retrySearch() = Unit
//            }
//        )
//    }
//}