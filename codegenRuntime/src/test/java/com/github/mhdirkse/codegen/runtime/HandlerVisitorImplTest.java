package com.github.mhdirkse.codegen.runtime;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.Assert;

import static org.easymock.EasyMock.*;

@RunWith(EasyMockRunner.class)
public class HandlerVisitorImplTest {
    @Mock(type = MockType.STRICT)
    HandlerRunner<HandlerStub> handlerRunner;

    @Mock(type = MockType.STRICT)
    HandlerStackManipulator<HandlerStub> handlerStack;

    HandlerStub first;
    HandlerStub second;
    HandlerStub third;

    HandlerVisitorImpl<HandlerStub> instanceVisiting;
    HandlerVisitorImpl<HandlerStub> instanceStackManipulating;

    @Before
    public void setUp() {
        first = new HandlerStub(true, "first");
        second = new HandlerStub(true, "second");
        third = new HandlerStub(true, "third");
        instanceVisiting = new HandlerVisitorImpl<HandlerStub>(handlerRunner, null);
        instanceStackManipulating = new HandlerVisitorImpl<HandlerStub>(null, handlerStack);
    }

    @Test
    public void testWhenRunnerReturnsTrueThenOnHandlerDoes() {
        HandlerStub handler = new HandlerStub(true);
        expect(handlerRunner.run(handler, instanceVisiting)).andReturn(true);
        replay(handlerRunner);
        Assert.assertTrue(instanceVisiting.onHandler(handler, null, null, 0));
        verify(handlerRunner);
    }

    @Test
    public void testWhenRunnerReturnsFalseThenOnHandlerDoes() {
        HandlerStub handler = new HandlerStub(false);
        expect(handlerRunner.run(handler, instanceVisiting)).andReturn(false);
        replay(handlerRunner);
        Assert.assertFalse(instanceVisiting.onHandler(handler, null, null, 0));
        verify(handlerRunner);
    }

    @Test
    public void testPrevNextSingleHandlerAndSeq() {
        instanceVisiting.onHandler(first, null, null, 0);
        Assert.assertTrue(instanceVisiting.isFirst());
        Assert.assertTrue(instanceVisiting.isLast());
        Assert.assertNull(instanceVisiting.getPreviousHandler());
        Assert.assertNull(instanceVisiting.getNextHandler());
        Assert.assertEquals(0, instanceVisiting.getHandlerSeq());
    }

    @Test
    public void testPrevNextFirstOfTwo() {
        instanceVisiting.onHandler(first, null, second, 0);
        Assert.assertTrue(instanceVisiting.isFirst());
        Assert.assertFalse(instanceVisiting.isLast());
        Assert.assertNull(instanceVisiting.getPreviousHandler());
        Assert.assertSame(second, instanceVisiting.getNextHandler());
    }

    @Test
    public void testPrevNextLastOfTwoAndSeq() {
        instanceVisiting.onHandler(second, first, null, 1);
        Assert.assertFalse(instanceVisiting.isFirst());
        Assert.assertTrue(instanceVisiting.isLast());
        Assert.assertSame(first, instanceVisiting.getPreviousHandler());
        Assert.assertNull(instanceVisiting.getNextHandler());
        Assert.assertEquals(1, instanceVisiting.getHandlerSeq());
    }

    @Test
    public void testWhenAddFirstThenNotYetFirstAdded() {
        replay(handlerStack);
        instanceStackManipulating.addFirst(first);
        verify(handlerStack);
    }

    @Test
    public void testWhenAddFirstAndAfterStackVisitedThenFirstAdded() {
        handlerStack.addFirst(same(first));
        replay(handlerStack);
        instanceStackManipulating.addFirst(first);
        instanceStackManipulating.afterStackVisited();
        verify(handlerStack);
    }

    @Test
    public void testWhenTwoTimesAddFirstAndAfterStackVisitedThenTwoAdded() {
        handlerStack.addFirst(same(second));
        handlerStack.addFirst(same(first));
        replay(handlerStack);
        instanceStackManipulating.addFirst(second);
        instanceStackManipulating.addFirst(first);
        instanceStackManipulating.afterStackVisited();
        verify(handlerStack);
    }

    @Test
    public void testWhenRemoveFirstThenNotYetFirstRemoved() {
        replay(handlerStack);
        instanceStackManipulating.removeFirst();
        verify(handlerStack);
    }

    @Test
    public void testWhenRemoveFirstAndAfterStackVisitedThenFirstRemoved() {
        handlerStack.removeFirst();
        replay(handlerStack);
        instanceStackManipulating.removeFirst();
        instanceStackManipulating.afterStackVisited();
        verify(handlerStack);
    }

    @Test
    public void testWhenTwoTimesRemoveFirstAndAfterStackVisitedThenTwoRemoved() {
        handlerStack.removeFirst();
        handlerStack.removeFirst();
        replay(handlerStack);
        instanceStackManipulating.removeFirst();
        instanceStackManipulating.removeFirst();
        instanceStackManipulating.afterStackVisited();
        verify(handlerStack);
    }
}
