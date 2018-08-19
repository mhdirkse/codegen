package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.test.output.HierarchyFilteredInterface;

/**
 * This class tests that interface HierarchyFilteredInterface
 * is created correctly. HierarchyFilteredInterface should have
 * methods that are based on a class hierarchy in
 * CodegenTestInput, and only classes implementing a test interface
 * "Composite" are used.
 * @author martijn
 *
 */
public class HierarchyFilteredTest implements HierarchyFilteredInterface {
    @Override
    public void hierarchyChild2() {
    }
}
