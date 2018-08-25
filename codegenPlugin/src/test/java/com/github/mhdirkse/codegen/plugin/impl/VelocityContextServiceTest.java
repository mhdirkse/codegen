package com.github.mhdirkse.codegen.plugin.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Optional;

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
        Optional<VelocityContext> result = instance.checkNotEmpty(vc, callback);
        verify(callback);
        Assert.assertTrue(result.isPresent());
    }

    @Test
    public void testWhenVelocityContextEmptyThenError() {
        expect(callback.getStatusVelocityContextEmpty()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        Optional<VelocityContext> result = instance.checkNotEmpty(null, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testWhenVelocityContextHasTargetThenNoError() {
        VelocityContext vc = new VelocityContext();
        vc.put("target", "Some value");
        replay(callback);
        Optional<Object> result = instance.checkHasTarget(vc, callback);
        verify(callback);
        Object resultUnwrapped = result.orElseThrow(
                () -> new IllegalArgumentException("Expected that target was filled."));
        Assert.assertEquals("Some value", resultUnwrapped);
    }

    @Test
    public void testWhenVelocityContextLacksTargetThenError() {
        VelocityContext vc = new VelocityContext();
        expect(callback.getStatusVelocityContextLacksTarget()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        Optional<Object> result = instance.checkHasTarget(vc, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testWhenTargetHasClassModelThenNoError() {
        Object target = new ClassModel();
        replay(callback);
        Optional<ClassModel> result = instance.checkTargetIsClassModel(target, callback);
        verify(callback);
        Assert.assertTrue(result.isPresent());
    }

    @Test
    public void testWhenTargetisNotClassModelThenError() {
        Object target = "Some value";
        expect(callback.getStatusTargetTypeMismatch()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        Optional<ClassModel> result = instance.checkTargetIsClassModel(target, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testWhenClassModelHasFullNameThenNoError() {
        ClassModel cm = new ClassModel();
        cm.setFullName("SomeClass");
        replay(callback);
        Optional<String> result = instance.checkClassModelHasFullName(cm, callback);
        verify(callback);
        Assert.assertTrue(result.isPresent());
    }

    @Test
    public void testWhenClassModelLacksFullNameThenError() {
        ClassModel cm = new ClassModel();
        expect(callback.getStatusClassModelNoFullName()).andReturn(
                Status.general(StatusCode.TEST_STATUS_ZERO_ARGS, LogPriority.ERROR));
        replay(callback);
        Optional<String> result = instance.checkClassModelHasFullName(cm, callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
        Assert.assertFalse(result.isPresent());
    }
}
