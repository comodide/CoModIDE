package com.comodide.helper;

import com.mxgraph.swing.mxGraphComponent;

public class GraphComponentHolder {

    private static GraphComponentHolder componentHolder =null;
    private mxGraphComponent component;

    //private constructor to prevent instance creation in other classes
    private GraphComponentHolder() {

    }

    public static GraphComponentHolder getInstance()
    {
        if(componentHolder ==null)
        {
            componentHolder =new GraphComponentHolder();
        }
        return componentHolder;

    }

    public mxGraphComponent getComponent() {
        return component;
    }

    public void setComponent(mxGraphComponent component) {
        this.component = component;
    }
}