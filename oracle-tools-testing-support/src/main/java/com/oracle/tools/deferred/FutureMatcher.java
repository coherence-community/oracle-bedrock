package com.oracle.tools.deferred;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.Is;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author jk 2016.04.08
 */
public class FutureMatcher<T> extends TypeSafeDiagnosingMatcher<CompletableFuture<? super T>>
{
    private final Matcher<? super T> resultMatcher;

    public FutureMatcher(Matcher<? super T> resultMatcher)
    {
        this.resultMatcher = resultMatcher;
    }

    @Override
    protected boolean matchesSafely(CompletableFuture<? super T> item, Description mismatchDescription)
    {
        try
        {
            Object result = item.get();

            return resultMatcher.matches(result);
        }
        catch (InterruptedException | ExecutionException e)
        {
            mismatchDescription.appendText(" but completed with exception ").appendValue(e);
            return false;
        }
    }

    @Override
    public void describeTo(Description description)
    {
        description
            .appendText("a CompletableFuture with result ")
            .appendDescriptionOf(resultMatcher);
    }




    /**
     * Decorates another Matcher, retaining its behaviour, but allowing tests
     * to be slightly more expressive.
     * <p/>
     * For example:
     * <pre>assertThat(cheese, is(equalTo(smelly)))</pre>
     * instead of:
     * <pre>assertThat(cheese, equalTo(smelly))</pre>
     *
     */
    @Factory
    public static <T> Matcher<T> futureOf(Matcher<T> matcher) {
        return new Is<T>(matcher);
    }

    /**
     * A shortcut to the frequently used <code>is(equalTo(x))</code>.
     * <p/>
     * For example:
     * <pre>assertThat(cheese, is(smelly))</pre>
     * instead of:
     * <pre>assertThat(cheese, is(equalTo(smelly)))</pre>
     *
     */
    @Factory
    public static <T> Matcher<T> futureOf(T value) {
        return futureOf(equalTo(value));
    }
}
