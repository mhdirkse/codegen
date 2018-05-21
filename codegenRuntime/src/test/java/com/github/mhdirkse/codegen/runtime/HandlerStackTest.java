package com.github.mhdirkse.codegen.runtime;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.isNull;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class HandlerStackTest {
    private HandlerStack<HandlerStub> instance;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock(type = MockType.STRICT)
    private HandlerVisitor<HandlerStub> visitor;

    @Before
    public void setUp() {
        instance = new HandlerStack<>();
    }

    @Test
    public void testWhenNoHandlersThenClearError() {
        replay(visitor);
        exception.expect(NotHandledException.class);
        exception.expectMessage("No handlers");
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenOneHandlerThenVisited() {
        HandlerStub handler = new HandlerStub(true);
        instance.addFirst(handler);
        expect(visitor.onHandler(same(handler), isNull(HandlerStub.class), isNull(HandlerStub.class))).andReturn(true);
        replay(visitor);
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenNotHandledThenException() {
        HandlerStub handler = new HandlerStub(true, "theHandler");
        instance.addFirst(handler);
        expect(visitor.onHandler(same(handler), isNull(HandlerStub.class), isNull(HandlerStub.class))).andReturn(false);
        replay(visitor);
        exception.expect(NotHandledException.class);
        exception.expectMessage("theHandler");
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenTwoHandlersThenVisitedInOrder() {
        HandlerStub first = new HandlerStub(true);
        HandlerStub second = new HandlerStub(true);
        instance.addFirst(second);
        instance.addFirst(first);
        expect(visitor.onHandler(same(first), isNull(HandlerStub.class), same(second))).andReturn(false);
        expect(visitor.onHandler(same(second), same(first), isNull(HandlerStub.class))).andReturn(true);
        replay(visitor);
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenFirstHandlesThenSecondHandlerIgnored() {
        HandlerStub first = new HandlerStub(true);
        HandlerStub second = new HandlerStub(true);
        instance.addFirst(second);
        instance.addFirst(first);
        expect(visitor.onHandler(same(first), isNull(HandlerStub.class), same(second))).andReturn(true);
        replay(visitor);
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenTwoHandlersDontHandleThenErrorMentionesBoth() {
        HandlerStub first = new HandlerStub(true, "first");
        HandlerStub second = new HandlerStub(true, "second");
        instance.addFirst(second);
        instance.addFirst(first);
        expect(visitor.onHandler(same(first), isNull(HandlerStub.class), same(second))).andReturn(false);
        expect(visitor.onHandler(same(second), same(first), isNull(HandlerStub.class))).andReturn(false);
        replay(visitor);
        exception.expect(NotHandledException.class);
        exception.expectMessage("first, second");
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenHandlerRemovedThenHandlerNotUsed() {
        HandlerStub first = new HandlerStub(true);
        HandlerStub second = new HandlerStub(true);
        instance.addFirst(second);
        instance.addFirst(first);
        instance.removeFirst();
        expect(visitor.onHandler(same(second), isNull(HandlerStub.class), isNull(HandlerStub.class))).andReturn(true);
        replay(visitor);
        instance.run(visitor);
        verify(visitor);
    }

    @Test
    public void testWhenThreeHandlersThenCurrentPrevNextRight() {
        HandlerStub first = new HandlerStub(true);
        HandlerStub second = new HandlerStub(true);
        HandlerStub third = new HandlerStub(true);
        instance.addFirst(third);
        instance.addFirst(second);
        instance.addFirst(first);
        expect(visitor.onHandler(same(first), isNull(HandlerStub.class), same(second))).andReturn(false);
        expect(visitor.onHandler(same(second), same(first), same(third))).andReturn(false);
        expect(visitor.onHandler(same(third), same(second), isNull(HandlerStub.class))).andReturn(true);
        replay(visitor);
        instance.run(visitor);
        verify(visitor);
    }
}
