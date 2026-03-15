package com.example.soen345;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAssertion;

import org.hamcrest.MatcherAssert;

import static org.hamcrest.Matchers.is;

public final class RecyclerViewItemAssertion {

    private RecyclerViewItemAssertion() {
    }

    public static ViewAssertion hasItemCount(int expectedCount) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter == null) throw new AssertionError("RecyclerView has no adapter");
            MatcherAssert.assertThat(adapter.getItemCount(), is(expectedCount));
        };
    }

    public static ViewAssertion atPositionHasText(int position, @IdRes int textViewId, String expectedText) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder == null) throw new AssertionError("No ViewHolder at adapter position: " + position);
            View itemView = viewHolder.itemView.findViewById(textViewId);
            if (!(itemView instanceof TextView)) throw new AssertionError("View at id " + textViewId + " is not a TextView");
            String actual = ((TextView) itemView).getText().toString();
            MatcherAssert.assertThat(actual, is(expectedText));
        };
    }

    public static ViewAssertion containsItemWithText(@IdRes int textViewId, String expectedText) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter == null) throw new AssertionError("RecyclerView has no adapter");

            for (int i = 0; i < adapter.getItemCount(); i++) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder == null) {
                    recyclerView.scrollToPosition(i);
                    recyclerView.measure(
                            View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.EXACTLY)
                    );
                    recyclerView.layout(recyclerView.getLeft(), recyclerView.getTop(), recyclerView.getRight(), recyclerView.getBottom());
                    holder = recyclerView.findViewHolderForAdapterPosition(i);
                }
                if (holder != null) {
                    View child = holder.itemView.findViewById(textViewId);
                    if (child instanceof TextView && expectedText.equals(((TextView) child).getText().toString())) {
                        return;
                    }
                }
            }
            throw new AssertionError("RecyclerView does not contain text: " + expectedText);
        };
    }

    public static ViewAssertion doesNotContainItemWithText(@IdRes int textViewId, String unexpectedText) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter == null) throw new AssertionError("RecyclerView has no adapter");

            for (int i = 0; i < adapter.getItemCount(); i++) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder == null) {
                    recyclerView.scrollToPosition(i);
                    recyclerView.measure(
                            View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.EXACTLY)
                    );
                    recyclerView.layout(recyclerView.getLeft(), recyclerView.getTop(), recyclerView.getRight(), recyclerView.getBottom());
                    holder = recyclerView.findViewHolderForAdapterPosition(i);
                }
                if (holder != null) {
                    View child = holder.itemView.findViewById(textViewId);
                    if (child instanceof TextView && unexpectedText.equals(((TextView) child).getText().toString())) {
                        throw new AssertionError("RecyclerView unexpectedly contains text: " + unexpectedText);
                    }
                }
            }
        };
    }
}
