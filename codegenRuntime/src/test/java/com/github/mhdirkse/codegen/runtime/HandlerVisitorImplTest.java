package com.github.mhdirkse.codegen.runtime;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.Assert;

import static org.easymock.EasyMock.*;

@RunWith(EasyMockRunner.class)
public class HandlerVisitorImplTest {
    @Mock
    HandlerRunner<HandlerStub> handlerRunner;

    HandlerStub first;
    HandlerStub second;
    HandlerStub third;

    HandlerVisitorImpl<HandlerStub> instance;

    @Before
    public void setUp() {
        first = new HandlerStub(true);
        second = new HandlerStub(true);
        third = new HandlerStub(true);
        instance = new HandlerVisitorImpl<HandlerStub>(handlerRunner);
    }

    @Test
    public void testWhenRunnerReturnsTrueThenOnHandlerDoes() {
        HandlerStub handler = new HandlerStub(true);
        expect(handlerRunner.run(handler, instance)).andReturn(true);
        replay(handlerRunner);
        instance.onHandler(handler, null, null);
        verify(handlerRunner);
    }

    @Test
    public void testWhenRunnerReturnsFalseThenOnHandlerDoes() {
        HandlerStub handler = new HandlerStub(false);
        expect(handlerRunner.run(handler, instance)).andReturn(false);
        replay(handlerRunner);
        instance.onHandler(handler, null, null);
        verify(handlerRunner);
    }

    @Test
    public void testPrevNextSingleHandler() {
        instance.onHandler(first, null, null);
        Assert.assertTrue(instance.isFirst());
        Assert.assertTrue(instance.isLast());
        Assert.assertNull(instance.getPreviousHandler());
        Assert.assertNull(instance.getNextHandler());
    }

    @Test
    public void testPrevNextFirstOfTwo() {
        instance.onHandler(first, null, second);
        Assert.assertTrue(instance.isFirst());
        Assert.assertFalse(instance.isLast());
        Assert.assertNull(instance.getPreviousHandler());
        Assert.assertSame(second, instance.getNextHandler());
    }

    @Test
    public void testPrevNextLastOfTwo() {
        instance.onHandler(second, first, null);
        Assert.assertFalse(instance.isFirst());
        Assert.assertTrue(instance.isLast());
        Assert.assertSame(first, instance.getPreviousHandler());
        Assert.assertNull(instance.getNextHandler());
    }
}
