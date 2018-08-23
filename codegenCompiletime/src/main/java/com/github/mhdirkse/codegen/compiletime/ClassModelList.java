package com.github.mhdirkse.codegen.compiletime;

import java.util.ArrayList;
import java.util.Collection;

public class ClassModelList extends ArrayList<ClassModel> {
    private static final long serialVersionUID = 1177651868233360461L;

    public ClassModelList() {
        super();
    }

    public ClassModelList(final Collection<ClassModel> origs) {
        super(origs);
    }
}
