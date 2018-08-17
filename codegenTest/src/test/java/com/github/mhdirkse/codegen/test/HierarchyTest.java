package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.test.output.HierarchyInterface;

/**
 * This class tests that interface HierarchyInterface
 * is created correctly. HierarchyInterface should have
 * methods that are based on a class hierarchy in
 * CodegenTestInput.
 * @author martijn
 *
 */
public class HierarchyTest implements HierarchyInterface {
    @Override
    public void hierarchyParent() {
    }

    @Override
    public void hierarchyChild1() {
    }

    @Override
    public void hierarchyChild2() {
    }
}
