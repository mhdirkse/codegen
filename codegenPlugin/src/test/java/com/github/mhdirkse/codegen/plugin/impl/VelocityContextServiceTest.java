package com.github.mhdirkse.codegen.plugin.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.apache.velocity.VelocityContext;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.mhdirkse.codegen.compiletime.ClassModel;

@RunWith(EasyMockRunner.class)
public class VelocityContextServiceTest {
    private StatusReportingServiceStub reporter;

    @Mock
    private VelocityContextService.Callback callback;

    private VelocityContextService instance;

    @Before
    public void setUp() {
        reporter = new StatusReportingServiceStub();
        ServiceFactoryImpl sf = new ServiceFactoryImpl(null, reporter, null);
        instance = sf.velocityContextService();
    }

    @Test
    public void testWhenVelocityContextFilledThenNoError() {
        VelocityContext vc = new VelocityContext();
        replay(callback);
        instance.checkNotEmpty(vc, callback);
        verify(callback);
    }

    @Test
    public void testWhenVelocityContextEmptyThenError() {
        expect(callback.getStatusVelocityContextEmpty()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        instance.checkNotEmpty(null, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
    }

    @Test
    public void testWhenVelocityContextHasTargetThenNoError() {
        VelocityContext vc = new VelocityContext();
        vc.put("target", "Some value");
        replay(callback);
        instance.checkHasTarget(vc, callback);
        verify(callback);
    }

    @Test
    public void testWhenVelocityContextLacksTargetThenError() {
        VelocityContext vc = new VelocityContext();
        expect(callback.getStatusVelocityContextLacksTarget()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        instance.checkHasTarget(vc, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
    }

    @Test
    public void testWhenTargetHasClassModelThenNoError() {
        VelocityContext vc = new VelocityContext();
        vc.put("target", new ClassModel());
        replay(callback);
        instance.checkTargetIsClassModel(vc, callback);
        verify(callback);
    }

    @Test
    public void testWhenTargetisNotClassModelThenError() {
        VelocityContext vc = new VelocityContext();
        vc.put("target", "Some value");
        expect(callback.getStatusTargetTypeMismatch()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        instance.checkTargetIsClassModel(vc, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
    }
}
