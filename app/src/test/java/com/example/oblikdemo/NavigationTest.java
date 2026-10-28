package com.example.oblikdemo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@LooperMode(LooperMode.Mode.PAUSED)
public class NavigationTest {
    private ActivityController<MainActivity> controller;
    private MainActivity.DemoView view;

    @Before public void setUp() {
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        view = controller.get().demo;
        view.layout(0, 0, 684, 1416);
    }

    @After public void tearDown() {
        if (view.animator != null) view.animator.end();
        controller.pause().stop().destroy();
    }

    @Test public void everyFrameShowsDestinationIncludingBeforeEndCallback() {
        float[] progress = {0f, .5f, .9989f, .999f, .9995f, .99999f, 1f};
        for (MainActivity.Screen source : MainActivity.Screen.values()) {
            for (MainActivity.Screen target : MainActivity.Screen.values()) {
                if (source == target) continue;
                view.current = source;
                view.switchTo(target);
                String context = source + " -> " + target;
                assertEquals(context, target, view.current);
                RecordingCanvas expected = new RecordingCanvas();
                view.drawScreen(expected, target, 255, 0);
                for (float fraction : progress) {
                    // Explicitly render a frame before the animator's end callback.
                    view.transition = fraction;
                    RecordingCanvas actual = new RecordingCanvas();
                    view.onDraw(actual);
                    assertEquals(context + " at " + fraction, expected.text, actual.text);
                }
                view.animator.end();
                assertEquals(context, target, view.current);
                assertEquals(1f, view.transition, 0f);
            }
        }
    }

    @Test public void finishingOrCancellingAnimationKeepsDestination() {
        view.current = MainActivity.Screen.MENU;
        view.switchTo(MainActivity.Screen.SERVICES);
        view.animator.cancel();
        assertEquals(MainActivity.Screen.SERVICES, view.current);
        assertEquals(1f, view.transition, 0f);
        RecordingCanvas frame = new RecordingCanvas();
        view.onDraw(frame);
        assertTrue(frame.text.contains("Виправити дані онлайн"));
        assertFalse(frame.text.contains("Активні сесії"));

        view.switchTo(MainActivity.Screen.MENU);
        view.animator.end();
        assertEquals(MainActivity.Screen.MENU, view.current);
        view.switchTo(MainActivity.Screen.SERVICES);
        view.animator.end();
        assertEquals(MainActivity.Screen.SERVICES, view.current);
    }

    @Test public void inputDuringTransitionDoesNotActOnIncomingPage() {
        view.current = MainActivity.Screen.MAIN;
        view.switchTo(MainActivity.Screen.DETAILS);
        assertTrue(view.animator.isRunning());
        assertTrue(view.goBack());
        assertEquals(MainActivity.Screen.DETAILS, view.current);

        view.detailsScroll = 100f;
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 80, 800, 0);
        MotionEvent move = MotionEvent.obtain(0, 10, MotionEvent.ACTION_MOVE, 80, 600, 0);
        try {
            view.onTouchEvent(down);
            view.onTouchEvent(move);
            assertEquals(100f, view.detailsScroll, 0f);
        } finally {
            down.recycle();
            move.recycle();
        }
        view.animator.end();
        assertTrue(view.goBack());
        assertEquals(MainActivity.Screen.MAIN, view.current);
    }

    private static class RecordingCanvas extends Canvas {
        final List<String> text = new ArrayList<>();
        @Override public void drawText(String value, float x, float y, Paint paint) {
            text.add(value);
        }
    }
}

