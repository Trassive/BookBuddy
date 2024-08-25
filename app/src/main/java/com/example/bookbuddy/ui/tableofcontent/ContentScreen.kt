package com.example.bookbuddy.ui.tableofcontent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.bookbuddy.R
import org.readium.r2.shared.publication.Href
import org.readium.r2.shared.publication.Link
import kotlin.text.Typography.bullet

@Composable
fun TableOfContentScreen(tableOfContent: List<Link>, goTo: (Href)-> Unit){
    Scaffold(
        topBar = {

        }
    ) {innerPadding->
        Content(tableOfContent, goTo)
    }
}


@Composable
fun Content(tableOfContent: List<Link>, goTo: (Href)-> Unit){
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id =R.dimen.large_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding))
        ) {

            itemsIndexed(items = tableOfContent.filter { it.title!=null }, key = { _, link->link.href}){ index, link->
                val collapsed = remember{ if(link.children.isNotEmpty())mutableStateOf(false) else null }
                val sectionLambda: ()->Unit = if(collapsed!=null) { { collapsed.value = !collapsed.value } } else { { goTo(link.href) } }
                OutlinedCard(
                    onClick = { sectionLambda() }
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.medium_padding))
                    ){
                        Text(
                            text = "$index   ${link.title!!}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding((dimensionResource(id = R.dimen.medium_padding)))
                                .width(50.dp)
                                .weight(1f)
                        )
                        IconButton(onClick = { sectionLambda() },modifier = Modifier.padding(dimensionResource(R.dimen.medium_padding)),){
                            if (collapsed != null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_expand_more_24),
                                    contentDescription = stringResource(id = R.string.expand),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_open_in_new_24),
                                    contentDescription = stringResource(R.string.jump_to),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(dimensionResource(R.dimen.medium_padding))
                                )
                            }
                        }
                    }
                    collapsed?.value?.let {collapsed->
                        AnimatedVisibility(
                            visible = collapsed,
                            enter = fadeIn()+ slideInVertically(
                                initialOffsetY = {fullHeight -> -fullHeight},
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                            ),
                            exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
                            )
                        ) {
                            Column(
                                Modifier
                                    .padding(
                                        start = dimensionResource(id = R.dimen.large_padding),
                                        end = dimensionResource(id = R.dimen.medium_padding)
                                    )
                                    .wrapContentSize(),
                            ){
                                repeat(link.children.size) { index ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                append(bullet)
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {

                                                    append("  ")
                                                    append(link.children[index].title)
                                                }
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier
                                                .width(50.dp)
                                                .weight(1f)
                                        )
                                        IconButton(onClick = { goTo(link.children[index].href) }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_open_in_new_24),
                                                contentDescription = stringResource(R.string.jump_to),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(dimensionResource(R.dimen.medium_padding))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

}