package com.comodide.helper;

import com.mxgraph.swing.mxGraphComponent;

public class Singleton {

    private static Singleton singletonInstance=null;
    mxGraphComponent component;

 //private constructor to prevent instance creation in other classes
    private Singleton() {

    }

    public static Singleton getInstance()
    {
        if(singletonInstance==null)
            {
                singletonInstance=new Singleton();
            }
        return singletonInstance;

    }

    public mxGraphComponent getComponent() {
        return component;
    }

    public void setComponent(mxGraphComponent component) {
        this.component = component;
    }
}
