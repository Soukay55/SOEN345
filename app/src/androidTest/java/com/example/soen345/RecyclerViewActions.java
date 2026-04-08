package com.example.soen345;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;


public final class RecyclerViewActions {

    private RecyclerViewActions() {}

    /**
     * Performs a click on {@code childViewId} inside the first RecyclerView card
     * whose {@code R.id.eventTitle} TextView equals {@code title}.
     */
    public static ViewAction actionOnItemWithTitle(String title, @IdRes int childViewId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "Click child " + childViewId + " on card with title: " + title;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView rv = (RecyclerView) view;
                RecyclerView.Adapter<?> adapter = rv.getAdapter();
                if (adapter == null) throw new AssertionError("RecyclerView has no adapter");

                for (int i = 0; i < adapter.getItemCount(); i++) {
                    // Ensure the ViewHolder is bound by scrolling to it
                    rv.scrollToPosition(i);
                    uiController.loopMainThreadUntilIdle();

                    RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(i);
                    if (holder == null) continue;

                    View titleView = holder.itemView.findViewById(R.id.eventTitle);
                    if (titleView instanceof TextView
                            && title.equals(((TextView) titleView).getText().toString())) {
                        View target = holder.itemView.findViewById(childViewId);
                        if (target == null) {
                            throw new AssertionError(
                                    "Child view " + childViewId + " not found in card at position " + i);
                        }
                        target.performClick();
                        uiController.loopMainThreadUntilIdle();
                        return;
                    }
                }
                throw new AssertionError("No card found with title: " + title);
            }
        };
    }
}





