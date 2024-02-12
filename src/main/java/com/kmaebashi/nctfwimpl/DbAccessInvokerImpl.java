package com.kmaebashi.nctfwimpl;

import com.kmaebashi.nctfw.*;

public class DbAccessInvokerImpl implements DbAccessInvoker {
    private DbAccessContext context;

    public DbAccessInvokerImpl(DbAccessContext context) {
        this.context = context;
    }

    @Override
    public <R> R invoke(ThrowableFunction<DbAccessContext, R> logic) {
        R ret;
        try {
            ret = logic.apply(this.context);
        } catch (Exception ex) {
            this.context.getLogger().error("DBアクセスでエラーが発生しました。\n" + ex.toString());
            throw new InternalException("DBアクセスでエラーが発生しました。", ex);
        }
        return ret;
    }

    DbAccessContext getContext() {
        return this.context;
    }
}
