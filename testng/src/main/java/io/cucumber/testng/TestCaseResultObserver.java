package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import org.testng.SkipException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.PENDING;
import static io.cucumber.plugin.event.Status.SKIPPED;
import static io.cucumber.plugin.event.Status.UNDEFINED;

class TestCaseResultObserver implements AutoCloseable {

    private static final String SKIPPED_MESSAGE = "This scenario is skipped";

    private final EventPublisher bus;
    private final Map<StepLocation, List<String>> snippetsPerStep = new TreeMap<>();
    private final List<UndefinedStepException.Suggestion> suggestions = new ArrayList<>();
    private final EventHandler<SnippetsSuggestedEvent> snippetsSuggested = this::handleSnippetSuggestedEvent;
    private final EventHandler<TestStepFinished> testStepFinished = this::handleTestStepFinished;
    private final boolean strict;
    private Result result;
    private final EventHandler<TestCaseFinished> testCaseFinished = this::handleTestCaseFinished;

    private TestCaseResultObserver(EventPublisher bus, boolean strict) {
        this.bus = bus;
        this.strict = strict;
        bus.registerHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggested);
        bus.registerHandlerFor(TestStepFinished.class, testStepFinished);
        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinished);
    }

    static TestCaseResultObserver observe(EventBus bus, boolean strict) {
        return new TestCaseResultObserver(bus, strict);
    }

    @Override
    public void close() {
        bus.removeHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggested);
        bus.removeHandlerFor(TestStepFinished.class, testStepFinished);
        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinished);
    }

    private void handleSnippetSuggestedEvent(SnippetsSuggestedEvent event) {
        snippetsPerStep.putIfAbsent(new StepLocation(
                event.getUri(),
                event.getStepLine()
            ),
            event.getSnippets()
        );
    }

    private void handleTestStepFinished(TestStepFinished event) {
        Result result = event.getResult();
        Status status = result.getStatus();
        if (!status.is(UNDEFINED)) {
            return;
        }

        TestStep testStep = event.getTestStep();
        if (!(testStep instanceof PickleStepTestStep)) {
            return;
        }

        PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
        String stepText = pickleStepTestStep.getStepText();

        List<String> snippets = snippetsPerStep.get(
            new StepLocation(
                pickleStepTestStep.getUri(),
                pickleStepTestStep.getStepLine()
            )
        );
        suggestions.add(new UndefinedStepException.Suggestion(stepText, snippets));
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        result = event.getResult();
    }

    void assertTestCasePassed() throws Throwable {
        Status status = result.getStatus();
        if (status.is(PASSED)) {
            return;
        }
        Throwable error = result.getError();
        if (status.is(SKIPPED)) {
            if (error == null) {
                throw new SkipException(SKIPPED_MESSAGE);
            }
            if (error instanceof SkipException) {
                throw error;
            }
            throw new SkipException(result.getError().getMessage(), error);
        }

        if (status.is(UNDEFINED)) {
            throw new UndefinedStepException(suggestions, strict);
        }

        if (status.is(PENDING) && !strict) {
            throw new SkipException(result.getError().getMessage(), result.getError());
        }

        throw error;
    }

    private static final class StepLocation implements Comparable<StepLocation> {
        private final URI uri;
        private final int line;

        private StepLocation(URI uri, int line) {
            this.uri = uri;
            this.line = line;
        }

        @Override
        public int compareTo(StepLocation o) {
            int order = uri.compareTo(o.uri);
            return order != 0 ? order : Integer.compare(line, o.line);
        }
    }

}
