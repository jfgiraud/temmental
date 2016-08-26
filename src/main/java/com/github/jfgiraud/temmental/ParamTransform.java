package com.github.jfgiraud.temmental;

public abstract class ParamTransform<Init,In,Out> implements Transform<Init,Transform<In,Out>> {

    public abstract Out apply(Init values, In value);

    public final Transform<In, Out> apply(final Init values) {
        return  new Transform<In, Out>() {
            public Out apply(In value) {
                return ParamTransform.this.apply(values, value);
            }
        };
    }

}
