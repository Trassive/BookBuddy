package com.example.bookbuddy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.bookbuddy.R
import org.readium.r2.shared.publication.Href
import org.readium.r2.shared.publication.Link
import kotlin.math.max

@Composable
fun ContentComposable(tableOfContent: List<Link>, goTo: (Href)-> Unit){
    Scaffold(){innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding))
        ) {
            items(items = tableOfContent, key = {it.href}){link->
                link.title?.let{
                    OutlinedCard(onClick = { goTo(link.href) }) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = link.title!!,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                modifier = Modifier.padding(
                                    vertical = dimensionResource(id = R.dimen.small_padding),
                                    horizontal = dimensionResource(id = R.dimen.medium_padding)
                                )
                            )
                            Text(
                                text = max(link.children.size,1).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(
                                    vertical = dimensionResource(id = R.dimen.small_padding),
                                    horizontal = dimensionResource(id = R.dimen.medium_padding)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}