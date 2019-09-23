package com.example.project.ui

import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.example.project.R
import com.example.project.data.db.GameDatabase
import com.example.project.data.service.GamesApiClient
import com.example.project.model.Game
import com.example.project.ui.main.MainActivity
import com.example.project.utils.EspressoIdlingResource
import com.example.project.utils.Listing
import com.example.project.utils.NetworkState
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * @author Baptiste Cassar
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class GameListFragmentTest : KoinTest {

    private val gamesApiClient: GamesApiClient by inject()
    private val gameDatabase: GameDatabase by inject()

    @Before
    fun clearDatabase() {
        gameDatabase.clearAllTables()
    }

    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.
     *
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    @JvmField
    var activityTestRule =
        ActivityTestRule<MainActivity>(MainActivity::class.java)

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun testPaginatedList() {
        val items = gamesApiClient.getGames(size = 5)
            .blockingGet()
        items.forEachIndexed { position, item ->
            Espresso.onView(ViewMatchers.withId(R.id.list_game))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
                .check(
                    ViewAssertions.matches(
                        atPositionOnView(
                            position, ViewMatchers.withText(item.name),
                            R.id.game_name
                        )
                    )
                )
                .check(
                    ViewAssertions.matches(
                        atPositionOnView(
                            position, ViewMatchers.withText(item.summary ?: ""),
                            R.id.game_summary
                        )
                    )
                )
        }
    }

    companion object {
        /**
         * Override default Koin configuration to use Room in-memory database
         */
        @BeforeClass
        @JvmStatic
        fun before() {
            loadKoinModules()
        }

        @AfterClass
        @JvmStatic
        fun after() {
            stopKoin()
        }
    }

    /**
     * extract the latest paged list from the listing
     */
    private fun getPagedList(listing: Listing<Game>): PagedList<Game> {
        val observer = LoggingObserver<PagedList<Game>>()
        listing.pagedList.observeForever(observer)
        MatcherAssert.assertThat(observer.value, CoreMatchers.`is`(CoreMatchers.notNullValue()))
        return observer.value!!
    }

    /**
     * extract the latest network state from the listing
     */
    private fun getNetworkState(listing: Listing<Game>): NetworkState? {
        val networkObserver = LoggingObserver<NetworkState>()
        listing.networkState.observeForever(networkObserver)
        return networkObserver.value
    }

    /**
     * simple observer that logs the latest value it receives
     */
    private class LoggingObserver<T> : Observer<T> {
        var value: T? = null
        override fun onChanged(t: T?) {
            this.value = t
        }
    }


}

fun atPositionOnView(
    position: Int, itemMatcher: Matcher<View>,
    targetViewId: Int
): Matcher<View> {

    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has view id $itemMatcher at position $position")
        }

        public override fun matchesSafely(recyclerView: RecyclerView): Boolean {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            val targetView = viewHolder!!.itemView.findViewById<View>(targetViewId)
            return itemMatcher.matches(targetView)
        }
    }
}